package com.devnoway.posproyect.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devnoway.posproyect.di.ContenedorApp
import com.devnoway.posproyect.navigation.Rutas
import com.devnoway.posproyect.ui.components.BandaMensaje
import com.devnoway.posproyect.ui.components.BotonSecundario
import com.devnoway.posproyect.ui.components.FilaDato
import com.devnoway.posproyect.ui.components.FilaNavegacion
import com.devnoway.posproyect.ui.components.LogoAcambapan
import com.devnoway.posproyect.ui.components.MarcoCliente
import com.devnoway.posproyect.ui.components.TituloSeccion
import com.devnoway.posproyect.ui.viewmodel.PerfilViewModel

/**
 * Perfil del cliente: datos, historial y ajustes.
 *
 * El usuario sale de la sesión guardada (funciona sin conexión). Las opciones
 * que todavía no tienen pantalla siguen mostrando el aviso de pendiente.
 */
@Composable
fun PerfilScreen(
    contenedor: ContenedorApp,
    onMisPedidos: () -> Unit,
    onDirecciones: () -> Unit,
    onMetodosPago: () -> Unit,
    onNotificaciones: () -> Unit,
    onAyuda: () -> Unit,
    onModoAdministrador: () -> Unit,
    onCerrarSesion: () -> Unit,
    onNavegar: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PerfilViewModel = viewModel(factory = PerfilViewModel.fabrica(contenedor))
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val usuario = estado.usuario

    MarcoCliente(
        titulo = "Mi perfil",
        rutaActual = Rutas.PERFIL,
        onNavegar = onNavegar,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LogoAcambapan(tamano = 64.dp)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = usuario?.nombre ?: "Invitado",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when {
                            usuario == null -> "Sin sesión: navegando como invitado"
                            usuario.esAdmin -> "Administrador de la panadería"
                            else -> "Cliente registrado"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (usuario != null) {
                TituloSeccion("Mis datos")
                Column {
                    FilaDato("Correo", usuario.correo)
                    FilaDato("Teléfono", usuario.telefono)
                    FilaDato("Dirección", usuario.direccion)
                }
            }

            estado.mensaje?.let { mensaje ->
                BandaMensaje(
                    mensaje = mensaje,
                    esError = true,
                    onCerrar = viewModel::limpiarMensaje
                )
            }

            Spacer(Modifier.height(4.dp))
            TituloSeccion("Ajustes")
            FilaNavegacion(
                emoji = "\uD83E\uDDFE",
                titulo = "Mis pedidos",
                subtitulo = "${estado.pedidos.size} pedido(s) guardados en este teléfono",
                onClick = onMisPedidos
            )
            FilaNavegacion(
                emoji = "\uD83D\uDCCD",
                titulo = "Direcciones de entrega",
                subtitulo = "Agregar o cambiar direcciones",
                onClick = onDirecciones
            )
            FilaNavegacion(
                emoji = "\uD83D\uDCB3",
                titulo = "Métodos de pago",
                subtitulo = "Efectivo, terminal o transferencia",
                onClick = onMetodosPago
            )
            FilaNavegacion(
                emoji = "\uD83D\uDD14",
                titulo = "Notificaciones",
                subtitulo = "${estado.noLeidas} sin leer · avisos del pedido",
                onClick = onNotificaciones
            )
            FilaNavegacion(
                emoji = "\uD83D\uDD04",
                titulo = "Sincronizar ahora",
                subtitulo = if (estado.enLinea) {
                    "Traer pedidos y avisos del servidor"
                } else {
                    "Sin conexión: se usan los datos guardados"
                },
                onClick = viewModel::sincronizar
            )
            FilaNavegacion(
                emoji = "\u2699",
                titulo = "Modo administrador",
                subtitulo = "Panel de la panadería",
                onClick = onModoAdministrador
            )
            FilaNavegacion(
                emoji = "\u2753",
                titulo = "Ayuda y soporte",
                subtitulo = "Preguntas frecuentes y contacto",
                onClick = onAyuda
            )

            Spacer(Modifier.height(8.dp))
            BotonSecundario(
                texto = if (usuario == null) "Iniciar sesión" else "Cerrar sesión",
                onClick = {
                    if (usuario == null) {
                        onCerrarSesion()
                    } else {
                        viewModel.cerrarSesion()
                        onCerrarSesion()
                    }
                }
            )
            Text(
                text = "Acambapan · versión 1.0 · Room + Retrofit",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
