package com.devnoway.posproyect.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.devnoway.posproyect.ui.components.BotonPrincipal
import com.devnoway.posproyect.ui.components.BotonSecundario
import com.devnoway.posproyect.ui.components.BotonTexto
import com.devnoway.posproyect.ui.components.EtiquetaEstado
import com.devnoway.posproyect.ui.components.FilaDato
import com.devnoway.posproyect.ui.components.IndicadorConexion
import com.devnoway.posproyect.ui.components.MarcoCliente
import com.devnoway.posproyect.ui.components.TituloSeccion
import com.devnoway.posproyect.ui.theme.VerdeExito
import com.devnoway.posproyect.ui.viewmodel.SeguimientoViewModel

/**
 * Seguimiento del pedido.
 *
 * Observa el pedido más reciente de Room (se actualiza solo cuando el servidor
 * lo cambia) y permite refrescarlo o reenviarlo si se creó sin conexión.
 */
@Composable
fun SeguimientoScreen(
    contenedor: ContenedorApp,
    onVolverAlCatalogo: () -> Unit,
    onNavegar: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SeguimientoViewModel = viewModel(factory = SeguimientoViewModel.fabrica(contenedor))
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    val pedido = estado.pedido

    MarcoCliente(
        titulo = "Seguimiento",
        rutaActual = Rutas.SEGUIMIENTO,
        onNavegar = onNavegar,
        modifier = modifier,
        acciones = {
            IndicadorConexion(
                enLinea = estado.enLinea,
                sincronizando = estado.actualizando,
                modifier = Modifier.padding(end = 4.dp)
            )
            IconButton(onClick = viewModel::refrescar) {
                Text(text = "⟳", fontSize = 20.sp)
            }
        }
    ) {
        if (pedido == null) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(48.dp))
                Text(
                    text = "Todavía no tienes pedidos.\nCuando confirmes el carrito verás aquí su estado.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(18.dp))
                BotonPrincipal(texto = "Ir al catálogo", onClick = onVolverAlCatalogo)
            }
            return@MarcoCliente
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pedido ${pedido.folio}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = pedido.fecha,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        EtiquetaEstado(
                            texto = pedido.estado.etiqueta,
                            color = if (pedido.estado == EstadoPedido.ENTREGADO) {
                                VerdeExito
                            } else {
                                MaterialTheme.colorScheme.secondary
                            }
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    FilaDato(etiqueta = "Entrega", valor = pedido.entrega)
                    FilaDato(etiqueta = "Total", valor = pedido.total.enPesos())
                    if (pedido.lineas.isNotEmpty()) {
                        FilaDato(
                            etiqueta = "Productos",
                            valor = pedido.lineas.joinToString { "${it.cantidad}× ${it.nombre}" }
                        )
                    }
                }
            }

            if (pedido.pendienteSincronizar) {
                Spacer(Modifier.height(12.dp))
                BandaMensaje(
                    mensaje = "Este pedido está guardado en el teléfono y todavía no se envió a la panadería. Se enviará al recuperar la conexión."
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                ) {
                    BotonTexto(
                        texto = if (estado.actualizando) "Enviando…" else "Reintentar envío",
                        onClick = viewModel::refrescar
                    )
                }
            }

            estado.mensaje?.let { mensaje ->
                Spacer(Modifier.height(12.dp))
                BandaMensaje(
                    mensaje = mensaje,
                    esError = true,
                    onCerrar = viewModel::limpiarMensaje
                )
            }

            if (pedido.estado == EstadoPedido.CANCELADO) {
                Spacer(Modifier.height(16.dp))
                BandaMensaje(mensaje = "Este pedido fue cancelado.", esError = true)
            } else {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Llega en aproximadamente ${pedido.tiempoEstimado}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                val progreso = (
                    EstadoPedido.flujo.indexOf(pedido.estado).coerceAtLeast(0) + 1
                    ).toFloat() / EstadoPedido.flujo.size
                LinearProgressIndicator(
                    progress = { progreso },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(20.dp))
                TituloSeccion("Estado del pedido")
                Spacer(Modifier.height(12.dp))

                EstadoPedido.flujo.forEachIndexed { indice, paso ->
                    val completado = indice <= EstadoPedido.flujo.indexOf(pedido.estado)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (completado) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (completado) "✓" else "${indice + 1}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (completado) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = paso.etiqueta,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (completado) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (completado) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    if (indice < EstadoPedido.flujo.lastIndex) {
                        Box(
                            modifier = Modifier
                                .padding(start = 14.dp)
                                .width(2.dp)
                                .height(22.dp)
                                .background(MaterialTheme.colorScheme.outline)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
                TituloSeccion("Repartidor")
                Spacer(Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = pedido.repartidor ?: "Aún sin asignar",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = pedido.telefonoRepartidor ?: "La panadería asignará a alguien al salir el pedido.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                LlamarRepartidor(pedido = pedido, onLlamar = { telefono ->
                    val limpio = telefono.filter { it.isDigit() || it == '+' }
                    contexto.startActivity(
                        Intent(Intent.ACTION_DIAL, Uri.parse("tel:$limpio"))
                    )
                })
            }

            Spacer(Modifier.height(8.dp))
            BotonPrincipal(texto = "Volver al catálogo", onClick = onVolverAlCatalogo)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun LlamarRepartidor(pedido: Pedido, onLlamar: (String) -> Unit) {
    val telefono = pedido.telefonoRepartidor
    BotonSecundario(
        texto = "Llamar al repartidor",
        onClick = { telefono?.let(onLlamar) },
        habilitado = !telefono.isNullOrBlank()
    )
}
