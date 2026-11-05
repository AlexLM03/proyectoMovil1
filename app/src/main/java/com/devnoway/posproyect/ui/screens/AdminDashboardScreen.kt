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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devnoway.posproyect.data.modelo.EstadoPedido
import com.devnoway.posproyect.data.modelo.enPesos
import com.devnoway.posproyect.di.ContenedorApp
import com.devnoway.posproyect.navigation.Rutas
import com.devnoway.posproyect.ui.components.BandaMensaje
import com.devnoway.posproyect.ui.components.BotonPrincipal
import com.devnoway.posproyect.ui.components.BotonSecundario
import com.devnoway.posproyect.ui.components.EtiquetaEstado
import com.devnoway.posproyect.ui.components.IndicadorConexion
import com.devnoway.posproyect.ui.components.MarcoAdmin
import com.devnoway.posproyect.ui.components.TarjetaIndicador
import com.devnoway.posproyect.ui.components.TituloSeccion
import com.devnoway.posproyect.ui.theme.RojoAlerta
import com.devnoway.posproyect.ui.theme.VerdeExito
import com.devnoway.posproyect.ui.viewmodel.AdminResumenViewModel

/** Meta de ventas del día (se puede ajustar cuando el negocio lo pida). */
private const val META_DEL_DIA = 7000.0

/**
 * Dash administrativo: los indicadores del negocio en un solo vistazo.
 * Todo se calcula con lo guardado en Room, así que se abre al instante.
 */
@Composable
fun AdminDashboardScreen(
    contenedor: ContenedorApp,
    onNavegar: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdminResumenViewModel = viewModel(
        factory = AdminResumenViewModel.fabrica(contenedor)
    )
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val avanceMeta = (estado.ventasHoy / META_DEL_DIA).toFloat().coerceIn(0f, 1f)

    MarcoAdmin(
        titulo = "Dash administrativo",
        rutaActual = Rutas.ADMIN_DASH,
        onNavegar = onNavegar,
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
            estado.mensaje?.let { mensaje ->
                BandaMensaje(
                    mensaje = mensaje,
                    esError = true,
                    onCerrar = viewModel::limpiarMensaje
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaIndicador(
                    titulo = "Ventas del día",
                    valor = estado.ventasHoy.enPesos(),
                    modifier = Modifier.weight(1f)
                )
                TarjetaIndicador(
                    titulo = "Pedidos",
                    valor = estado.pedidosHoy.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaIndicador(
                    titulo = "Ticket promedio",
                    valor = estado.ticketPromedio.enPesos(),
                    modifier = Modifier.weight(1f)
                )
                TarjetaIndicador(
                    titulo = "Insumos bajos",
                    valor = estado.insumosBajos.toString(),
                    modifier = Modifier.weight(1f),
                    colorValor = RojoAlerta
                )
            }

            Spacer(Modifier.height(4.dp))
            TituloSeccion("Meta del día")
            Text(
                text = "${estado.ventasHoy.enPesos()} de ${META_DEL_DIA.enPesos()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LinearProgressIndicator(
                progress = { avanceMeta },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "${(avanceMeta * 100).toInt()}% alcanzado",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(4.dp))
            TituloSeccion("Pedidos recientes")
            if (estado.recientes.isEmpty()) {
                SinDatos("Todavía no hay pedidos guardados. Toca ⟳ para traerlos del servidor.")
            }
            estado.recientes.forEach { pedido ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pedido.folio,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${pedido.cliente} · ${pedido.fecha}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = pedido.total.enPesos(),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            EtiquetaEstado(
                                texto = pedido.estado.etiqueta,
                                color = if (pedido.estado == EstadoPedido.ENTREGADO) {
                                    VerdeExito
                                } else {
                                    MaterialTheme.colorScheme.secondary
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            BotonPrincipal(
                texto = "Ver todos los pedidos",
                onClick = { onNavegar(Rutas.ADMIN_PEDIDOS) }
            )
            BotonSecundario(
                texto = "Revisar inventario",
                onClick = { onNavegar(Rutas.ADMIN_INVENTARIO) }
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}
