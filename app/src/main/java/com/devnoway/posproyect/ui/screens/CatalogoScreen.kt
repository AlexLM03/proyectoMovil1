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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devnoway.posproyect.di.ContenedorApp
import com.devnoway.posproyect.navigation.Rutas
import com.devnoway.posproyect.ui.components.BandaMensaje
import com.devnoway.posproyect.ui.components.CampoTexto
import com.devnoway.posproyect.ui.components.ChipCategoria
import com.devnoway.posproyect.ui.components.IndicadorConexion
import com.devnoway.posproyect.ui.components.MarcoCliente
import com.devnoway.posproyect.ui.components.TarjetaProducto
import com.devnoway.posproyect.ui.components.TituloSeccion
import com.devnoway.posproyect.ui.viewmodel.CatalogoViewModel

/**
 * Inicio / catálogo.
 *
 * Los productos salen de Room (se pueden ver sin internet); el botón ⟳ los
 * actualiza desde la Web API cuando hay conexión. Los chips y el buscador
 * filtran en memoria lo que ya está guardado.
 */
@Composable
fun CatalogoScreen(
    contenedor: ContenedorApp,
    onAbrirProducto: (Int) -> Unit,
    onNavegar: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CatalogoViewModel = viewModel(factory = CatalogoViewModel.fabrica(contenedor))
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    MarcoCliente(
        titulo = "Acambapan",
        rutaActual = Rutas.CATALOGO,
        onNavegar = onNavegar,
        modifier = modifier,
        acciones = {
            IndicadorConexion(
                enLinea = estado.enLinea,
                sincronizando = estado.sincronizando,
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
            CampoTexto(
                valor = estado.busqueda,
                onValorCambio = viewModel::cambiarBusqueda,
                etiqueta = "Buscar pan, pastel o bebida"
            )

            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChipCategoria(
                    texto = "Todos",
                    seleccionado = estado.categoriaId == null,
                    onClick = { viewModel.seleccionarCategoria(null) }
                )
                estado.categorias.forEach { categoria ->
                    ChipCategoria(
                        texto = categoria.nombre,
                        seleccionado = estado.categoriaId == categoria.id,
                        onClick = { viewModel.seleccionarCategoria(categoria.id) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            PromocionEnvio()

            estado.mensaje?.let { mensaje ->
                Spacer(Modifier.height(10.dp))
                BandaMensaje(
                    mensaje = mensaje,
                    esError = true,
                    onCerrar = viewModel::limpiarMensaje
                )
            }

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TituloSeccion("Nuestro catálogo")
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${estado.visibles.size} artículos",
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
                items(estado.visibles, key = { it.id }) { producto ->
                    TarjetaProducto(
                        producto = producto,
                        onClick = { onAbrirProducto(producto.id) }
                    )
                }
                if (estado.visibles.isEmpty() && !estado.cargando) {
                    item {
                        Text(
                            text = if (estado.enLinea) {
                                "No hay productos que coincidan con tu búsqueda."
                            } else {
                                "Todavía no hay catálogo guardado. Conéctate a internet y toca ⟳."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp)
                        )
                    }
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
                    text = "En pedidos mayores a $250 dentro de Acámbaro",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
