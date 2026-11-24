package com.devnoway.posproyect.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devnoway.posproyect.R
import com.devnoway.posproyect.data.modelo.MetodoPago
import com.devnoway.posproyect.di.ContenedorApp
import com.devnoway.posproyect.navigation.Rutas
import com.devnoway.posproyect.ui.components.BandaMensaje
import com.devnoway.posproyect.ui.components.MarcoCliente
import com.devnoway.posproyect.ui.components.TituloSeccion
import com.devnoway.posproyect.ui.descripcionRes
import com.devnoway.posproyect.ui.etiquetaRes
import com.devnoway.posproyect.ui.viewmodel.MetodosPagoViewModel

/**
 * Métodos de pago aceptados por la panadería.
 *
 * No se guardan datos de tarjetas: solo se elige la forma de pago preferida,
 * que se propone al confirmar el carrito y se guarda en el perfil.
 */
@Composable
fun MetodosPagoScreen(
    contenedor: ContenedorApp,
    onAtras: () -> Unit,
    onNavegar: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MetodosPagoViewModel = viewModel(
        factory = MetodosPagoViewModel.fabrica(contenedor)
    )
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    MarcoCliente(
        titulo = stringResource(R.string.pago_titulo),
        rutaActual = Rutas.PERFIL,
        onNavegar = onNavegar,
        onAtras = onAtras,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.pago_intro),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (estado.usuario == null) {
                BandaMensaje(
                    mensaje = stringResource(R.string.pago_sin_sesion),
                    esError = true
                )
            }

            estado.mensaje?.let { mensaje ->
                BandaMensaje(mensaje = mensaje, onCerrar = viewModel::limpiarMensajes)
            }
            estado.error?.let { mensaje ->
                BandaMensaje(
                    mensaje = mensaje,
                    esError = true,
                    onCerrar = viewModel::limpiarMensajes
                )
            }

            Spacer(Modifier.height(4.dp))
            TituloSeccion(stringResource(R.string.pago_formas))

            MetodoPago.entries.forEach { metodo ->
                val elegido = estado.seleccionado == metodo
                Card(
                    onClick = { viewModel.elegir(metodo) },
                    enabled = estado.usuario != null && !estado.guardando,
                    colors = CardDefaults.cardColors(
                        containerColor = if (elegido) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = elegido,
                            onClick = { viewModel.elegir(metodo) },
                            enabled = estado.usuario != null && !estado.guardando
                        )
                        Column(modifier = Modifier.padding(start = 6.dp)) {
                            Text(
                                text = stringResource(metodo.etiquetaRes),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = stringResource(metodo.descripcionRes),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = when (estado.seleccionado) {
                    MetodoPago.EFECTIVO ->
                        stringResource(R.string.pago_efectivo_detalle)
                    MetodoPago.TARJETA_TERMINAL ->
                        stringResource(R.string.pago_tarjeta_detalle)
                    MetodoPago.TRANSFERENCIA ->
                        stringResource(R.string.pago_transferencia_detalle)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
