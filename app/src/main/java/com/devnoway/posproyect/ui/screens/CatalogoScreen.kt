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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devnoway.posproyect.data.DatosMock
import com.devnoway.posproyect.navigation.Rutas
import com.devnoway.posproyect.ui.components.CampoTexto
import com.devnoway.posproyect.ui.components.ChipCategoria
import com.devnoway.posproyect.ui.components.MarcoCliente
import com.devnoway.posproyect.ui.components.TarjetaProducto
import com.devnoway.posproyect.ui.components.TituloSeccion
import com.devnoway.posproyect.ui.theme.AcambapanTheme

/**
 * Inicio / catalogo: lista de productos de la panaderia.
 * Los chips de categoria filtran la lista (estado local) y cada tarjeta
 * abre el detalle del producto.
 */
@Composable
fun CatalogoScreen(
    onAbrirProducto: (Int) -> Unit,
    onNavegar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var busqueda by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf(DatosMock.categorias.first()) }

    val productos = if (categoriaSeleccionada == DatosMock.categorias.first()) {
        DatosMock.productos
    } else {
        DatosMock.productos.filter { it.categoria == categoriaSeleccionada }
    }

    MarcoCliente(
        titulo = "Acambapan",
        rutaActual = Rutas.CATALOGO,
        onNavegar = onNavegar,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            CampoTexto(
                valor = busqueda,
                onValorCambio = { busqueda = it },
                etiqueta = "Buscar pan, pastel o bebida"
            )

            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DatosMock.categorias.forEach { categoria ->
                    ChipCategoria(
                        texto = categoria,
                        seleccionado = categoria == categoriaSeleccionada,
                        onClick = { categoriaSeleccionada = categoria }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            PromocionEnvio()

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TituloSeccion("Nuestro catálogo")
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${productos.size} artículos",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(productos, key = { it.id }) { producto ->
                    TarjetaProducto(
                        producto = producto,
                        onClick = { onAbrirProducto(producto.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PromocionEnvio() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🥖", fontSize = 26.sp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "Envío gratis",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "En pedidos mayores a $250 dentro de Acambay",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CatalogoScreenPreview() {
    AcambapanTheme {
        CatalogoScreen(onAbrirProducto = {}, onNavegar = {})
    }
}
