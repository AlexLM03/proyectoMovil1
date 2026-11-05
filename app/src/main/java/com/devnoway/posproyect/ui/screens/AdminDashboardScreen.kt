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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devnoway.posproyect.data.DatosMock
import com.devnoway.posproyect.data.EstadoPedido
import com.devnoway.posproyect.data.enPesos
import com.devnoway.posproyect.navigation.Rutas
import com.devnoway.posproyect.ui.components.BotonPrincipal
import com.devnoway.posproyect.ui.components.BotonSecundario
import com.devnoway.posproyect.ui.components.EtiquetaEstado
import com.devnoway.posproyect.ui.components.MarcoAdmin
import com.devnoway.posproyect.ui.components.TarjetaIndicador
import com.devnoway.posproyect.ui.components.TituloSeccion
import com.devnoway.posproyect.ui.theme.AcambapanTheme
import com.devnoway.posproyect.ui.theme.RojoAlerta
import com.devnoway.posproyect.ui.theme.VerdeExito

/** Avance de la meta de ventas del dia (dato de ejemplo). */
private const val META_DEL_DIA = 7000.0

/**
 * Dash administrativo: indicadores del negocio en un solo vistazo.
 * Los numeros son de ejemplo; en la version final salen de la base de datos.
 */
@Composable
fun AdminDashboardScreen(
    onNavegar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val avanceMeta = (DatosMock.ventasDelDia / META_DEL_DIA).toFloat().coerceIn(0f, 1f)

    MarcoAdmin(
        titulo = "Dash administrativo",
        rutaActual = Rutas.ADMIN_DASH,
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
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaIndicador(
                    titulo = "Ventas del día",
                    valor = DatosMock.ventasDelDia.enPesos(),
                    modifier = Modifier.weight(1f)
                )
                TarjetaIndicador(
                    titulo = "Pedidos",
                    valor = DatosMock.pedidosDelDia.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaIndicador(
                    titulo = "Ticket promedio",
                    valor = DatosMock.ticketPromedio.enPesos(),
                    modifier = Modifier.weight(1f)
                )
                TarjetaIndicador(
                    titulo = "Insumos bajos",
                    valor = DatosMock.productosBajosDeStock.toString(),
                    modifier = Modifier.weight(1f),
                    colorValor = RojoAlerta
                )
            }

            Spacer(Modifier.height(4.dp))
            TituloSeccion("Meta del día")
            Text(
                text = "${DatosMock.ventasDelDia.enPesos()} de ${META_DEL_DIA.enPesos()}",
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
            DatosMock.pedidos.take(3).forEach { pedido ->
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

@Preview(showBackground = true)
@Composable
fun AdminDashboardScreenPreview() {
    AcambapanTheme {
        AdminDashboardScreen(onNavegar = {})
    }
}
