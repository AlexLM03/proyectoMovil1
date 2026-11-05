package com.devnoway.posproyect.data.mapeadores

import com.devnoway.posproyect.data.local.entidades.CategoriaEntity
import com.devnoway.posproyect.data.local.entidades.DetallePedidoEntity
import com.devnoway.posproyect.data.local.entidades.DireccionEntity
import com.devnoway.posproyect.data.local.entidades.InsumoEntity
import com.devnoway.posproyect.data.local.entidades.MultimediaEntity
import com.devnoway.posproyect.data.local.entidades.NotificacionEntity
import com.devnoway.posproyect.data.local.entidades.PedidoConLineas
import com.devnoway.posproyect.data.local.entidades.PedidoEntity
import com.devnoway.posproyect.data.local.entidades.ProductoConCategoria
import com.devnoway.posproyect.data.local.entidades.ProductoEntity
import com.devnoway.posproyect.data.local.entidades.TandaEntity
import com.devnoway.posproyect.data.local.entidades.UsuarioEntity
import com.devnoway.posproyect.data.modelo.Categoria
import com.devnoway.posproyect.data.modelo.Direccion
import com.devnoway.posproyect.data.modelo.EstadoPedido
import com.devnoway.posproyect.data.modelo.Insumo
import com.devnoway.posproyect.data.modelo.LineaPedido
import com.devnoway.posproyect.data.modelo.MetodoPago
import com.devnoway.posproyect.data.modelo.Notificacion
import com.devnoway.posproyect.data.modelo.Pedido
import com.devnoway.posproyect.data.modelo.Producto
import com.devnoway.posproyect.data.modelo.TandaHorneado
import com.devnoway.posproyect.data.modelo.Usuario
import com.devnoway.posproyect.data.modelo.fechaLegible
import com.devnoway.posproyect.data.remote.dto.CategoriaDto
import com.devnoway.posproyect.data.remote.dto.DireccionDto
import com.devnoway.posproyect.data.remote.dto.DireccionSolicitud
import com.devnoway.posproyect.data.remote.dto.InsumoDto
import com.devnoway.posproyect.data.remote.dto.MultimediaDto
import com.devnoway.posproyect.data.remote.dto.NotificacionDto
import com.devnoway.posproyect.data.remote.dto.PedidoDto
import com.devnoway.posproyect.data.remote.dto.ProductoDto
import com.devnoway.posproyect.data.remote.dto.TandaDto
import com.devnoway.posproyect.data.remote.dto.UsuarioDto

/**
 * Traducciones entre las tres representaciones de los datos:
 * DTO (JSON del servidor) <-> Entidad (Room) <-> Modelo (pantallas).
 */

fun CategoriaDto.aEntidad() = CategoriaEntity(
    id = id,
    nombre = nombre.orEmpty(),
    iconoEmoji = iconoEmoji ?: "🥐",
    activo = activo
)

fun CategoriaEntity.aModelo() = Categoria(
    id = id,
    nombre = nombre,
    iconoEmoji = iconoEmoji,
    activo = activo
)

fun ProductoDto.aEntidad() = ProductoEntity(
    id = id,
    categoriaId = categoriaId,
    nombre = nombre.orEmpty(),
    descripcion = descripcion.orEmpty(),
    precio = precio,
    emoji = emoji ?: "🥖",
    disponible = disponible
)

fun ProductoConCategoria.aModelo() = Producto(
    id = producto.id,
    categoriaId = producto.categoriaId,
    nombre = producto.nombre,
    descripcion = producto.descripcion,
    precio = producto.precio,
    emoji = producto.emoji,
    disponible = producto.disponible,
    categoria = categoriaNombre.orEmpty()
)

fun TandaDto.aEntidad() = TandaEntity(
    id = id,
    productoId = productoId,
    productoNombre = productoNombre.orEmpty(),
    horaSalida = horaSalida.orEmpty(),
    piezasEstimadas = piezasEstimadas,
    piezasDisponibles = piezasDisponibles,
    estado = estado ?: "EN_HORNO",
    diasSemana = diasSemana ?: "Lunes a Domingo"
)

fun TandaEntity.aModelo() = TandaHorneado(
    id = id,
    productoId = productoId,
    productoNombre = productoNombre,
    horaSalida = horaSalida,
    piezasEstimadas = piezasEstimadas,
    piezasDisponibles = piezasDisponibles,
    estado = estado,
    diasSemana = diasSemana
)

fun UsuarioDto.aModelo() = Usuario(
    id = id,
    nombre = nombre.orEmpty(),
    correo = correo.orEmpty(),
    telefono = telefono.orEmpty(),
    direccion = direccion.orEmpty(),
    rol = rol ?: "CLIENTE",
    metodoPagoPreferido = MetodoPago.desdeCodigo(metodoPagoPreferido),
    activo = activo
)

fun Usuario.aEntidad() = UsuarioEntity(
    id = id,
    nombre = nombre,
    correo = correo,
    telefono = telefono,
    direccion = direccion,
    rol = rol,
    activo = activo
)

fun InsumoDto.aEntidad() = InsumoEntity(
    id = id,
    nombre = nombre.orEmpty(),
    stock = stock,
    minimo = minimo,
    unidad = unidad.orEmpty()
)

fun InsumoEntity.aModelo() = Insumo(
    id = id,
    nombre = nombre,
    stock = stock,
    minimo = minimo,
    unidad = unidad
)

fun Insumo.aSolicitud() = com.devnoway.posproyect.data.remote.dto.InsumoSolicitud(
    nombre = nombre,
    stock = stock,
    minimo = minimo,
    unidad = unidad
)

fun MultimediaDto.aEntidad() = MultimediaEntity(
    id = id,
    productoId = productoId,
    tipo = tipo ?: "imagen",
    url = url.orEmpty(),
    orden = orden
)

fun NotificacionDto.aEntidad() = NotificacionEntity(
    id = id,
    usuarioId = usuarioId,
    pedidoFolio = pedidoFolio,
    titulo = titulo.orEmpty(),
    mensaje = mensaje.orEmpty(),
    leida = leida,
    creadoEn = creadoEn.orEmpty()
)

fun NotificacionEntity.aModelo() = Notificacion(
    id = id,
    usuarioId = usuarioId,
    pedidoFolio = pedidoFolio,
    titulo = titulo,
    mensaje = mensaje,
    leida = leida,
    creadoEn = creadoEn
)

/** Pedido del servidor -> fila local (siempre ya sincronizada). */
fun PedidoDto.aEntidad() = PedidoEntity(
    folio = folio.orEmpty(),
    clienteId = clienteId,
    clienteNombre = clienteNombre.orEmpty(),
    clienteTelefono = clienteTelefono.orEmpty(),
    tipoEntrega = tipoEntrega ?: "A domicilio",
    direccionEntrega = direccionEntrega,
    tipoEmpaque = tipoEmpaque ?: "Bolsa ecológica de papel",
    metodoPago = metodoPago ?: MetodoPago.EFECTIVO.codigo,
    requiereRebanado = requiereRebanado,
    notasPanadero = notasPanadero,
    subtotal = subtotal,
    costoEnvio = costoEnvio,
    total = total,
    estado = estado ?: EstadoPedido.RECIBIDO.codigo,
    repartidorId = repartidorId,
    repartidorNombre = repartidorNombre,
    repartidorTelefono = repartidorTelefono,
    tiempoEstimado = tiempoEstimado ?: "15 min",
    fechaPedido = fechaPedido.orEmpty(),
    pendienteSincronizar = false
)

fun PedidoDto.aDetalles(): List<DetallePedidoEntity> {
    val folioPedido = folio ?: return emptyList()
    return detalles.orEmpty().map { detalle ->
        DetallePedidoEntity(
            pedidoFolio = folioPedido,
            productoId = detalle.productoId,
            productoNombre = detalle.nombre.orEmpty(),
            cantidad = detalle.cantidad,
            precioUnitario = detalle.precioUnitario
        )
    }
}

fun PedidoConLineas.aModelo() = Pedido(
    folio = pedido.folio,
    clienteId = pedido.clienteId,
    cliente = pedido.clienteNombre,
    telefono = pedido.clienteTelefono,
    fecha = pedido.fechaPedido.fechaLegible(),
    subtotal = pedido.subtotal,
    costoEnvio = pedido.costoEnvio,
    total = pedido.total,
    estado = EstadoPedido.desdeCodigo(pedido.estado),
    entrega = pedido.tipoEntrega,
    direccion = pedido.direccionEntrega,
    tipoEmpaque = pedido.tipoEmpaque,
    metodoPago = MetodoPago.desdeCodigo(pedido.metodoPago),
    requiereRebanado = pedido.requiereRebanado,
    notas = pedido.notasPanadero,
    repartidor = pedido.repartidorNombre,
    telefonoRepartidor = pedido.repartidorTelefono,
    tiempoEstimado = pedido.tiempoEstimado,
    pendienteSincronizar = pedido.pendienteSincronizar,
    lineas = lineas.map { detalle ->
        LineaPedido(
            productoId = detalle.productoId,
            nombre = detalle.productoNombre,
            cantidad = detalle.cantidad,
            precioUnitario = detalle.precioUnitario
        )
    }
)

fun DireccionDto.aEntidad() = DireccionEntity(
    id = id,
    usuarioId = usuarioId,
    alias = alias.orEmpty(),
    calle = calle.orEmpty(),
    referencias = referencias,
    predeterminada = predeterminada
)

fun DireccionEntity.aModelo() = Direccion(
    id = id,
    usuarioId = usuarioId,
    alias = alias,
    calle = calle,
    referencias = referencias,
    predeterminada = predeterminada
)

fun Direccion.aSolicitud() = DireccionSolicitud(
    alias = alias,
    calle = calle,
    referencias = referencias,
    predeterminada = predeterminada
)

