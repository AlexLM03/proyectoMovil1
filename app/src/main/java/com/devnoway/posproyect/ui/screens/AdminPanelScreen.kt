package com.devnoway.posproyect.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devnoway.posproyect.data.modelo.enPesos
import com.devnoway.posproyect.di.ContenedorApp
import com.devnoway.posproyect.navigation.Rutas
import com.devnoway.posproyect.ui.components.BandaMensaje
import com.devnoway.posproyect.ui.components.BotonSecundario
import com.devnoway.posproyect.ui.components.FilaNavegacion
import com.devnoway.posproyect.ui.components.IndicadorConexion
import com.devnoway.posproyect.ui.components.MarcoAdmin
import com.devnoway.posproyect.ui.components.TarjetaIndicador
import com.devnoway.posproyect.ui.components.TituloSeccion
import com.devnoway.posproyect.ui.theme.RojoAlerta
import com.devnoway.posproyect.ui.viewmodel.AdminResumenViewModel

/**
 * Panel principal del administrador.
 *
 * Indicadores calculados con los pedidos e insumos guardados; el botón ⟳ los
 * refresca desde la Web API.
 */
@Composable
fun AdminPanelScreen(
    contenedor: ContenedorApp,
    onNavegar: (String) -> Unit,
    onVolverATienda: () -> Unit,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdminResumenViewModel = viewModel(
        factory = AdminResumenViewModel.fabrica(contenedor)
    )
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val sesion by viewModel.sesion.collectAsStateWithLifecycle()

    MarcoAdmin(
        titulo = "Panel principal",
        rutaActual = Rutas.ADMIN_PANEL,
        onNavegar = onNavegar,
        onAtras = onVolverATienda,
        modifier = modifier,
        acciones = {
            IndicadorConexion(
                enLinea = estado.enLinea,
                sincronizando = estado.sincronizando,
                modifier = Modifier.padding(end = 4.dp)
            )
            IconButton(onClick = viewModel::refrescar) {
                Text(text = "⟳", fontSize = 20.sp)
            }
        }
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Panadería Acambapan",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Sucursal Acámbaro · datos guardados en el teléfono",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Sesión: ${sesion?.correo ?: "sin sesión"} · ${sesion?.rol ?: "-"}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.SemiBold
            )

            estado.mensaje?.let { mensaje ->
                BandaMensaje(
                    mensaje = mensaje,
                    esError = true,
                    onCerrar = viewModel::limpiarMensaje
                )
            }

            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaIndicador(
                    titulo = "Pedidos de hoy",
                    valor = estado.pedidosHoy.toString(),
                    modifier = Modifier.weight(1f)
                )
                TarjetaIndicador(
                    titulo = "Ventas de hoy",
                    valor = estado.ventasHoy.enPesos(),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaIndicador(
                    titulo = "Por enviar",
                    valor = estado.pedidosPendientes.toString(),
                    modifier = Modifier.weight(1f),
                    colorValor = MaterialTheme.colorScheme.secondary
                )
                TarjetaIndicador(
                    titulo = "Insumos bajos",
                    valor = estado.insumosBajos.toString(),
                    modifier = Modifier.weight(1f),
                    colorValor = RojoAlerta
                )
            }

            Spacer(Modifier.height(4.dp))
            TituloSeccion("Accesos rápidos")
            FilaNavegacion(
                emoji = "\uD83D\uDCCA",
                titulo = "Dash administrativo",
                subtitulo = "Indicadores y ventas del día",
                onClick = { onNavegar(Rutas.ADMIN_DASH) }
            )
            FilaNavegacion(
                emoji = "\uD83D\uDCCB",
                titulo = "Gestión de pedidos",
                subtitulo = "Pedidos pendientes, en camino y entregados",
                onClick = { onNavegar(Rutas.ADMIN_PEDIDOS) }
            )
            FilaNavegacion(
                emoji = "\uD83D\uDCE6",
                titulo = "Inventario",
                subtitulo = "Insumos y existencias de la panadería",
                onClick = { onNavegar(Rutas.ADMIN_INVENTARIO) }
            )

            Spacer(Modifier.height(4.dp))
            TituloSeccion("Tienda")
            FilaNavegacion(
                emoji = "\uD83D\uDED2",
                titulo = "Ver catálogo como cliente",
                subtitulo = "Regresa a la tienda de la app",
                onClick = onVolverATienda
            )

            Spacer(Modifier.height(8.dp))
            BotonSecundario(
                texto = "Cerrar sesión de administrador",
                onClick = {
                    viewModel.limpiarMensaje()
                    onCerrarSesion()
                }
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

/** Texto de apoyo para las pantallas administrativas sin datos. */
@Composable
internal fun SinDatos(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    )
}
