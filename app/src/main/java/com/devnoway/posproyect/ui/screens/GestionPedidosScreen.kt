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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devnoway.posproyect.data.modelo.EstadoPedido
import com.devnoway.posproyect.data.modelo.Pedido
import com.devnoway.posproyect.data.modelo.enPesos
import com.devnoway.posproyect.di.ContenedorApp
import com.devnoway.posproyect.navigation.Rutas
import com.devnoway.posproyect.ui.components.BandaMensaje
import com.devnoway.posproyect.ui.components.BotonTexto
import com.devnoway.posproyect.ui.components.ChipCategoria
import com.devnoway.posproyect.ui.components.EtiquetaEstado
import com.devnoway.posproyect.ui.components.IndicadorConexion
import com.devnoway.posproyect.ui.components.MarcoAdmin
import com.devnoway.posproyect.ui.theme.VerdeExito
import com.devnoway.posproyect.ui.viewmodel.PedidosAdminViewModel

@Composable
private fun colorDeEstado(estado: EstadoPedido): Color = when (estado) {
    EstadoPedido.RECIBIDO -> MaterialTheme.colorScheme.onSurfaceVariant
    EstadoPedido.EN_PREPARACION -> MaterialTheme.colorScheme.secondary
    EstadoPedido.EN_CAMINO -> MaterialTheme.colorScheme.tertiary
    EstadoPedido.ENTREGADO -> VerdeExito
    EstadoPedido.CANCELADO -> MaterialTheme.colorScheme.error
}

/** Siguiente estado del flujo normal de un pedido. */
private fun siguienteEstado(estado: EstadoPedido): EstadoPedido? = when (estado) {
    EstadoPedido.RECIBIDO -> EstadoPedido.EN_PREPARACION
    EstadoPedido.EN_PREPARACION -> EstadoPedido.EN_CAMINO
    EstadoPedido.EN_CAMINO -> EstadoPedido.ENTREGADO
    EstadoPedido.ENTREGADO, EstadoPedido.CANCELADO -> null
}

/**
 * Gestión de pedidos del administrador.
 *
 * Los chips filtran y cada pedido permite avanzar al siguiente estado o
 * cancelarlo. Cada cambio se manda a la Web API y se refleja en Room, así el
 * cliente lo ve en su seguimiento.
 */
@Composable
fun GestionPedidosScreen(
    contenedor: ContenedorApp,
    onVerSeguimiento: () -> Unit,
    onNavegar: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PedidosAdminViewModel = viewModel(
        factory = PedidosAdminViewModel.fabrica(contenedor)
    )
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    MarcoAdmin(
        titulo = "Gestión de pedidos",
        rutaActual = Rutas.ADMIN_PEDIDOS,
        onNavegar = onNavegar,
        modifier = modifier,
        acciones = {
            IndicadorConexion(
                enLinea = estado.enLinea,
                sincronizando = estado.trabajando,
                modifier = Modifier.padding(end = 4.dp)
            )
            IconButton(onClick = viewModel::sincronizar) {
                Text(text = "⟳", fontSize = 20.sp)
            }
        }
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
                ChipCategoria(
                    texto = "Todos",
                    seleccionado = estado.filtro == null,
                    onClick = { viewModel.filtrar(null) }
                )
                EstadoPedido.entries.forEach { filtro ->
                    ChipCategoria(
                        texto = filtro.etiqueta,
                        seleccionado = estado.filtro == filtro,
                        onClick = { viewModel.filtrar(filtro) }
                    )
                }
            }

            estado.mensaje?.let { mensaje ->
                Spacer(Modifier.height(10.dp))
                BandaMensaje(
                    mensaje = mensaje,
                    esError = false,
                    onCerrar = viewModel::limpiarMensaje
                )
            }

            Spacer(Modifier.height(12.dp))
            Text(
                text = "${estado.visibles.size} pedidos",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(estado.visibles, key = { it.folio }) { pedido ->
                    TarjetaPedido(
                        pedido = pedido,
                        habilitado = !estado.trabajando,
                        onVerSeguimiento = onVerSeguimiento,
                        onCambiarEstado = { nuevo -> viewModel.cambiarEstado(pedido.folio, nuevo) }
                    )
                }
                if (estado.visibles.isEmpty()) {
                    item {
                        Text(
                            text = "No hay pedidos con este filtro.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaPedido(
    pedido: Pedido,
    habilitado: Boolean,
    onVerSeguimiento: () -> Unit,
    onCambiarEstado: (EstadoPedido) -> Unit
) {
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
                    Text(text = pedido.cliente, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "${pedido.fecha} · ${pedido.entrega}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (pedido.pendienteSincronizar) {
                        Text(
                            text = "Pendiente de envío (creado sin conexión)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
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
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BotonTexto(texto = "Ver seguimiento", onClick = onVerSeguimiento)
                siguienteEstado(pedido.estado)?.let { siguiente ->
                    BotonTexto(
                        texto = "Marcar ${siguiente.etiqueta.lowercase()}",
                        onClick = { if (habilitado) onCambiarEstado(siguiente) }
                    )
                }
                if (!pedido.estado.esFinal) {
                    BotonTexto(
                        texto = "Cancelar",
                        onClick = {
                            if (habilitado) onCambiarEstado(EstadoPedido.CANCELADO)
                        }
                    )
                }
            }
        }
    }
}
