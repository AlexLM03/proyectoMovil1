package com.devnoway.posproyect.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devnoway.posproyect.R
import com.devnoway.posproyect.data.modelo.enPesos
import com.devnoway.posproyect.di.ContenedorApp
import com.devnoway.posproyect.navigation.Rutas
import com.devnoway.posproyect.ui.adaptativo.DosColumnas
import com.devnoway.posproyect.ui.components.BotonPrincipal
import com.devnoway.posproyect.ui.components.BotonSecundario
import com.devnoway.posproyect.ui.components.EtiquetaEstado
import com.devnoway.posproyect.ui.components.FilaDato
import com.devnoway.posproyect.ui.components.MarcoCliente
import com.devnoway.posproyect.ui.components.RecuadroEmoji
import com.devnoway.posproyect.ui.viewmodel.DetalleProductoViewModel

/**
 * Detalle del producto elegido en el catálogo.
 *
 * Recibe el id por la ruta (`detalle_producto/{productoId}`), lo lee de Room y
 * refresca la ficha desde la API. "Agregar al carrito" guarda la línea en el
 * carrito compartido y abre el carrito.
 */
@Composable
fun DetalleProductoScreen(
    contenedor: ContenedorApp,
    productoId: Int,
    onAgregarAlCarrito: () -> Unit,
    onAtras: () -> Unit,
    onNavegar: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetalleProductoViewModel = viewModel(
        factory = DetalleProductoViewModel.fabrica(contenedor, productoId)
    )
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var cantidad by remember { mutableIntStateOf(1) }
    val producto = estado.producto

    MarcoCliente(
        titulo = stringResource(R.string.det_titulo),
        rutaActual = Rutas.CATALOGO,
        onNavegar = onNavegar,
        onAtras = onAtras,
        modifier = modifier
    ) {
        if (producto == null) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (estado.cargando) {
                        stringResource(R.string.det_cargando)
                    } else {
                        stringResource(R.string.det_no_guardado)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                BotonSecundario(
                    texto = stringResource(R.string.det_volver_catalogo),
                    onClick = onAtras
                )
            }
        } else {
            // En horizontal, ficha a la izquierda y compra a la derecha.
            DosColumnas(
                modifier = Modifier
                    .weight(1f)
                    .padding(20.dp),
                pesoIzquierda = 1.1f,
                izquierda = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        RecuadroEmoji(emoji = producto.emoji, tamano = 140.dp)
                    }

                    Text(
                        text = producto.nombre,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (producto.categoria.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            EtiquetaEstado(
                                texto = producto.categoria,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    Text(
                        text = producto.precio.enPesos(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = producto.descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Horneada del día: el dato ya viene de la API.
                    estado.tanda?.let { tanda ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.det_tanda_titulo),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                FilaDato(
                                    etiqueta = stringResource(R.string.det_tanda_hora_etiqueta),
                                    valor = stringResource(
                                        R.string.det_tanda_hora,
                                        tanda.horaSalida.take(5)
                                    )
                                )
                                FilaDato(
                                    etiqueta = stringResource(R.string.det_tanda_piezas_etiqueta),
                                    valor = stringResource(
                                        R.string.det_tanda_piezas,
                                        tanda.piezasDisponibles
                                    )
                                )
                            }
                        }
                    }
                },
                derecha = {
                    Text(
                        text = stringResource(R.string.det_cantidad),
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
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            FilaDato(
                                etiqueta = stringResource(R.string.det_disponibilidad),
                                valor = if (producto.disponible) {
                                    stringResource(R.string.det_disponible_hoy)
                                } else {
                                    stringResource(R.string.det_agotado)
                                }
                            )
                            FilaDato(
                                etiqueta = stringResource(R.string.det_total),
                                valor = (producto.precio * cantidad).enPesos()
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    BotonPrincipal(
                        texto = stringResource(R.string.det_agregar_carrito),
                        onClick = {
                            viewModel.agregarAlCarrito(cantidad)
                            onAgregarAlCarrito()
                        },
                        habilitado = producto.disponible
                    )
                    BotonSecundario(
                        texto = stringResource(R.string.det_seguir_comprando),
                        onClick = onAtras
                    )
                }
            )
        }
    }
}
