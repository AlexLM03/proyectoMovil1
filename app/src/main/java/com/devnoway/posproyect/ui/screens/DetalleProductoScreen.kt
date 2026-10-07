package com.devnoway.posproyect.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devnoway.posproyect.data.DatosMock
import com.devnoway.posproyect.data.enPesos
import com.devnoway.posproyect.navigation.Rutas
import com.devnoway.posproyect.ui.components.BotonPrincipal
import com.devnoway.posproyect.ui.components.BotonSecundario
import com.devnoway.posproyect.ui.components.EtiquetaEstado
import com.devnoway.posproyect.ui.components.FilaDato
import com.devnoway.posproyect.ui.components.MarcoCliente
import com.devnoway.posproyect.ui.components.RecuadroEmoji
import com.devnoway.posproyect.ui.theme.AcambapanTheme

/**
 * Detalle del producto elegido en el catalogo.
 *
 * Recibe el id del producto por la ruta (detalle_producto/{productoId}) y lo
 * busca en los datos de ejemplo.
 */
@Composable
fun DetalleProductoScreen(
    productoId: Int,
    onAgregarAlCarrito: () -> Unit,
    onAtras: () -> Unit,
    onNavegar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val producto = DatosMock.productos.firstOrNull { it.id == productoId }
        ?: DatosMock.productos.first()

    var cantidad by remember { mutableIntStateOf(1) }

    MarcoCliente(
        titulo = "Detalle",
        rutaActual = Rutas.CATALOGO,
        onNavegar = onNavegar,
        onAtras = onAtras,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RecuadroEmoji(emoji = producto.emoji, tamano = 140.dp)

            Text(
                text = producto.nombre,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            EtiquetaEstado(texto = producto.categoria, color = MaterialTheme.colorScheme.secondary)

            Text(
                text = producto.precio.enPesos(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = producto.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))
            Text(
                text = "Cantidad",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (cantidad > 1) cantidad-- }) {
                    Text(text = "−", fontSize = 26.sp)
                }
                Text(
                    text = cantidad.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                IconButton(onClick = { cantidad++ }) {
                    Text(text = "+", fontSize = 22.sp)
                }
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    FilaDato(
                        etiqueta = "Disponibilidad",
                        valor = if (producto.disponible) "Disponible hoy" else "Agotado"
                    )
                    FilaDato(
                        etiqueta = "Total",
                        valor = (producto.precio * cantidad).enPesos()
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            BotonPrincipal(texto = "Agregar al carrito", onClick = onAgregarAlCarrito)
            BotonSecundario(texto = "Seguir comprando", onClick = onAtras)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetalleProductoScreenPreview() {
    AcambapanTheme {
        DetalleProductoScreen(
            productoId = 1,
            onAgregarAlCarrito = {},
            onAtras = {},
            onNavegar = {}
        )
    }
}
