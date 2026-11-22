package com.devnoway.posproyect.data.remote.dto

/**
 * DTOs de la Web API.
 *
 * Los nombres coinciden con el JSON que entrega la API PHP
 * (`C:\xampp\virtualhost\panaderia\api`). Todo lo opcional se declara nullable
 * para que un campo ausente no rompa el parseo de Gson.
 */

data class ErrorDto(val codigo: String? = null, val mensaje: String? = null)

data class ErrorRespuestaDto(val ok: Boolean = false, val error: ErrorDto? = null)

data class PingRespuestaDto(
    val ok: Boolean = false,
    val version: String? = null,
    val hora: String? = null,
    val db: String? = null
)

data class UsuarioDto(
    val id: Int = 0,
    val nombre: String? = null,
    val correo: String? = null,
    val telefono: String? = null,
    val direccion: String? = null,
    val rol: String? = null,
    val metodoPagoPreferido: String? = null,
    val activo: Boolean = true
)

data class SesionRespuestaDto(
    val ok: Boolean = false,
    val token: String? = null,
    val usuario: UsuarioDto? = null,
    val error: ErrorDto? = null
)

data class CategoriaDto(
    val id: Int = 0,
    val nombre: String? = null,
    val iconoEmoji: String? = null,
    val activo: Boolean = true
)

data class CategoriasRespuestaDto(
    val ok: Boolean = false,
    val categorias: List<CategoriaDto>? = null,
    val error: ErrorDto? = null
)

data class ProductoDto(
    val id: Int = 0,
    val categoriaId: Int = 0,
    val categoriaNombre: String? = null,
    val nombre: String? = null,
    val descripcion: String? = null,
    val precio: Double = 0.0,
    val emoji: String? = null,
    val disponible: Boolean = true
)

data class ProductosRespuestaDto(
    val ok: Boolean = false,
    val productos: List<ProductoDto>? = null,
    val error: ErrorDto? = null
)

data class MultimediaDto(
    val id: Int = 0,
    val productoId: Int = 0,
    val tipo: String? = null,
    val url: String? = null,
    val orden: Int = 1
)

data class TandaDto(
    val id: Int = 0,
    val productoId: Int = 0,
    val productoNombre: String? = null,
    val horaSalida: String? = null,
    val piezasEstimadas: Int = 0,
    val piezasDisponibles: Int = 0,
    val estado: String? = null,
    val diasSemana: String? = null
)

data class TandasRespuestaDto(
    val ok: Boolean = false,
    val tandas: List<TandaDto>? = null,
    val error: ErrorDto? = null
)

data class ProductoRespuestaDto(
    val ok: Boolean = false,
    val producto: ProductoDto? = null,
    val multimedia: List<MultimediaDto>? = null,
    val tandas: List<TandaDto>? = null,
    val error: ErrorDto? = null
)

data class DetalleDto(
    val productoId: Int = 0,
    val nombre: String? = null,
    val cantidad: Int = 0,
    val precioUnitario: Double = 0.0,
    val subtotal: Double = 0.0
)

data class PedidoDto(
    val folio: String? = null,
    val clienteId: Int? = null,
    val clienteNombre: String? = null,
    val clienteTelefono: String? = null,
    val tipoEntrega: String? = null,
    val direccionEntrega: String? = null,
    val horaRecoleccion: String? = null,
    val tipoEmpaque: String? = null,
    val metodoPago: String? = null,
    val requiereRebanado: Boolean = false,
    val notasPanadero: String? = null,
    val subtotal: Double = 0.0,
    val costoEnvio: Double = 0.0,
    val total: Double = 0.0,
    val estado: String? = null,
    val repartidorId: Int? = null,
    val repartidorNombre: String? = null,
    val repartidorTelefono: String? = null,
    val tiempoEstimado: String? = null,
    val fechaPedido: String? = null,
    val pendienteSync: Boolean = false,
    val detalles: List<DetalleDto>? = null
)

data class PedidosRespuestaDto(
    val ok: Boolean = false,
    val pedidos: List<PedidoDto>? = null,
    val error: ErrorDto? = null
)

data class PedidoRespuestaDto(
    val ok: Boolean = false,
    val pedido: PedidoDto? = null,
    val error: ErrorDto? = null
)

data class InsumoDto(
    val id: Int = 0,
    val nombre: String? = null,
    val stock: Double = 0.0,
    val minimo: Double = 0.0,
    val unidad: String? = null
)

data class InsumosRespuestaDto(
    val ok: Boolean = false,
    val insumos: List<InsumoDto>? = null,
    val error: ErrorDto? = null
)

data class InsumoRespuestaDto(
    val ok: Boolean = false,
    val insumo: InsumoDto? = null,
    val error: ErrorDto? = null
)

data class ResumenDto(
    val pedidosHoy: Int = 0,
    val ventasHoy: Double = 0.0,
    val ticketPromedio: Double = 0.0,
    val insumosBajos: Int = 0,
    val pedidosRecientes: List<PedidoDto>? = null
)

data class ResumenRespuestaDto(
    val ok: Boolean = false,
    val resumen: ResumenDto? = null,
    val error: ErrorDto? = null
)

data class NotificacionDto(
    val id: Int = 0,
    val usuarioId: Int? = null,
    val pedidoFolio: String? = null,
    val titulo: String? = null,
    val mensaje: String? = null,
    val leida: Boolean = false,
    val creadoEn: String? = null
)

data class NotificacionesRespuestaDto(
    val ok: Boolean = false,
    val notificaciones: List<NotificacionDto>? = null,
    val error: ErrorDto? = null
)

data class OkRespuestaDto(
    val ok: Boolean = false,
    val error: ErrorDto? = null
)

// ------------------------- Cuerpos de petición -------------------------

data class LoginSolicitud(val correo: String, val password: String)

data class RegistroSolicitud(
    val nombre: String,
    val correo: String,
    val password: String,
    val telefono: String,
    val direccion: String
)

data class ItemSolicitud(
    val productoId: Int,
    val cantidad: Int,
    val precioUnitario: Double
)

data class CrearPedidoSolicitud(
    val folio: String,
    val clienteId: Int?,
    val clienteNombre: String,
    val clienteTelefono: String,
    val tipoEntrega: String,
    val direccionEntrega: String?,
    val tipoEmpaque: String,
    val metodoPago: String,
    val requiereRebanado: Boolean,
    val notasPanadero: String?,
    val costoEnvio: Double,
    val items: List<ItemSolicitud>
)

data class EstadoSolicitud(
    val estado: String,
    val repartidorId: Int? = null,
    val tiempoEstimado: String? = null
)

data class InsumoSolicitud(
    val nombre: String? = null,
    val stock: Double? = null,
    val minimo: Double? = null,
    val unidad: String? = null
)

data class DireccionDto(
    val id: Int = 0,
    val usuarioId: Int = 0,
    val alias: String? = null,
    val calle: String? = null,
    val referencias: String? = null,
    val predeterminada: Boolean = false
)

data class DireccionesRespuestaDto(
    val ok: Boolean = false,
    val direcciones: List<DireccionDto>? = null,
    val error: ErrorDto? = null
)

data class DireccionRespuestaDto(
    val ok: Boolean = false,
    val direccion: DireccionDto? = null,
    val error: ErrorDto? = null
)

data class DireccionSolicitud(
    val alias: String? = null,
    val calle: String? = null,
    val referencias: String? = null,
    val predeterminada: Boolean? = null
)

data class MetodoPagoSolicitud(val metodo: String)
