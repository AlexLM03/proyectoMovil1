package com.devnoway.posproyect.data.modelo

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Modelos de dominio de Acambapan.
 *
 * Son las clases que consumen las pantallas y los ViewModel. No saben nada de
 * Room ni de Retrofit: las capas de datos traducen entidades y DTOs a estos
 * modelos, de modo que la interfaz no depende de la fuente (local o remota).
 */

/** Formatea un precio en pesos: 18.0 -> $18.00 */
fun Double.enPesos(): String =
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-MX")).format(this)

/** Formatea cantidades de inventario sin decimales innecesarios: 40.0 -> 40 */
fun Double.enCantidad(): String =
    if (this % 1.0 == 0.0) toInt().toString() else String.format(Locale.US, "%.2f", this)

/** Día de hoy en el formato de MySQL: 2026-10-27. */
fun hoyIso(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(java.util.Date())

/** true si la fecha del pedido ("2026-10-27 10:24:00") es de hoy. */
fun String.esDeHoy(): Boolean = startsWith(hoyIso())

/** Convierte "2026-10-27 10:24:00" en "27/10/2026 10:24". */
fun String.fechaLegible(): String {
    val original = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    val destino = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-MX"))
    return runCatching { destino.format(original.parse(this)!!) }.getOrDefault(this)
}

/** Formas de pago aceptadas por la panadería. */
enum class MetodoPago(val etiqueta: String, val codigo: String, val descripcion: String) {
    EFECTIVO("Efectivo", "EFECTIVO", "Pagas al recibir tu pedido"),
    TARJETA_TERMINAL("Tarjeta", "TARJETA_TERMINAL", "El repartidor lleva terminal bancaria"),
    TRANSFERENCIA("Transferencia", "TRANSFERENCIA", "SPEI; la panadería comparte los datos");

    companion object {
        fun desdeCodigo(codigo: String?): MetodoPago =
            entries.firstOrNull { it.codigo == codigo } ?: EFECTIVO
    }
}

/** Dirección de entrega guardada por el cliente. */
data class Direccion(
    val id: Int = 0,
    val usuarioId: Int = 0,
    val alias: String,
    val calle: String,
    val referencias: String? = null,
    val predeterminada: Boolean = false
)

/** Categoría del catálogo (tabla `categorias`). */
data class Categoria(
    val id: Int,
    val nombre: String,
    val iconoEmoji: String = "🥐",
    val activo: Boolean = true
)

/** Producto del catálogo (tabla `productos`). */
data class Producto(
    val id: Int,
    val categoriaId: Int,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val emoji: String,
    val disponible: Boolean = true,
    val categoria: String = ""
)

/** Estados por los que pasa un pedido (ENUM de la base de datos). */
enum class EstadoPedido(val etiqueta: String, val codigo: String) {
    RECIBIDO("Recibido", "RECIBIDO"),
    EN_PREPARACION("En preparación", "EN_PREPARACION"),
    EN_CAMINO("En camino", "EN_CAMINO"),
    ENTREGADO("Entregado", "ENTREGADO"),
    CANCELADO("Cancelado", "CANCELADO");

    val esFinal: Boolean get() = this == ENTREGADO || this == CANCELADO

    companion object {
        /** Estados que forman la línea de tiempo del cliente. */
        val flujo: List<EstadoPedido> = listOf(RECIBIDO, EN_PREPARACION, EN_CAMINO, ENTREGADO)

        fun desdeCodigo(codigo: String?): EstadoPedido =
            entries.firstOrNull { it.codigo == codigo } ?: RECIBIDO
    }
}

/** Una línea (detalle) de un pedido. */
data class LineaPedido(
    val productoId: Int,
    val nombre: String,
    val cantidad: Int,
    val precioUnitario: Double
) {
    val subtotal: Double get() = cantidad * precioUnitario
}

/** Un pedido completo, con sus líneas. */
data class Pedido(
    val folio: String,
    val clienteId: Int? = null,
    val cliente: String = "",
    val telefono: String = "",
    /** Fecha lista para mostrar: 27/10/2026 10:24. */
    val fecha: String = "",
    val subtotal: Double = 0.0,
    val costoEnvio: Double = 0.0,
    val total: Double = 0.0,
    val estado: EstadoPedido = EstadoPedido.RECIBIDO,
    val entrega: String = "A domicilio",
    val direccion: String? = null,
    val tipoEmpaque: String = "Bolsa ecológica de papel",
    val metodoPago: MetodoPago = MetodoPago.EFECTIVO,
    val requiereRebanado: Boolean = false,
    val notas: String? = null,
    val repartidor: String? = null,
    val telefonoRepartidor: String? = null,
    val tiempoEstimado: String = "15 min",
    val pendienteSincronizar: Boolean = false,
    val lineas: List<LineaPedido> = emptyList()
) {
    val piezas: Int get() = lineas.sumOf { it.cantidad }
}

/** Insumo del inventario (tabla `insumos`). */
data class Insumo(
    val id: Int = 0,
    val nombre: String,
    val stock: Double,
    val minimo: Double,
    val unidad: String
) {
    val bajo: Boolean get() = stock <= minimo
}

/** Tanda de horneado programada (tabla `tandas_horneado`). */
data class TandaHorneado(
    val id: Int,
    val productoId: Int,
    val productoNombre: String,
    val horaSalida: String,
    val piezasEstimadas: Int,
    val piezasDisponibles: Int,
    val estado: String,
    val diasSemana: String = "Lunes a Domingo"
)

/** Aviso para el cliente (tabla `notificaciones`). */
data class Notificacion(
    val id: Int,
    val usuarioId: Int?,
    val pedidoFolio: String?,
    val titulo: String,
    val mensaje: String,
    val leida: Boolean,
    val creadoEn: String
)

/** Una línea del carrito de compras (vive en memoria mientras se arma el pedido). */
data class LineaCarrito(val producto: Producto, val cantidad: Int) {
    val subtotal: Double get() = producto.precio * cantidad
}

/** Usuario de la app. `rol` viene del ENUM: CLIENTE, ADMIN o REPARTIDOR. */
data class Usuario(
    val id: Int,
    val nombre: String,
    val correo: String,
    val telefono: String = "",
    val direccion: String = "",
    val rol: String = "CLIENTE",
    val metodoPagoPreferido: MetodoPago = MetodoPago.EFECTIVO,
    val activo: Boolean = true
) {
    val esAdmin: Boolean get() = rol == "ADMIN"
}

/** Números del dash administrativo, calculados con los datos locales. */
data class ResumenNegocio(
    val pedidosHoy: Int = 0,
    val ventasHoy: Double = 0.0,
    val ticketPromedio: Double = 0.0,
    val insumosBajos: Int = 0
)

/** Datos que se capturan al confirmar el carrito. */
data class NuevoPedido(
    val clienteId: Int?,
    val clienteNombre: String,
    val clienteTelefono: String,
    val aDomicilio: Boolean,
    val direccion: String?,
    val tipoEmpaque: String = "Bolsa ecológica de papel",
    val metodoPago: MetodoPago = MetodoPago.EFECTIVO,
    val requiereRebanado: Boolean = false,
    val notas: String? = null,
    val lineas: List<LineaCarrito>
)
