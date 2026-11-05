package com.devnoway.posproyect.ui.screens

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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.devnoway.posproyect.ui.components.DialogoPendiente
import com.devnoway.posproyect.ui.components.EtiquetaEstado
import com.devnoway.posproyect.ui.components.FilaDato
import com.devnoway.posproyect.ui.components.MarcoCliente
import com.devnoway.posproyect.ui.components.TituloSeccion
import com.devnoway.posproyect.ui.theme.AcambapanTheme
import com.devnoway.posproyect.ui.theme.VerdeExito

/**
 * Seguimiento del pedido.
 *
 * Muestra la linea de tiempo con los 4 estados y los datos del repartidor.
 * En la version final esta pantalla se actualiza desde la API (tiempo real).
 */
@Composable
fun SeguimientoScreen(
    onVolverAlCatalogo: () -> Unit,
    onNavegar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val pedido = DatosMock.pedidoEnCurso
    val pasos = EstadoPedido.entries
    val progreso = (pedido.estado.ordinal + 1).toFloat() / pasos.size

    var mostrarAviso by remember { mutableStateOf(false) }

    MarcoCliente(
        titulo = "Seguimiento",
        rutaActual = Rutas.SEGUIMIENTO,
        onNavegar = onNavegar,
        modifier = modifier
    ) {
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
                            color = if (pedido.estado == EstadoPedido.ENTREGADO) VerdeExito else MaterialTheme.colorScheme.secondary
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    FilaDato(etiqueta = "Entrega", valor = pedido.entrega)
                    FilaDato(etiqueta = "Total", valor = pedido.total.enPesos())
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = "Llega en aproximadamente ${DatosMock.tiempoEstimado}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(20.dp))
            TituloSeccion("Estado del pedido")
            Spacer(Modifier.height(12.dp))

            pasos.forEachIndexed { indice, paso ->
                val completado = indice <= pedido.estado.ordinal
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
                if (indice < pasos.lastIndex) {
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = DatosMock.repartidor,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = DatosMock.telefonoRepartidor,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            BotonSecundario(
                texto = "Llamar al repartidor",
                onClick = { mostrarAviso = true }
            )
            Spacer(Modifier.height(8.dp))
            BotonPrincipal(texto = "Volver al catálogo", onClick = onVolverAlCatalogo)
        }
    }

    if (mostrarAviso) {
        DialogoPendiente(
            titulo = "Llamar al repartidor",
            mensaje = "En la version final este boton abre el marcador con el numero del repartidor. Todavia no esta programado.",
            onCerrar = { mostrarAviso = false }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SeguimientoScreenPreview() {
    AcambapanTheme {
        SeguimientoScreen(onVolverAlCatalogo = {}, onNavegar = {})
    }
}
