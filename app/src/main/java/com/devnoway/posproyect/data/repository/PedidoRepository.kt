package com.devnoway.posproyect.data.repository

import androidx.room.withTransaction
import com.devnoway.posproyect.data.conectividad.ObservadorConectividad
import com.devnoway.posproyect.data.local.AcambapanDatabase
import com.devnoway.posproyect.data.local.daos.PedidoDao
import com.devnoway.posproyect.data.local.entidades.DetallePedidoEntity
import com.devnoway.posproyect.data.local.entidades.PedidoEntity
import com.devnoway.posproyect.data.mapeadores.aDetalles
import com.devnoway.posproyect.data.mapeadores.aEntidad
import com.devnoway.posproyect.data.mapeadores.aModelo
import com.devnoway.posproyect.data.modelo.EstadoPedido
import com.devnoway.posproyect.data.modelo.NuevoPedido
import com.devnoway.posproyect.data.modelo.Pedido
import com.devnoway.posproyect.data.modelo.ResumenNegocio
import com.devnoway.posproyect.data.modelo.enPesos
import com.devnoway.posproyect.data.remote.ApiServicio
import com.devnoway.posproyect.data.remote.Resultado
import com.devnoway.posproyect.data.remote.dto.CrearPedidoSolicitud
import com.devnoway.posproyect.data.remote.dto.EstadoSolicitud
import com.devnoway.posproyect.data.remote.dto.ItemSolicitud
import com.devnoway.posproyect.data.remote.dto.PedidoDto
import com.devnoway.posproyect.data.remote.ejecutarRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repositorio de pedidos.
 *
 * Crear un pedido SIEMPRE escribe primero en Room: si no hay red, el pedido
 * queda con `pendiente_sincronizar = true` y se envía después (cola offline).
 * El folio se genera en el dispositivo para que el envío sea idempotente: si
 * la petición se repite, el servidor devuelve el mismo pedido sin duplicarlo.
 */
class PedidoRepository(
    private val baseDeDatos: AcambapanDatabase,
    private val pedidoDao: PedidoDao,
    private val api: ApiServicio,
    private val conectividad: ObservadorConectividad
) {

    val pedidos: Flow<List<Pedido>> =
        pedidoDao.observarPedidos().map { lista -> lista.map { it.aModelo() } }

    val ultimoPedido: Flow<Pedido?> =
        pedidoDao.observarUltimoPedido().map { it?.aModelo() }

    fun pedidosDeCliente(clienteId: Int): Flow<List<Pedido>> =
        pedidoDao.observarPedidosDeCliente(clienteId).map { lista -> lista.map { it.aModelo() } }

    fun pedido(folio: String): Flow<Pedido?> =
        pedidoDao.observarPedido(folio).map { it?.aModelo() }

    fun hayConexion(): Boolean = conectividad.estaEnLinea()

    val enLinea: Flow<Boolean> get() = conectividad.observar()

    /**
     * Guarda el pedido en local y trata de enviarlo en el acto.
     * Devuelve el folio generado aunque no haya conexión: la pantalla de
     * seguimiento mostrará que está pendiente de envío.
     */
    suspend fun crear(nuevo: NuevoPedido): Resultado<String> {
        if (nuevo.lineas.isEmpty()) {
            return Resultado.Fallo("Agrega al menos un producto al carrito.")
        }
        val folio = "APP-" + System.currentTimeMillis().toString(36).uppercase(Locale.US)
        val subtotal = nuevo.lineas.sumOf { it.subtotal }
        val costoEnvio = if (nuevo.aDomicilio && subtotal < ENVIO_GRATIS_DESDE) COSTO_ENVIO else 0.0
        val total = subtotal + costoEnvio

        val entidad = PedidoEntity(
            folio = folio,
            clienteId = nuevo.clienteId,
            clienteNombre = nuevo.clienteNombre,
            clienteTelefono = nuevo.clienteTelefono,
            tipoEntrega = if (nuevo.aDomicilio) "A domicilio" else "Para recoger",
            direccionEntrega = if (nuevo.aDomicilio) nuevo.direccion else null,
            tipoEmpaque = nuevo.tipoEmpaque,
            metodoPago = nuevo.metodoPago.codigo,
            requiereRebanado = nuevo.requiereRebanado,
            notasPanadero = nuevo.notas,
            subtotal = subtotal,
            costoEnvio = costoEnvio,
            total = total,
            estado = EstadoPedido.RECIBIDO.codigo,
            repartidorId = null,
            repartidorNombre = null,
            repartidorTelefono = null,
            tiempoEstimado = "15 min",
            fechaPedido = ahora(),
            pendienteSincronizar = true
        )

        val detalles = nuevo.lineas.map { linea ->
            DetallePedidoEntity(
                pedidoFolio = folio,
                productoId = linea.producto.id,
                productoNombre = linea.producto.nombre,
                cantidad = linea.cantidad,
                precioUnitario = linea.producto.precio
            )
        }

        baseDeDatos.withTransaction {
            pedidoDao.guardarPedido(entidad)
            pedidoDao.guardarDetalles(detalles)
        }

        // Intentamos subirlo de inmediato; si falla, se queda en la cola.
        if (conectividad.estaEnLinea()) {
            empujarPedido(folio)
        }
        return Resultado.Exito(folio)
    }

    /** Reintenta enviar los pedidos creados sin conexión y refresca los existentes. */
    suspend fun sincronizar(clienteId: Int?, esAdmin: Boolean): Resultado<Int> {
        if (!conectividad.estaEnLinea()) {
            return Resultado.Fallo("Sin conexión: los pedidos se enviarán cuando vuelva la red.")
        }
        return ejecutarRed {
            var enviados = 0
            pedidoDao.pendientesDeSincronizar().forEach { pendiente ->
                if (empujarPedido(pendiente.folio) is Resultado.Exito) enviados++
            }

            val respuesta = api.pedidos(clienteId = if (esAdmin) null else clienteId)
            respuesta.pedidos.orEmpty().forEach { guardarDesdeServidor(it) }
            enviados
        }
    }

    /** Fuerza el envío de un pedido pendiente (botón "Reintentar" del seguimiento). */
    suspend fun enviarPendiente(folio: String): Resultado<Unit> {
        if (!conectividad.estaEnLinea()) {
            return Resultado.Fallo("Sin conexión: el pedido sigue guardado en el teléfono.")
        }
        return empujarPedido(folio)
    }

    /** Cambia el estado de un pedido y lo refleja en el servidor (requiere red). */
    suspend fun cambiarEstado(folio: String, estado: EstadoPedido): Resultado<Unit> {
        val local = pedidoDao.buscarPedido(folio)
            ?: return Resultado.Fallo("El pedido $folio no existe en el teléfono.")

        if (local.pendienteSincronizar) {
            pedidoDao.actualizarEstado(folio, estado.codigo)
            return Resultado.Exito(Unit)
        }
        if (!conectividad.estaEnLinea()) {
            return Resultado.Fallo("Necesitas conexión para cambiar el estado de un pedido enviado.")
        }
        return ejecutarRed {
            val respuesta = api.cambiarEstadoPedido(
                folio = folio,
                cuerpo = EstadoSolicitud(
                    estado = estado.codigo,
                    tiempoEstimado = if (estado == EstadoPedido.EN_CAMINO) "12 min" else null
                )
            )
            val pedido = respuesta.pedido
                ?: throw IllegalStateException("El servidor no devolvió el pedido actualizado.")
            guardarDesdeServidor(pedido)
        }
    }

    /** Cancela un pedido (el servidor lo marca CANCELADO, no lo borra). */
    suspend fun cancelar(folio: String): Resultado<Unit> {
        if (!conectividad.estaEnLinea()) {
            return Resultado.Fallo("Necesitas conexión para cancelar el pedido.")
        }
        return ejecutarRed {
            val respuesta = api.cancelarPedido(folio)
            val pedido = respuesta.pedido
                ?: throw IllegalStateException("El servidor no devolvió el pedido cancelado.")
            guardarDesdeServidor(pedido)
        }
    }

    /** Trae un pedido concreto del servidor (seguimiento casi en tiempo real). */
    suspend fun refrescar(folio: String): Resultado<Unit> {
        if (!conectividad.estaEnLinea()) {
            return Resultado.Fallo("Sin conexión: se muestra el último estado guardado.")
        }
        return ejecutarRed {
            val respuesta = api.pedido(folio)
            val pedido = respuesta.pedido
                ?: throw IllegalStateException("El pedido $folio no existe en el servidor.")
            guardarDesdeServidor(pedido)
        }
    }

    /**
     * Números del día calculados por el servidor.
     *
     * El dash los prefiere sobre el cálculo local: así no depende del reloj del
     * teléfono (un emulador con la fecha cambiada mostraría ventas en cero).
     */
    suspend fun resumenServidor(): Resultado<ResumenNegocio> {
        if (!conectividad.estaEnLinea()) {
            return Resultado.Fallo("Sin conexión: se muestran los datos guardados.")
        }
        return ejecutarRed {
            val resumen = api.resumen().resumen
                ?: throw IllegalStateException("El servidor no devolvió el resumen.")
            ResumenNegocio(
                pedidosHoy = resumen.pedidosHoy,
                ventasHoy = resumen.ventasHoy,
                ticketPromedio = resumen.ticketPromedio,
                insumosBajos = resumen.insumosBajos
            )
        }
    }

    private suspend fun empujarPedido(folio: String): Resultado<Unit> {
        val entidad = pedidoDao.buscarPedido(folio)
            ?: return Resultado.Fallo("El pedido $folio ya no está en el teléfono.")
        val detalles = pedidoDao.detallesDe(folio)
        if (detalles.isEmpty()) {
            return Resultado.Fallo("El pedido $folio no tiene productos.")
        }

        val solicitud = CrearPedidoSolicitud(
            folio = entidad.folio,
            clienteId = entidad.clienteId,
            clienteNombre = entidad.clienteNombre,
            clienteTelefono = entidad.clienteTelefono,
            tipoEntrega = entidad.tipoEntrega,
            direccionEntrega = entidad.direccionEntrega,
            tipoEmpaque = entidad.tipoEmpaque,
            metodoPago = entidad.metodoPago,
            requiereRebanado = entidad.requiereRebanado,
            notasPanadero = entidad.notasPanadero,
            costoEnvio = entidad.costoEnvio,
            items = detalles.map {
                ItemSolicitud(
                    productoId = it.productoId,
                    cantidad = it.cantidad,
                    precioUnitario = it.precioUnitario
                )
            }
        )

        return ejecutarRed {
            val respuesta = api.crearPedido(solicitud)
            val pedido = respuesta.pedido
                ?: throw IllegalStateException("El servidor no confirmó el pedido.")
            guardarDesdeServidor(pedido)
        }
    }

    private suspend fun guardarDesdeServidor(pedido: PedidoDto) {
        val folio = pedido.folio ?: return
        baseDeDatos.withTransaction {
            pedidoDao.guardarPedido(pedido.aEntidad().copy(pendienteSincronizar = false))
            pedidoDao.borrarDetalles(folio)
            val detalles = pedido.aDetalles()
            if (detalles.isNotEmpty()) {
                pedidoDao.guardarDetalles(detalles)
            }
        }
    }

    private fun ahora(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())

    companion object {
        /** Costo de envío a domicilio dentro de Acámbaro. */
        const val COSTO_ENVIO = 30.0

        /** A partir de este monto el envío sale gratis. */
        const val ENVIO_GRATIS_DESDE = 250.0

        /** Regla del carrito usada por las pantallas. */
        fun costoEnvioPara(subtotal: Double, aDomicilio: Boolean): Double =
            if (aDomicilio && subtotal < ENVIO_GRATIS_DESDE) COSTO_ENVIO else 0.0

        fun totalConEnvio(subtotal: Double, aDomicilio: Boolean): String =
            (subtotal + costoEnvioPara(subtotal, aDomicilio)).enPesos()
    }
}
