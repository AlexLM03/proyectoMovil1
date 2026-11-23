package com.devnoway.posproyect.navigation

/**
 * Rutas de navegacion de la app.
 *
 * Se usan con Navigation Compose (NavHost + composable(route = ...)).
 * Ninguna pantalla tiene logica todavia: solo se mueven entre ellas.
 */
object Rutas {
    const val LOGIN = "login"

    /** Variante del login que no entra sola con la sesión guardada (candado admin). */
    const val LOGIN_ADMIN = "login_admin"
    const val REGISTRO = "registro"

    // --- Flujo del cliente ---
    const val CATALOGO = "catalogo"
    const val CARRITO = "carrito"
    const val SEGUIMIENTO = "seguimiento"
    const val PERFIL = "perfil"
    const val DIRECCIONES = "direcciones"
    const val METODOS_PAGO = "metodos_pago"
    const val NOTIFICACIONES = "notificaciones"
    const val AYUDA = "ayuda"

    // --- Flujo del administrador ---
    const val ADMIN_PANEL = "admin_panel"
    const val ADMIN_DASH = "admin_dash"
    const val ADMIN_PEDIDOS = "admin_pedidos"
    const val ADMIN_INVENTARIO = "admin_inventario"

    // --- Detalle de producto: recibe el id como argumento ---
    const val ARG_PRODUCTO_ID = "productoId"
    const val DETALLE_PRODUCTO = "detalle_producto"
    fun detalleProducto(id: Int) = "$DETALLE_PRODUCTO/$id"
    val detalleProductoConArg = "$DETALLE_PRODUCTO/{$ARG_PRODUCTO_ID}"
}
