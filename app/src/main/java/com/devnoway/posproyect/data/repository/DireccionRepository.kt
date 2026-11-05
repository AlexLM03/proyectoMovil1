package com.devnoway.posproyect.data.repository

import com.devnoway.posproyect.data.conectividad.ObservadorConectividad
import com.devnoway.posproyect.data.local.daos.DireccionDao
import com.devnoway.posproyect.data.mapeadores.aEntidad
import com.devnoway.posproyect.data.mapeadores.aModelo
import com.devnoway.posproyect.data.mapeadores.aSolicitud
import com.devnoway.posproyect.data.modelo.Direccion
import com.devnoway.posproyect.data.remote.ApiServicio
import com.devnoway.posproyect.data.remote.Resultado
import com.devnoway.posproyect.data.remote.ejecutarRed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repositorio de direcciones de entrega.
 *
 * La lista se guarda en Room (se puede consultar sin conexión) y se administra
 * contra la API: alta, edición, borrado y dirección predeterminada.
 */
class DireccionRepository(
    private val direccionDao: DireccionDao,
    private val api: ApiServicio,
    private val conectividad: ObservadorConectividad
) {

    fun direcciones(usuarioId: Int): Flow<List<Direccion>> =
        direccionDao.observarDeUsuario(usuarioId).map { lista -> lista.map { it.aModelo() } }

    val enLinea: Flow<Boolean> get() = conectividad.observar()

    fun hayConexion(): Boolean = conectividad.estaEnLinea()

    /** Dirección que debe proponerse al confirmar el pedido. */
    suspend fun predeterminada(usuarioId: Int): Direccion? {
        val locales = direccionDao.listarDeUsuario(usuarioId).map { it.aModelo() }
        return locales.firstOrNull { it.predeterminada } ?: locales.firstOrNull()
    }

    suspend fun sincronizar(usuarioId: Int): Resultado<Int> {
        if (!conectividad.estaEnLinea()) {
            return Resultado.Fallo("Sin conexión: se muestran las direcciones guardadas.")
        }
        return ejecutarRed {
            val lista = api.direcciones().direcciones.orEmpty()
            direccionDao.borrarDeUsuario(usuarioId)
            direccionDao.guardar(lista.map { it.aEntidad() })
            lista.size
        }
    }

    /** Crea (id = 0) o actualiza una dirección y refresca la copia local. */
    suspend fun guardar(usuarioId: Int, direccion: Direccion): Resultado<Unit> {
        if (direccion.alias.isBlank() || direccion.calle.isBlank()) {
            return Resultado.Fallo("Escribe un alias y la dirección.")
        }
        if (!conectividad.estaEnLinea()) {
            return Resultado.Fallo("Necesitas conexión para guardar una dirección.")
        }
        return ejecutarRed {
            val solicitud = direccion.aSolicitud()
            if (direccion.id == 0) {
                api.crearDireccion(solicitud)
            } else {
                api.actualizarDireccion(direccion.id, solicitud)
            }
            refrescarLocal(usuarioId)
        }
    }

    suspend fun eliminar(usuarioId: Int, id: Int): Resultado<Unit> {
        if (!conectividad.estaEnLinea()) {
            return Resultado.Fallo("Necesitas conexión para eliminar una dirección.")
        }
        return ejecutarRed {
            val respuesta = api.eliminarDireccion(id)
            if (!respuesta.ok) {
                throw IllegalStateException(
                    respuesta.error?.mensaje ?: "El servidor no permitió eliminar la dirección."
                )
            }
            refrescarLocal(usuarioId)
        }
    }

    private suspend fun refrescarLocal(usuarioId: Int) {
        val lista = api.direcciones().direcciones.orEmpty()
        direccionDao.borrarDeUsuario(usuarioId)
        direccionDao.guardar(lista.map { it.aEntidad() })
    }
}
