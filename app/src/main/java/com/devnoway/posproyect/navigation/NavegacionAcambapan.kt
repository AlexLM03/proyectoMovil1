package com.devnoway.posproyect.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.devnoway.posproyect.di.ContenedorApp
import com.devnoway.posproyect.ui.screens.AccesoRestringidoScreen
import com.devnoway.posproyect.ui.screens.AdminDashboardScreen
import com.devnoway.posproyect.ui.screens.AdminPanelScreen
import com.devnoway.posproyect.ui.screens.AyudaScreen
import com.devnoway.posproyect.ui.screens.CarritoScreen
import com.devnoway.posproyect.ui.screens.CatalogoScreen
import com.devnoway.posproyect.ui.screens.DetalleProductoScreen
import com.devnoway.posproyect.ui.screens.DireccionesScreen
import com.devnoway.posproyect.ui.screens.GestionPedidosScreen
import com.devnoway.posproyect.ui.screens.InventarioScreen
import com.devnoway.posproyect.ui.screens.LoginScreen
import com.devnoway.posproyect.ui.screens.MetodosPagoScreen
import com.devnoway.posproyect.ui.screens.NotificacionesScreen
import com.devnoway.posproyect.ui.screens.PerfilScreen
import com.devnoway.posproyect.ui.screens.RegistroScreen
import com.devnoway.posproyect.ui.screens.SeguimientoScreen

/**
 * Navegación de la app (NavHost).
 *
 * El contenedor de dependencias llega desde la Application a través de la
 * Activity; cada pantalla crea su ViewModel con ese contenedor. Las rutas de
 * administrador están protegidas: sin sesión con rol ADMIN muestran el candado.
 */
@Composable
fun AcambapanApp(contenedor: ContenedorApp) {
    val navController = rememberNavController()
    val sesion by contenedor.sesionRepository.sesion.collectAsStateWithLifecycle()
    val esAdmin = sesion?.esAdmin == true

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
                    contenedor = contenedor,
                    onEntrarCliente = { irAlCatalogo(navController) },
                    onEntrarAdmin = { irAlPanelAdmin(navController) },
                    onCrearCuenta = { navController.navigate(Rutas.REGISTRO) },
                    onInvitado = { irAlCatalogo(navController) }
                )
            }
            composable(route = Rutas.LOGIN_ADMIN) {
                LoginScreen(
                    contenedor = contenedor,
                    onEntrarCliente = { irAlCatalogo(navController) },
                    onEntrarAdmin = { irAlPanelAdmin(navController) },
                    onCrearCuenta = { navController.navigate(Rutas.REGISTRO) },
                    onInvitado = { irAlCatalogo(navController) },
                    entrarAutomatico = false
                )
            }
            composable(route = Rutas.REGISTRO) {
                RegistroScreen(
                    contenedor = contenedor,
                    onRegistrado = { irAlCatalogo(navController) },
                    onVolverLogin = { navController.popBackStack() }
                )
            }

            // ---------------- Cliente ----------------
            composable(route = Rutas.CATALOGO) {
                CatalogoScreen(
                    contenedor = contenedor,
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
                    contenedor = contenedor,
                    productoId = productoId,
                    onAgregarAlCarrito = { navController.navigate(Rutas.CARRITO) },
                    onAtras = { navController.popBackStack() },
                    onNavegar = { ruta -> navegarCliente(navController, ruta) }
                )
            }
            composable(route = Rutas.CARRITO) {
                CarritoScreen(
                    contenedor = contenedor,
                    onConfirmarPedido = { navController.navigate(Rutas.SEGUIMIENTO) },
                    onSeguirComprando = { navegarCliente(navController, Rutas.CATALOGO) },
                    onNavegar = { ruta -> navegarCliente(navController, ruta) }
                )
            }
            composable(route = Rutas.SEGUIMIENTO) {
                SeguimientoScreen(
                    contenedor = contenedor,
                    onVolverAlCatalogo = { navegarCliente(navController, Rutas.CATALOGO) },
                    onNavegar = { ruta -> navegarCliente(navController, ruta) }
                )
            }
            composable(route = Rutas.PERFIL) {
                PerfilScreen(
                    contenedor = contenedor,
                    onMisPedidos = { navegarCliente(navController, Rutas.SEGUIMIENTO) },
                    onDirecciones = { navController.navigate(Rutas.DIRECCIONES) },
                    onMetodosPago = { navController.navigate(Rutas.METODOS_PAGO) },
                    onNotificaciones = { navController.navigate(Rutas.NOTIFICACIONES) },
                    onAyuda = { navController.navigate(Rutas.AYUDA) },
                    onModoAdministrador = { navController.navigate(Rutas.ADMIN_PANEL) },
                    onCerrarSesion = { cerrarSesion(navController, contenedor) },
                    onNavegar = { ruta -> navegarCliente(navController, ruta) }
                )
            }
            composable(route = Rutas.DIRECCIONES) {
                DireccionesScreen(
                    contenedor = contenedor,
                    onAtras = { navController.popBackStack() },
                    onNavegar = { ruta -> navegarCliente(navController, ruta) }
                )
            }
            composable(route = Rutas.METODOS_PAGO) {
                MetodosPagoScreen(
                    contenedor = contenedor,
                    onAtras = { navController.popBackStack() },
                    onNavegar = { ruta -> navegarCliente(navController, ruta) }
                )
            }
            composable(route = Rutas.NOTIFICACIONES) {
                NotificacionesScreen(
                    contenedor = contenedor,
                    onAtras = { navController.popBackStack() },
                    onNavegar = { ruta -> navegarCliente(navController, ruta) }
                )
            }
            composable(route = Rutas.AYUDA) {
                AyudaScreen(
                    onAtras = { navController.popBackStack() },
                    onNavegar = { ruta -> navegarCliente(navController, ruta) }
                )
            }

            // ---------------- Administrador (protegido) ----------------
            composable(route = Rutas.ADMIN_PANEL) {
                if (!esAdmin) {
                    AccesoRestringidoScreen(
                        onIrLogin = { navController.navigate(Rutas.LOGIN_ADMIN) },
                        onVolver = { irAlCatalogo(navController) }
                    )
                } else {
                    AdminPanelScreen(
                        contenedor = contenedor,
                        onNavegar = { ruta -> navegarAdmin(navController, ruta) },
                        onVolverATienda = { irAlCatalogo(navController) },
                        onCerrarSesion = { cerrarSesion(navController, contenedor) }
                    )
                }
            }
            composable(route = Rutas.ADMIN_DASH) {
                if (!esAdmin) {
                    AccesoRestringidoScreen(
                        onIrLogin = { navController.navigate(Rutas.LOGIN_ADMIN) },
                        onVolver = { irAlCatalogo(navController) }
                    )
                } else {
                    AdminDashboardScreen(
                        contenedor = contenedor,
                        onNavegar = { ruta -> navegarAdmin(navController, ruta) }
                    )
                }
            }
            composable(route = Rutas.ADMIN_PEDIDOS) {
                if (!esAdmin) {
                    AccesoRestringidoScreen(
                        onIrLogin = { navController.navigate(Rutas.LOGIN_ADMIN) },
                        onVolver = { irAlCatalogo(navController) }
                    )
                } else {
                    GestionPedidosScreen(
                        contenedor = contenedor,
                        onVerSeguimiento = { navegarCliente(navController, Rutas.SEGUIMIENTO) },
                        onNavegar = { ruta -> navegarAdmin(navController, ruta) }
                    )
                }
            }
            composable(route = Rutas.ADMIN_INVENTARIO) {
                if (!esAdmin) {
                    AccesoRestringidoScreen(
                        onIrLogin = { navController.navigate(Rutas.LOGIN_ADMIN) },
                        onVolver = { irAlCatalogo(navController) }
                    )
                } else {
                    InventarioScreen(
                        contenedor = contenedor,
                        onNavegar = { ruta -> navegarAdmin(navController, ruta) }
                    )
                }
            }
        }
    }
}

/**
 * Entra al catálogo limpiando la pila: se usa al iniciar sesión, al entrar
 * como invitado, al terminar el registro y al volver a la tienda desde el
 * panel administrativo.
 */
private fun irAlCatalogo(navController: NavHostController) {
    navController.navigate(Rutas.CATALOGO) {
        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
        launchSingleTop = true
    }
}

/** Entra al panel de administrador limpiando la pila de acceso. */
private fun irAlPanelAdmin(navController: NavHostController) {
    navController.navigate(Rutas.ADMIN_PANEL) {
        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
        launchSingleTop = true
    }
}

/** Mueve entre las pantallas del cliente sin acumular copias en la pila. */
private fun navegarCliente(navController: NavHostController, ruta: String) {
    navController.navigate(ruta) {
        popUpTo(Rutas.CATALOGO) { inclusive = ruta == Rutas.CATALOGO }
        launchSingleTop = true
    }
}

/** Mueve entre las pantallas del administrador. */
private fun navegarAdmin(navController: NavHostController, ruta: String) {
    navController.navigate(ruta) {
        popUpTo(Rutas.ADMIN_PANEL) { inclusive = ruta == Rutas.ADMIN_PANEL }
        launchSingleTop = true
    }
}

/** Cierra la sesión y regresa al inicio de sesión. */
private fun cerrarSesion(navController: NavHostController, contenedor: ContenedorApp) {
    contenedor.sesionRepository.cerrarSesion()
    navController.navigate(Rutas.LOGIN) {
        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
        launchSingleTop = true
    }
}
