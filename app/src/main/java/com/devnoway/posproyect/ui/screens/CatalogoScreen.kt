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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as itemsCuadricula
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devnoway.posproyect.R
import com.devnoway.posproyect.di.ContenedorApp
import com.devnoway.posproyect.navigation.Rutas
import com.devnoway.posproyect.ui.adaptativo.LocalClaseVentana
import com.devnoway.posproyect.ui.components.BandaMensaje
import com.devnoway.posproyect.ui.components.BotonTexto
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
    // En teléfono se ve una columna; en tableta, dos. Lo decide el ancho real.
    val columnas = LocalClaseVentana.current.columnasCatalogo

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
                etiqueta = stringResource(R.string.cat_buscar_etiqueta)
            )

            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChipCategoria(
                    texto = stringResource(R.string.cat_todos),
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

            estado.mensaje?.let { mensaje ->
                Spacer(Modifier.height(10.dp))
                BandaMensaje(
                    mensaje = mensaje,
                    esError = true,
                    onCerrar = viewModel::limpiarMensaje
                )
            }

            Spacer(Modifier.height(8.dp))
            if (columnas == 1) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        CabeceraCatalogo(
                            visibles = estado.visibles.size,
                            total = estado.productos.size,
                            filtrando = estado.busqueda.isNotBlank() ||
                                estado.categoriaId != null,
                            onLimpiar = viewModel::limpiarFiltros
                        )
                    }
                    items(estado.visibles, key = { it.id }) { producto ->
                        TarjetaProducto(
                            producto = producto,
                            onClick = { onAbrirProducto(producto.id) }
                        )
                    }
                    if (estado.visibles.isEmpty() && !estado.cargando) {
                        item { SinResultados(enLinea = estado.enLinea) }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columnas),
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        CabeceraCatalogo(
                            visibles = estado.visibles.size,
                            total = estado.productos.size,
                            filtrando = estado.busqueda.isNotBlank() ||
                                estado.categoriaId != null,
                            onLimpiar = viewModel::limpiarFiltros
                        )
                    }
                    itemsCuadricula(estado.visibles, key = { it.id }) { producto ->
                        TarjetaProducto(
                            producto = producto,
                            onClick = { onAbrirProducto(producto.id) }
                        )
                    }
                    if (estado.visibles.isEmpty() && !estado.cargando) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            SinResultados(enLinea = estado.enLinea)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Cabecera del catálogo que se desplaza con los productos.
 *
 * Antes la promoción y el título quedaban fijos y robaban alto a la lista,
 * sobre todo en horizontal. Ahora viajan dentro del listado y dejan libre la
 * pantalla para los productos.
 */
@Composable
private fun CabeceraCatalogo(
    visibles: Int,
    total: Int,
    filtrando: Boolean,
    onLimpiar: () -> Unit
) {
    PromocionEnvio()
    Spacer(Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TituloSeccion(stringResource(R.string.cat_titulo_catalogo))
        Spacer(Modifier.weight(1f))
        Text(
            text = if (filtrando) {
                stringResource(R.string.cat_contador_filtrado, visibles, total)
            } else {
                stringResource(R.string.cat_articulos, visibles)
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    if (filtrando) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            BotonTexto(
                texto = stringResource(R.string.cat_limpiar_busqueda),
                onClick = onLimpiar
            )
        }
    }
    Spacer(Modifier.height(4.dp))
}

/** Estado vacío del catálogo, compartido por la lista y la cuadrícula. */
@Composable
private fun SinResultados(enLinea: Boolean) {
    Text(
        text = if (enLinea) {
            stringResource(R.string.cat_sin_resultados)
        } else {
            stringResource(R.string.cat_sin_catalogo)
        },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp)
    )
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
                    text = stringResource(R.string.cat_envio_gratis),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.cat_envio_gratis_detalle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
