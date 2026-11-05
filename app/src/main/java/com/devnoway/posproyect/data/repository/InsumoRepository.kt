package com.devnoway.posproyect.data.repository

import com.devnoway.posproyect.data.conectividad.ObservadorConectividad
import com.devnoway.posproyect.data.local.daos.InsumoDao
import com.devnoway.posproyect.data.mapeadores.aEntidad
import com.devnoway.posproyect.data.mapeadores.aModelo
import com.devnoway.posproyect.data.mapeadores.aSolicitud
import com.devnoway.posproyect.data.modelo.Insumo
import com.devnoway.posproyect.data.remote.ApiServicio
import com.devnoway.posproyect.data.remote.Resultado
import com.devnoway.posproyect.data.remote.ejecutarRed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repositorio del inventario de insumos.
 *
 * Lectura: Room (funciona sin conexión).
 * Escritura: va al servidor y luego se refleja en local. Modificar el
 * inventario es una operación administrativa, así que exige conexión; se avisa
 * al usuario en lugar de inventar un estado que el servidor no conoce.
 */
class InsumoRepository(
    private val insumoDao: InsumoDao,
    private val api: ApiServicio,
    private val conectividad: ObservadorConectividad
) {

    val insumos: Flow<List<Insumo>> =
        insumoDao.observarInsumos().map { lista -> lista.map { it.aModelo() } }

    val enLinea: Flow<Boolean> get() = conectividad.observar()

    fun hayConexion(): Boolean = conectividad.estaEnLinea()

    /** Descarga el inventario completo y reemplaza la copia local. */
    suspend fun sincronizar(): Resultado<Int> {
        if (!conectividad.estaEnLinea()) {
            return Resultado.Fallo("Sin conexión: se muestra el inventario guardado.")
        }
        return ejecutarRed {
            val insumosDto = api.insumos().insumos.orEmpty()
            insumoDao.borrarTodo()
            insumoDao.guardarInsumos(insumosDto.map { it.aEntidad() })
            insumosDto.size
        }
    }

    /** Crea (id = 0) o actualiza (id > 0) un insumo. */
    suspend fun guardar(insumo: Insumo): Resultado<Insumo> {
        if (insumo.nombre.isBlank() || insumo.unidad.isBlank()) {
            return Resultado.Fallo("El nombre y la unidad son obligatorios.")
        }
        if (insumo.stock < 0 || insumo.minimo < 0) {
            return Resultado.Fallo("Las existencias y el mínimo no pueden ser negativos.")
        }
        if (!conectividad.estaEnLinea()) {
            return Resultado.Fallo("Necesitas conexión para modificar el inventario.")
        }
        return ejecutarRed {
            val respuesta = if (insumo.id == 0) {
                api.crearInsumo(insumo.aSolicitud())
            } else {
                api.actualizarInsumo(insumo.id, insumo.aSolicitud())
            }
            val dto = respuesta.insumo
                ?: throw IllegalStateException("El servidor no devolvió el insumo guardado.")
            val entidad = dto.aEntidad()
            insumoDao.guardarInsumo(entidad)
            entidad.aModelo()
        }
    }

    suspend fun eliminar(id: Int): Resultado<Unit> {
        if (!conectividad.estaEnLinea()) {
            return Resultado.Fallo("Necesitas conexión para eliminar un insumo.")
        }
        return ejecutarRed {
            val respuesta = api.eliminarInsumo(id)
            if (!respuesta.ok) {
                throw IllegalStateException(
                    respuesta.error?.mensaje ?: "El servidor no permitió eliminar el insumo."
                )
            }
            insumoDao.borrarInsumo(id)
        }
    }
}
