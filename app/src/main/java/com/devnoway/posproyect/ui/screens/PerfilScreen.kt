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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devnoway.posproyect.R
import com.devnoway.posproyect.di.ContenedorApp
import com.devnoway.posproyect.navigation.Rutas
import com.devnoway.posproyect.data.modelo.enPesos
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
        titulo = stringResource(R.string.per_titulo),
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
                        text = usuario?.nombre ?: stringResource(R.string.per_invitado),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when {
                            usuario == null -> stringResource(R.string.per_sin_sesion)
                            usuario.esAdmin -> stringResource(R.string.per_rol_admin)
                            else -> stringResource(R.string.per_rol_cliente)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (usuario != null) {
                TituloSeccion(stringResource(R.string.per_mis_datos))
                Column {
                    FilaDato(stringResource(R.string.per_correo), usuario.correo)
                    FilaDato(stringResource(R.string.per_telefono), usuario.telefono)
                    FilaDato(stringResource(R.string.per_direccion), usuario.direccion)
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
            TituloSeccion(stringResource(R.string.per_ajustes))
            FilaNavegacion(
                emoji = "\uD83E\uDDFE",
                titulo = stringResource(R.string.per_mis_pedidos),
                subtitulo = stringResource(R.string.per_mis_pedidos_sub, estado.pedidos.size),
                onClick = onMisPedidos
            )
            if (estado.pedidos.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.per_ultimos_pedidos),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
                estado.pedidos.take(3).forEach { pedido ->
                    FilaNavegacion(
                        emoji = "\uD83D\uDCE6",
                        titulo = pedido.folio,
                        subtitulo = stringResource(
                            R.string.per_pedido_resumen,
                            pedido.fecha,
                            pedido.total.enPesos()
                        ),
                        onClick = onMisPedidos
                    )
                }
            }
            FilaNavegacion(
                emoji = "\uD83D\uDCCD",
                titulo = stringResource(R.string.per_direcciones),
                subtitulo = stringResource(R.string.per_direcciones_sub),
                onClick = onDirecciones
            )
            FilaNavegacion(
                emoji = "\uD83D\uDCB3",
                titulo = stringResource(R.string.per_metodos_pago),
                subtitulo = stringResource(R.string.per_metodos_pago_sub),
                onClick = onMetodosPago
            )
            FilaNavegacion(
                emoji = "\uD83D\uDD14",
                titulo = stringResource(R.string.per_notificaciones),
                subtitulo = stringResource(R.string.per_notificaciones_sub, estado.noLeidas),
                onClick = onNotificaciones
            )
            FilaNavegacion(
                emoji = "\uD83D\uDD04",
                titulo = stringResource(R.string.per_sincronizar),
                subtitulo = if (estado.enLinea) {
                    stringResource(R.string.per_sincronizar_online)
                } else {
                    stringResource(R.string.per_sincronizar_offline)
                },
                onClick = viewModel::sincronizar
            )
            FilaNavegacion(
                emoji = "\u2699",
                titulo = stringResource(R.string.per_modo_admin),
                subtitulo = stringResource(R.string.per_modo_admin_sub),
                onClick = onModoAdministrador
            )
            FilaNavegacion(
                emoji = "\u2753",
                titulo = stringResource(R.string.per_ayuda),
                subtitulo = stringResource(R.string.per_ayuda_sub),
                onClick = onAyuda
            )

            Spacer(Modifier.height(8.dp))
            BotonSecundario(
                texto = if (usuario == null) {
                    stringResource(R.string.per_iniciar_sesion)
                } else {
                    stringResource(R.string.per_cerrar_sesion)
                },
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
                text = stringResource(R.string.per_version),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
