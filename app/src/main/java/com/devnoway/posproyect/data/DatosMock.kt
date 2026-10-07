package com.devnoway.posproyect.data

import java.text.NumberFormat
import java.util.Locale

/**
 * Datos de ejemplo (en memoria) para la maqueta de Acambapan.
 *
 * Todavia no hay base de datos ni API: todo lo que se ve en las pantallas
 * sale de este archivo. Cuando se conecte la API de panaderia.devnoway.com
 * estas listas se reemplazan por la respuesta del servidor.
 */

/** Formatea un precio en pesos: 18.0 -> $18.00 */
fun Double.enPesos(): String =
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-MX")).format(this)

/** Un producto del catalogo. */
data class Producto(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val categoria: String,
    val emoji: String,
    val disponible: Boolean = true
)

/** Estados por los que pasa un pedido. */
enum class EstadoPedido(val etiqueta: String) {
    RECIBIDO("Recibido"),
    EN_PREPARACION("En preparación"),
    EN_CAMINO("En camino"),
    ENTREGADO("Entregado")
}

/** Un pedido del historial / panel administrativo. */
data class Pedido(
    val folio: String,
    val cliente: String,
    val fecha: String,
    val total: Double,
    val estado: EstadoPedido,
    val entrega: String = "A domicilio"
)

/** Un insumo del inventario de la panaderia. */
data class Insumo(
    val nombre: String,
    val stock: Int,
    val minimo: Int,
    val unidad: String
) {
    val bajo: Boolean get() = stock <= minimo
}

/** Una linea del carrito de compras. */
data class LineaCarrito(val producto: Producto, val cantidad: Int) {
    val subtotal: Double get() = producto.precio * cantidad
}

/** Datos del usuario que "inicio sesion" en la maqueta. */
data class UsuarioDemo(
    val nombre: String,
    val correo: String,
    val telefono: String,
    val direccion: String
)

object DatosMock {

    val categorias = listOf("Todos", "Pan dulce", "Pan salado", "Pasteles", "Bebidas")

    val productos = listOf(
        Producto(1, "Concha de vainilla", "Costra de azúcar y vainilla", 18.0, "Pan dulce", "🥯"),
        Producto(2, "Concha de chocolate", "Costra de chocolate amargo", 18.0, "Pan dulce", "🍫"),
        Producto(3, "Cuernito de mantequilla", "Hojaldre recién horneado", 16.0, "Pan dulce", "🥐"),
        Producto(4, "Orejas de hojaldre", "Hojaldre con azúcar y canela", 17.0, "Pan dulce", "🥨"),
        Producto(5, "Bolillo", "Pan blanco de sal, horneado cada hora", 6.0, "Pan salado", "🥖"),
        Producto(6, "Telera", "Pan de sal para tortas y molletes", 12.0, "Pan salado", "🫓"),
        Producto(7, "Pan de muerto", "Temporada: naranja y ajonjolí", 45.0, "Pan dulce", "🍞"),
        Producto(8, "Rebanada de pastel de chocolate", "Tres capas con ganache", 55.0, "Pasteles", "🍰"),
        Producto(9, "Cheesecake de fresa", "Base de galleta y fresa natural", 60.0, "Pasteles", "🍓"),
        Producto(10, "Café de olla", "Con canela y piloncillo", 25.0, "Bebidas", "☕"),
        Producto(11, "Chocolate caliente", "Chocolate de mesa con leche", 30.0, "Bebidas", "🍵"),
        Producto(12, "Dona glaseada", "Glaseado de azúcar clásico", 20.0, "Pan dulce", "🍩")
    )

    /** Carrito de ejemplo, como si el cliente ya hubiera agregado cosas. */
    val carrito = listOf(
        LineaCarrito(productos[0], 2),
        LineaCarrito(productos[4], 4),
        LineaCarrito(productos[9], 1)
    )

    val pedidos = listOf(
        Pedido("ACB-1042", "Janetzy Maldonado", "Hoy 10:24", 145.0, EstadoPedido.EN_CAMINO),
        Pedido("ACB-1043", "María Nava", "Hoy 11:02", 210.0, EstadoPedido.RECIBIDO),
        Pedido("ACB-1041", "Alexander López", "Hoy 09:58", 78.0, EstadoPedido.EN_PREPARACION, "Para recoger"),
        Pedido("ACB-1040", "Ambar Fujarte", "Hoy 09:12", 320.0, EstadoPedido.ENTREGADO),
        Pedido("ACB-1039", "Cliente invitado", "Ayer 18:40", 96.0, EstadoPedido.ENTREGADO, "Para recoger")
    )

    val inventario = listOf(
        Insumo("Harina de trigo", 40, 10, "sacos"),
        Insumo("Azúcar estándar", 25, 8, "kg"),
        Insumo("Mantequilla", 6, 6, "kg"),
        Insumo("Levadura fresca", 3, 4, "kg"),
        Insumo("Huevo", 180, 60, "piezas"),
        Insumo("Leche entera", 30, 10, "litros"),
        Insumo("Chocolate amargo", 8, 5, "kg"),
        Insumo("Café molido", 12, 4, "kg")
    )

    val usuario = UsuarioDemo(
        nombre = "Janetzy Maldonado Nava",
        correo = "s23120227@alumnos.itsur.edu.mx",
        telefono = "445 217 3556",
        direccion = "Av. Juárez 165, Acámbaro, Guanajuato"
    )

    /** Pedido que se esta siguiendo en la pantalla de seguimiento. */
    val pedidoEnCurso = pedidos.first()

    val repartidor = "Luis Martínez"
    val telefonoRepartidor = "55 8765 4321"
    val tiempoEstimado = "12 min"

    // --- Numeros del dash administrativo ---
    val ventasDelDia = 4830.50
    val pedidosDelDia = 27
    val ticketPromedio = 178.90
    val productosBajosDeStock = inventario.count { it.bajo }
}
