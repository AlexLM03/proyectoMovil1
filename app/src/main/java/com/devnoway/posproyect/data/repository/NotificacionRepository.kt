package com.devnoway.posproyect.data.repository

import com.devnoway.posproyect.data.conectividad.ObservadorConectividad
import com.devnoway.posproyect.data.local.daos.NotificacionDao
import com.devnoway.posproyect.data.mapeadores.aEntidad
import com.devnoway.posproyect.data.mapeadores.aModelo
import com.devnoway.posproyect.data.modelo.Notificacion
import com.devnoway.posproyect.data.remote.ApiServicio
import com.devnoway.posproyect.data.remote.Resultado
import com.devnoway.posproyect.data.remote.ejecutarRed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repositorio de notificaciones (avisos del estado de los pedidos).
 *
 * El modelo y el DAO ya están listos para la pantalla de avisos; por ahora se
 * usa el contador de no leídas en el perfil.
 */
class NotificacionRepository(
    private val notificacionDao: NotificacionDao,
    private val api: ApiServicio,
    private val conectividad: ObservadorConectividad
) {

    val noLeidas: Flow<Int> = notificacionDao.observarNoLeidas()

    val enLinea: Flow<Boolean> get() = conectividad.observar()

    fun deUsuario(usuarioId: Int): Flow<List<Notificacion>> =
        notificacionDao.observarDeUsuario(usuarioId).map { lista -> lista.map { it.aModelo() } }

    suspend fun sincronizar(clienteId: Int?): Resultado<Int> {
        if (!conectividad.estaEnLinea()) {
            return Resultado.Fallo("Sin conexión: se muestran los avisos guardados.")
        }
        return ejecutarRed {
            val notificaciones = api.notificaciones(clienteId).notificaciones.orEmpty()
            notificacionDao.guardarNotificaciones(notificaciones.map { it.aEntidad() })
            notificaciones.size
        }
    }

    /** Marca leída en local y, si hay red, también en el servidor. */
    suspend fun marcarLeida(id: Int): Resultado<Unit> {
        notificacionDao.marcarLeida(id)
        if (!conectividad.estaEnLinea()) return Resultado.Exito(Unit)
        return ejecutarRed { api.marcarNotificacionLeida(id) }
    }
}
