package com.devnoway.posproyect.ui.screens

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devnoway.posproyect.R
import com.devnoway.posproyect.data.modelo.Direccion
import com.devnoway.posproyect.di.ContenedorApp
import com.devnoway.posproyect.navigation.Rutas
import com.devnoway.posproyect.ui.components.BandaMensaje
import com.devnoway.posproyect.ui.components.BotonPrincipal
import com.devnoway.posproyect.ui.components.BotonTexto
import com.devnoway.posproyect.ui.components.CampoTexto
import com.devnoway.posproyect.ui.components.EtiquetaEstado
import com.devnoway.posproyect.ui.components.MarcoCliente
import com.devnoway.posproyect.ui.components.TituloSeccion
import com.devnoway.posproyect.ui.theme.VerdeExito
import com.devnoway.posproyect.ui.viewmodel.DireccionesViewModel

/**
 * Direcciones de entrega del cliente.
 *
 * Lista, agrega, edita, elimina y marca la predeterminada. La lista se puede
 * consultar sin conexión; para guardar cambios se necesita conexión.
 */
@Composable
fun DireccionesScreen(
    contenedor: ContenedorApp,
    onAtras: () -> Unit,
    onNavegar: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DireccionesViewModel = viewModel(
        factory = DireccionesViewModel.fabrica(contenedor)
    )
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var enEdicion by remember { mutableStateOf<Direccion?>(null) }
    var creando by remember { mutableStateOf(false) }
    var confirmandoBorrado by remember { mutableStateOf<Direccion?>(null) }

    MarcoCliente(
        titulo = stringResource(R.string.dir_titulo),
        rutaActual = Rutas.PERFIL,
        onNavegar = onNavegar,
        onAtras = onAtras,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            if (estado.usuario == null) {
                BandaMensaje(mensaje = stringResource(R.string.dir_sin_sesion_guardar))
                Spacer(Modifier.height(12.dp))
            }

            estado.mensaje?.let { mensaje ->
                BandaMensaje(mensaje = mensaje, onCerrar = viewModel::limpiarMensajes)
                Spacer(Modifier.height(10.dp))
            }
            estado.error?.let { mensaje ->
                BandaMensaje(
                    mensaje = mensaje,
                    esError = true,
                    onCerrar = viewModel::limpiarMensajes
                )
                Spacer(Modifier.height(10.dp))
            }

            BotonPrincipal(
                texto = stringResource(R.string.dir_agregar),
                onClick = { creando = true },
                habilitado = estado.usuario != null && !estado.trabajando
            )

            Spacer(Modifier.height(16.dp))
            TituloSeccion(stringResource(R.string.dir_mis_direcciones))
            Spacer(Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(estado.direcciones, key = { it.id }) { direccion ->
                    TarjetaDireccion(
                        direccion = direccion,
                        habilitado = !estado.trabajando,
                        onPredeterminar = { viewModel.marcarPredeterminada(direccion) },
                        onEditar = { enEdicion = direccion },
                        onEliminar = { confirmandoBorrado = direccion }
                    )
                }
                if (estado.direcciones.isEmpty() && !estado.cargando) {
                    item {
                        Text(
                            text = if (estado.usuario == null) {
                                stringResource(R.string.dir_sin_sesion_ver)
                            } else {
                                stringResource(R.string.dir_vacio)
                            },
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
        DialogoDireccion(
            direccion = null,
            onGuardar = { nueva ->
                viewModel.guardar(nueva)
                creando = false
            },
            onCerrar = { creando = false }
        )
    }

    enEdicion?.let { direccion ->
        DialogoDireccion(
            direccion = direccion,
            onGuardar = { editada ->
                viewModel.guardar(editada)
                enEdicion = null
            },
            onCerrar = { enEdicion = null }
        )
    }

    confirmandoBorrado?.let { direccion ->
        AlertDialog(
            onDismissRequest = { confirmandoBorrado = null },
            title = { Text(stringResource(R.string.dir_eliminar_titulo)) },
            text = {
                Text(
                    stringResource(R.string.dir_confirmar_borrado, direccion.alias)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminar(direccion.id)
                        confirmandoBorrado = null
                    }
                ) { Text(stringResource(R.string.dir_eliminar)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmandoBorrado = null }) {
                    Text(stringResource(R.string.dir_cancelar))
                }
            }
        )
    }
}

@Composable
private fun TarjetaDireccion(
    direccion: Direccion,
    habilitado: Boolean,
    onPredeterminar: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = direccion.alias,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (direccion.predeterminada) {
                    EtiquetaEstado(
                        texto = stringResource(R.string.dir_predeterminada),
                        color = VerdeExito
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(text = direccion.calle, style = MaterialTheme.typography.bodyMedium)
            direccion.referencias?.takeIf { it.isNotBlank() }?.let { referencia ->
                Text(
                    text = referencia,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (!direccion.predeterminada) {
                    BotonTexto(
                        texto = stringResource(R.string.dir_predeterminada),
                        onClick = { if (habilitado) onPredeterminar() }
                    )
                }
                BotonTexto(
                    texto = stringResource(R.string.dir_editar),
                    onClick = { if (habilitado) onEditar() }
                )
                BotonTexto(
                    texto = stringResource(R.string.dir_eliminar),
                    onClick = { if (habilitado) onEliminar() }
                )
            }
        }
    }
}

@Composable
private fun DialogoDireccion(
    direccion: Direccion?,
    onGuardar: (Direccion) -> Unit,
    onCerrar: () -> Unit
) {
    var alias by remember(direccion) { mutableStateOf(direccion?.alias.orEmpty()) }
    var calle by remember(direccion) { mutableStateOf(direccion?.calle.orEmpty()) }
    var referencias by remember(direccion) { mutableStateOf(direccion?.referencias.orEmpty()) }
    var predeterminada by remember(direccion) {
        mutableStateOf(direccion?.predeterminada ?: (direccion == null))
    }
    var error by remember { mutableStateOf<String?>(null) }
    val textoErrorCampos = stringResource(R.string.dir_error_campos)

    AlertDialog(
        onDismissRequest = onCerrar,
        title = {
            Text(
                if (direccion == null) {
                    stringResource(R.string.dir_agregar)
                } else {
                    stringResource(R.string.dir_editar_titulo)
                }
            )
        },
        text = {
            Column {
                CampoTexto(
                    alias,
                    { alias = it },
                    stringResource(R.string.dir_alias_etiqueta)
                )
                Spacer(Modifier.height(8.dp))
                CampoTexto(
                    calle,
                    { calle = it },
                    stringResource(R.string.dir_calle_etiqueta),
                    lineas = 2
                )
                Spacer(Modifier.height(8.dp))
                CampoTexto(
                    valor = referencias,
                    onValorCambio = { referencias = it },
                    etiqueta = stringResource(R.string.dir_referencias_etiqueta),
                    lineas = 2
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.dir_usar_predeterminada),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(checked = predeterminada, onCheckedChange = { predeterminada = it })
                }
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
                    if (alias.isBlank() || calle.isBlank()) {
                        error = textoErrorCampos
                        return@TextButton
                    }
                    onGuardar(
                        Direccion(
                            id = direccion?.id ?: 0,
                            usuarioId = direccion?.usuarioId ?: 0,
                            alias = alias.trim(),
                            calle = calle.trim(),
                            referencias = referencias.trim().ifBlank { null },
                            predeterminada = predeterminada
                        )
                    )
                }
            ) { Text(stringResource(R.string.dir_guardar)) }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) { Text(stringResource(R.string.dir_cancelar)) }
        }
    )
}
