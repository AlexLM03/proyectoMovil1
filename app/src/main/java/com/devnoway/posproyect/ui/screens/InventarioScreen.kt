package com.devnoway.posproyect.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devnoway.posproyect.data.modelo.Insumo
import com.devnoway.posproyect.data.modelo.enCantidad
import com.devnoway.posproyect.di.ContenedorApp
import com.devnoway.posproyect.navigation.Rutas
import com.devnoway.posproyect.ui.components.BandaMensaje
import com.devnoway.posproyect.ui.components.BotonPrincipal
import com.devnoway.posproyect.ui.components.CampoTexto
import com.devnoway.posproyect.ui.components.EtiquetaEstado
import com.devnoway.posproyect.ui.components.IndicadorConexion
import com.devnoway.posproyect.ui.components.MarcoAdmin
import com.devnoway.posproyect.ui.components.TarjetaIndicador
import com.devnoway.posproyect.ui.theme.RojoAlerta
import com.devnoway.posproyect.ui.theme.VerdeExito
import com.devnoway.posproyect.ui.viewmodel.InventarioViewModel

/**
 * Inventario de insumos: listar, agregar, editar y eliminar.
 *
 * La lectura sale de Room (se consulta sin conexión); guardar o eliminar exige
 * conexión porque el inventario es compartido con el servidor.
 */
@Composable
fun InventarioScreen(
    contenedor: ContenedorApp,
    onNavegar: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InventarioViewModel = viewModel(
        factory = InventarioViewModel.fabrica(contenedor)
    )
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var insumoEnEdicion by remember { mutableStateOf<Insumo?>(null) }
    var creando by remember { mutableStateOf(false) }

    MarcoAdmin(
        titulo = "Inventario",
        rutaActual = Rutas.ADMIN_INVENTARIO,
        onNavegar = onNavegar,
        modifier = modifier,
        acciones = {
            IndicadorConexion(
                enLinea = estado.enLinea,
                sincronizando = estado.guardando,
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
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaIndicador(
                    titulo = "Insumos registrados",
                    valor = estado.insumos.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                TarjetaIndicador(
                    titulo = "Bajos de stock",
                    valor = estado.bajos.toString(),
                    modifier = Modifier.weight(1f),
                    colorValor = RojoAlerta
                )
            }

            estado.mensaje?.let { mensaje ->
                Spacer(Modifier.height(10.dp))
                BandaMensaje(mensaje = mensaje, onCerrar = viewModel::limpiarMensajes)
            }
            estado.error?.let { mensaje ->
                Spacer(Modifier.height(10.dp))
                BandaMensaje(
                    mensaje = mensaje,
                    esError = true,
                    onCerrar = viewModel::limpiarMensajes
                )
            }

            Spacer(Modifier.height(12.dp))
            BotonPrincipal(texto = "Agregar insumo", onClick = { creando = true })

            Spacer(Modifier.height(12.dp))
            Text(
                text = "Existencias",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(estado.insumos, key = { it.id }) { insumo ->
                    TarjetaInsumo(
                        insumo = insumo,
                        onClick = { insumoEnEdicion = insumo }
                    )
                }
                if (estado.insumos.isEmpty()) {
                    item {
                        Text(
                            text = "No hay insumos guardados. Toca ⟳ para traerlos del servidor.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                }
            }
        }
    }

    if (creando) {
        DialogoInsumo(
            insumo = null,
            onGuardar = { insumo ->
                viewModel.guardar(insumo)
                creando = false
            },
            onEliminar = null,
            onCerrar = { creando = false }
        )
    }

    insumoEnEdicion?.let { insumo ->
        DialogoInsumo(
            insumo = insumo,
            onGuardar = { editado ->
                viewModel.guardar(editado)
                insumoEnEdicion = null
            },
            onEliminar = {
                viewModel.eliminar(insumo.id)
                insumoEnEdicion = null
            },
            onCerrar = { insumoEnEdicion = null }
        )
    }
}

@Composable
private fun TarjetaInsumo(insumo: Insumo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = insumo.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${insumo.stock.enCantidad()} ${insumo.unidad} · mínimo ${insumo.minimo.enCantidad()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            EtiquetaEstado(
                texto = if (insumo.bajo) "Bajo" else "Suficiente",
                color = if (insumo.bajo) RojoAlerta else VerdeExito
            )
        }
    }
}

/** Alta o edición de un insumo. `insumo = null` significa "nuevo". */
@Composable
private fun DialogoInsumo(
    insumo: Insumo?,
    onGuardar: (Insumo) -> Unit,
    onEliminar: (() -> Unit)?,
    onCerrar: () -> Unit
) {
    var nombre by remember(insumo) { mutableStateOf(insumo?.nombre.orEmpty()) }
    var stock by remember(insumo) { mutableStateOf(insumo?.stock?.enCantidad().orEmpty()) }
    var minimo by remember(insumo) { mutableStateOf(insumo?.minimo?.enCantidad().orEmpty()) }
    var unidad by remember(insumo) { mutableStateOf(insumo?.unidad.orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(if (insumo == null) "Agregar insumo" else "Editar insumo") },
        text = {
            Column {
                CampoTexto(nombre, { nombre = it }, "Nombre")
                Spacer(Modifier.height(8.dp))
                CampoTexto(
                    valor = stock,
                    onValorCambio = { stock = it },
                    etiqueta = "Existencias",
                    tipoTeclado = KeyboardType.Decimal
                )
                Spacer(Modifier.height(8.dp))
                CampoTexto(
                    valor = minimo,
                    onValorCambio = { minimo = it },
                    etiqueta = "Mínimo",
                    tipoTeclado = KeyboardType.Decimal
                )
                Spacer(Modifier.height(8.dp))
                CampoTexto(unidad, { unidad = it }, "Unidad (kg, piezas, litros)")
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val stockNumero = stock.replace(',', '.').toDoubleOrNull()
                    val minimoNumero = minimo.replace(',', '.').toDoubleOrNull()
                    if (nombre.isBlank() || unidad.isBlank() ||
                        stockNumero == null || minimoNumero == null
                    ) {
                        error = "Escribe el nombre, la unidad y cantidades válidas."
                        return@TextButton
                    }
                    onGuardar(
                        Insumo(
                            id = insumo?.id ?: 0,
                            nombre = nombre.trim(),
                            stock = stockNumero,
                            minimo = minimoNumero,
                            unidad = unidad.trim()
                        )
                    )
                }
            ) { Text("Guardar") }
        },
        dismissButton = {
            Row {
                if (onEliminar != null) {
                    TextButton(onClick = onEliminar) { Text("Eliminar") }
                }
                TextButton(onClick = onCerrar) { Text("Cancelar") }
            }
        }
    )
}
