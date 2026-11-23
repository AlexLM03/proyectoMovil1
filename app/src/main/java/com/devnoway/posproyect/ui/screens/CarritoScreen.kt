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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devnoway.posproyect.data.modelo.MetodoPago
import com.devnoway.posproyect.data.modelo.enPesos
import com.devnoway.posproyect.di.ContenedorApp
import com.devnoway.posproyect.navigation.Rutas
import com.devnoway.posproyect.ui.components.BandaMensaje
import com.devnoway.posproyect.ui.components.BotonPrincipal
import com.devnoway.posproyect.ui.components.BotonSecundario
import com.devnoway.posproyect.ui.components.CampoTexto
import com.devnoway.posproyect.ui.components.ChipCategoria
import com.devnoway.posproyect.ui.components.FilaDato
import com.devnoway.posproyect.ui.components.MarcoCliente
import com.devnoway.posproyect.ui.components.RecuadroEmoji
import com.devnoway.posproyect.ui.components.TituloSeccion
import com.devnoway.posproyect.ui.viewmodel.CarritoViewModel

/**
 * Carrito y pago.
 *
 * Las líneas viven en el carrito compartido del contenedor. Al confirmar se
 * crea el pedido en Room y se intenta enviar a la API; si no hay red queda en
 * cola y el seguimiento lo avisa.
 */
@Composable
fun CarritoScreen(
    contenedor: ContenedorApp,
    onConfirmarPedido: () -> Unit,
    onSeguirComprando: () -> Unit,
    onNavegar: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CarritoViewModel = viewModel(factory = CarritoViewModel.fabrica(contenedor))
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    LaunchedEffect(estado.folioCreado) {
        if (estado.folioCreado != null) {
            viewModel.limpiarNavegacion()
            onConfirmarPedido()
        }
    }

    MarcoCliente(
        titulo = "Carrito",
        rutaActual = Rutas.CARRITO,
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
            TituloSeccion("Tu pedido (${estado.lineas.size} productos · ${estado.piezas} piezas)")

            if (estado.vacio) {
                Text(
                    text = "Tu carrito está vacío. Agrega pan, pasteles o bebidas del catálogo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                )
                BotonSecundario(texto = "Ir al catálogo", onClick = onSeguirComprando)
            }

            estado.lineas.forEach { linea ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RecuadroEmoji(emoji = linea.producto.emoji, tamano = 48.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = linea.producto.nombre,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${linea.cantidad} x ${linea.producto.precio.enPesos()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = linea.subtotal.enPesos(),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = {
                                    viewModel.cambiarCantidad(
                                        linea.producto.id,
                                        linea.cantidad - 1
                                    )
                                }
                            ) { Text(text = "−", fontSize = 20.sp) }
                            Text(
                                text = linea.cantidad.toString(),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = {
                                    viewModel.cambiarCantidad(
                                        linea.producto.id,
                                        linea.cantidad + 1
                                    )
                                }
                            ) { Text(text = "+", fontSize = 18.sp) }
                            IconButton(onClick = { viewModel.quitar(linea.producto.id) }) {
                                Text(text = "✕", fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

            estado.error?.let { mensaje ->
                BandaMensaje(
                    mensaje = mensaje,
                    esError = true,
                    onCerrar = viewModel::limpiarError
                )
            }

            if (!estado.vacio) {
                Spacer(Modifier.height(4.dp))
                TituloSeccion("Entrega")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChipCategoria(
                        texto = "A domicilio",
                        seleccionado = estado.aDomicilio,
                        onClick = { viewModel.cambiarEntrega(true) }
                    )
                    ChipCategoria(
                        texto = "Para recoger",
                        seleccionado = !estado.aDomicilio,
                        onClick = { viewModel.cambiarEntrega(false) }
                    )
                }

                if (estado.aDomicilio) {
                    CampoTexto(
                        valor = estado.direccion,
                        onValorCambio = viewModel::cambiarDireccion,
                        etiqueta = "Dirección de entrega",
                        lineas = 2
                    )
                }

                Spacer(Modifier.height(4.dp))
                TituloSeccion("Pago")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetodoPago.entries.forEach { metodo ->
                        ChipCategoria(
                            texto = metodo.etiqueta,
                            seleccionado = estado.metodoPago == metodo,
                            onClick = { viewModel.cambiarMetodoPago(metodo) }
                        )
                    }
                }
                Text(
                    text = estado.metodoPago.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(4.dp))
                TituloSeccion("Resumen")
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        FilaDato(etiqueta = "Subtotal", valor = estado.subtotal.enPesos())
                        FilaDato(
                            etiqueta = "Envío",
                            valor = if (estado.costoEnvio == 0.0) {
                                "Gratis"
                            } else {
                                estado.costoEnvio.enPesos()
                            }
                        )
                        FilaDato(etiqueta = "Total", valor = estado.total.enPesos())
                    }
                }

                Spacer(Modifier.height(4.dp))
                BotonPrincipal(
                    texto = if (estado.confirmando) "Guardando pedido…" else "Confirmar pedido",
                    onClick = viewModel::confirmarPedido,
                    habilitado = !estado.confirmando && !estado.vacio
                )
                BotonSecundario(texto = "Seguir comprando", onClick = onSeguirComprando)
            }
        }
    }
}
