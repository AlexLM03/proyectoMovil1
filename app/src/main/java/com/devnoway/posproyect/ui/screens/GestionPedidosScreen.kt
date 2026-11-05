package com.devnoway.posproyect.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devnoway.posproyect.data.DatosMock
import com.devnoway.posproyect.data.EstadoPedido
import com.devnoway.posproyect.data.Pedido
import com.devnoway.posproyect.data.enPesos
import com.devnoway.posproyect.navigation.Rutas
import com.devnoway.posproyect.ui.components.BotonTexto
import com.devnoway.posproyect.ui.components.ChipCategoria
import com.devnoway.posproyect.ui.components.EtiquetaEstado
import com.devnoway.posproyect.ui.components.MarcoAdmin
import com.devnoway.posproyect.ui.theme.AcambapanTheme
import com.devnoway.posproyect.ui.theme.VerdeExito

/** Valor de filtro para ver todos los pedidos sin importar el estado. */
private const val FILTRO_TODOS = "Todos"

@Composable
private fun colorDeEstado(estado: EstadoPedido): Color = when (estado) {
    EstadoPedido.RECIBIDO -> MaterialTheme.colorScheme.onSurfaceVariant
    EstadoPedido.EN_PREPARACION -> MaterialTheme.colorScheme.secondary
    EstadoPedido.EN_CAMINO -> MaterialTheme.colorScheme.tertiary
    EstadoPedido.ENTREGADO -> VerdeExito
}

/**
 * Gestion de pedidos del administrador.
 *
 * Los chips filtran la lista por estado y cada pedido tiene un boton para
 * abrir el seguimiento. Cambiar el estado real sera una llamada a la API.
 */
@Composable
fun GestionPedidosScreen(
    onVerSeguimiento: () -> Unit,
    onNavegar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filtros = listOf(FILTRO_TODOS) + EstadoPedido.entries.map { it.etiqueta }
    var filtroSeleccionado by remember { mutableStateOf(FILTRO_TODOS) }

    val pedidos = if (filtroSeleccionado == FILTRO_TODOS) {
        DatosMock.pedidos
    } else {
        DatosMock.pedidos.filter { it.estado.etiqueta == filtroSeleccionado }
    }

    MarcoAdmin(
        titulo = "Gestión de pedidos",
        rutaActual = Rutas.ADMIN_PEDIDOS,
        onNavegar = onNavegar,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filtros.forEach { filtro ->
                    ChipCategoria(
                        texto = filtro,
                        seleccionado = filtro == filtroSeleccionado,
                        onClick = { filtroSeleccionado = filtro }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                text = "${pedidos.size} pedidos",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(pedidos, key = { it.folio }) { pedido ->
                    TarjetaPedido(
                        pedido = pedido,
                        onVerSeguimiento = onVerSeguimiento
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaPedido(pedido: Pedido, onVerSeguimiento: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pedido.folio,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = pedido.cliente,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${pedido.fecha} · ${pedido.entrega}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = pedido.total.enPesos(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    EtiquetaEstado(
                        texto = pedido.estado.etiqueta,
                        color = colorDeEstado(pedido.estado)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                BotonTexto(
                    texto = "Ver seguimiento",
                    onClick = onVerSeguimiento
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GestionPedidosScreenPreview() {
    AcambapanTheme {
        GestionPedidosScreen(onVerSeguimiento = {}, onNavegar = {})
    }
}
