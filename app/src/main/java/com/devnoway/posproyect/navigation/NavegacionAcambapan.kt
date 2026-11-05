package com.devnoway.posproyect.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.devnoway.posproyect.ui.screens.AdminDashboardScreen
import com.devnoway.posproyect.ui.screens.AdminPanelScreen
import com.devnoway.posproyect.ui.screens.CarritoScreen
import com.devnoway.posproyect.ui.screens.CatalogoScreen
import com.devnoway.posproyect.ui.screens.DetalleProductoScreen
import com.devnoway.posproyect.ui.screens.GestionPedidosScreen
import com.devnoway.posproyect.ui.screens.InventarioScreen
import com.devnoway.posproyect.ui.screens.LoginScreen
import com.devnoway.posproyect.ui.screens.PerfilScreen
import com.devnoway.posproyect.ui.screens.RegistroScreen
import com.devnoway.posproyect.ui.screens.SeguimientoScreen

/**
 * Navegacion de la app.
 *
 * Aqui se conectan todas las pantallas de la maqueta con Navigation Compose:
 * cada boton llama a una ruta y la ruta muestra la pantalla correspondiente.
 */
@Composable
fun AcambapanApp() {
    val navController = rememberNavController()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(
            navController = navController,
            startDestination = Rutas.LOGIN
        ) {
            // ---------------- Acceso ----------------
            composable(route = Rutas.LOGIN) {
                LoginScreen(
                    onIniciarSesion = { irAlCatalogo(navController) },
                    onCrearCuenta = { navController.navigate(Rutas.REGISTRO) },
                    onInvitado = { irAlCatalogo(navController) },
                    onAccesoAdmin = { navController.navigate(Rutas.ADMIN_PANEL) }
                )
            }
            composable(route = Rutas.REGISTRO) {
                RegistroScreen(
                    onRegistrado = { irAlCatalogo(navController) },
                    onVolverLogin = { navController.popBackStack() }
                )
            }

            // ---------------- Cliente ----------------
            composable(route = Rutas.CATALOGO) {
                CatalogoScreen(
                    onAbrirProducto = { id -> navController.navigate(Rutas.detalleProducto(id)) },
                    onNavegar = { ruta -> navegarCliente(navController, ruta) }
                )
            }
            composable(
                route = Rutas.detalleProductoConArg,
                arguments = listOf(
                    navArgument(Rutas.ARG_PRODUCTO_ID) { type = NavType.IntType }
                )
            ) { entrada ->
                val productoId = entrada.arguments?.getInt(Rutas.ARG_PRODUCTO_ID) ?: 1
                DetalleProductoScreen(
                    productoId = productoId,
                    onAgregarAlCarrito = { navController.navigate(Rutas.CARRITO) },
                    onAtras = { navController.popBackStack() },
                    onNavegar = { ruta -> navegarCliente(navController, ruta) }
                )
            }
            composable(route = Rutas.CARRITO) {
                CarritoScreen(
                    onConfirmarPedido = { navController.navigate(Rutas.SEGUIMIENTO) },
                    onSeguirComprando = { navegarCliente(navController, Rutas.CATALOGO) },
                    onNavegar = { ruta -> navegarCliente(navController, ruta) }
                )
            }
            composable(route = Rutas.SEGUIMIENTO) {
                SeguimientoScreen(
                    onVolverAlCatalogo = { navegarCliente(navController, Rutas.CATALOGO) },
                    onNavegar = { ruta -> navegarCliente(navController, ruta) }
                )
            }
            composable(route = Rutas.PERFIL) {
                PerfilScreen(
                    onMisPedidos = { navegarCliente(navController, Rutas.SEGUIMIENTO) },
                    onModoAdministrador = { navController.navigate(Rutas.ADMIN_PANEL) },
                    onCerrarSesion = { cerrarSesion(navController) },
                    onNavegar = { ruta -> navegarCliente(navController, ruta) }
                )
            }

            // ---------------- Administrador ----------------
            composable(route = Rutas.ADMIN_PANEL) {
                AdminPanelScreen(
                    onNavegar = { ruta -> navegarAdmin(navController, ruta) },
                    onVolverATienda = { irAlCatalogo(navController) },
                    onCerrarSesion = { cerrarSesion(navController) }
                )
            }
            composable(route = Rutas.ADMIN_DASH) {
                AdminDashboardScreen(onNavegar = { ruta -> navegarAdmin(navController, ruta) })
            }
            composable(route = Rutas.ADMIN_PEDIDOS) {
                GestionPedidosScreen(
                    onVerSeguimiento = { navegarCliente(navController, Rutas.SEGUIMIENTO) },
                    onNavegar = { ruta -> navegarAdmin(navController, ruta) }
                )
            }
            composable(route = Rutas.ADMIN_INVENTARIO) {
                InventarioScreen(onNavegar = { ruta -> navegarAdmin(navController, ruta) })
            }
        }
    }
}

/**
 * Entra al catalogo limpiando la pila: se usa al iniciar sesion, al entrar
 * como invitado, al terminar el registro y al volver a la tienda desde el
 * panel administrativo.
 */
private fun irAlCatalogo(navController: NavHostController) {
    navController.navigate(Rutas.CATALOGO) {
        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
        launchSingleTop = true
    }
}

/** Mueve entre las pantallas del cliente sin acumular copias en la pila. */
private fun navegarCliente(navController: NavHostController, ruta: String) {
    navController.navigate(ruta) {
        popUpTo(Rutas.CATALOGO)
        launchSingleTop = true
    }
}

/** Mueve entre las pantallas del administrador. */
private fun navegarAdmin(navController: NavHostController, ruta: String) {
    navController.navigate(ruta) {
        popUpTo(Rutas.ADMIN_PANEL)
        launchSingleTop = true
    }
}

/** Regresa a la pantalla de inicio de sesion. */
private fun cerrarSesion(navController: NavHostController) {
    navController.navigate(Rutas.LOGIN) {
        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
        launchSingleTop = true
    }
}
