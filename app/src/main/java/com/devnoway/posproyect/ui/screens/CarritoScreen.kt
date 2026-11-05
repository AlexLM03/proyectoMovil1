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
import androidx.compose.material3.MaterialTheme
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
import com.devnoway.posproyect.data.enPesos
import com.devnoway.posproyect.navigation.Rutas
import com.devnoway.posproyect.ui.components.BotonPrincipal
import com.devnoway.posproyect.ui.components.BotonSecundario
import com.devnoway.posproyect.ui.components.CampoTexto
import com.devnoway.posproyect.ui.components.ChipCategoria
import com.devnoway.posproyect.ui.components.FilaDato
import com.devnoway.posproyect.ui.components.MarcoCliente
import com.devnoway.posproyect.ui.components.RecuadroEmoji
import com.devnoway.posproyect.ui.components.TituloSeccion
import com.devnoway.posproyect.ui.theme.AcambapanTheme

/** Costo del envio a domicilio dentro de Acambay. */
private const val COSTO_ENVIO = 30.0

/** A partir de este monto el envio sale gratis. */
private const val MONTO_ENVIO_GRATIS = 250.0

/**
 * Carrito y pago.
 *
 * El resumen se calcula con los datos de ejemplo: no hay pasarela de pago ni
 * pedido real, "Confirmar pedido" solo lleva al seguimiento.
 */
@Composable
fun CarritoScreen(
    onConfirmarPedido: () -> Unit,
    onSeguirComprando: () -> Unit,
    onNavegar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var aDomicilio by remember { mutableStateOf(true) }
    var direccion by remember { mutableStateOf(DatosMock.usuario.direccion) }

    val subtotal = DatosMock.carrito.sumOf { it.subtotal }
    val envio = if (aDomicilio && subtotal < MONTO_ENVIO_GRATIS) COSTO_ENVIO else 0.0
    val total = subtotal + envio

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
            TituloSeccion("Tu pedido (${DatosMock.carrito.size} productos)")

            DatosMock.carrito.forEach { linea ->
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
                }
            }

            Spacer(Modifier.height(4.dp))
            TituloSeccion("Entrega")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChipCategoria(
                    texto = "A domicilio",
                    seleccionado = aDomicilio,
                    onClick = { aDomicilio = true }
                )
                ChipCategoria(
                    texto = "Para recoger",
                    seleccionado = !aDomicilio,
                    onClick = { aDomicilio = false }
                )
            }

            if (aDomicilio) {
                CampoTexto(
                    valor = direccion,
                    onValorCambio = { direccion = it },
                    etiqueta = "Dirección de entrega",
                    lineas = 2
                )
            }

            Spacer(Modifier.height(4.dp))
            TituloSeccion("Resumen")
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    FilaDato(etiqueta = "Subtotal", valor = subtotal.enPesos())
                    FilaDato(
                        etiqueta = "Envío",
                        valor = if (envio == 0.0) "Gratis" else envio.enPesos()
                    )
                    FilaDato(etiqueta = "Total", valor = total.enPesos())
                }
            }

            Spacer(Modifier.height(4.dp))
            BotonPrincipal(texto = "Confirmar pedido", onClick = onConfirmarPedido)
            BotonSecundario(texto = "Seguir comprando", onClick = onSeguirComprando)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CarritoScreenPreview() {
    AcambapanTheme {
        CarritoScreen(onConfirmarPedido = {}, onSeguirComprando = {}, onNavegar = {})
    }
}
