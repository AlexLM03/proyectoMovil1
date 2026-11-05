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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devnoway.posproyect.data.modelo.Notificacion
import com.devnoway.posproyect.data.modelo.fechaLegible
import com.devnoway.posproyect.di.ContenedorApp
import com.devnoway.posproyect.navigation.Rutas
import com.devnoway.posproyect.ui.components.BandaMensaje
import com.devnoway.posproyect.ui.components.BotonTexto
import com.devnoway.posproyect.ui.components.IndicadorConexion
import com.devnoway.posproyect.ui.components.MarcoCliente
import com.devnoway.posproyect.ui.components.TituloSeccion
import com.devnoway.posproyect.ui.viewmodel.NotificacionesViewModel

/** Bandeja de avisos del pedido (recibido, en camino, entregado…). */
@Composable
fun NotificacionesScreen(
    contenedor: ContenedorApp,
    onAtras: () -> Unit,
    onNavegar: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificacionesViewModel = viewModel(
        factory = NotificacionesViewModel.fabrica(contenedor)
    )
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    MarcoCliente(
        titulo = "Notificaciones",
        rutaActual = Rutas.PERFIL,
        onNavegar = onNavegar,
        onAtras = onAtras,
        modifier = modifier,
        acciones = {
            IndicadorConexion(
                enLinea = estado.enLinea,
                sincronizando = estado.cargando,
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

            if (estado.usuario == null) {
                BandaMensaje(mensaje = "Inicia sesión para ver tus avisos.")
                Spacer(Modifier.height(10.dp))
            }

            estado.mensaje?.let { mensaje ->
                BandaMensaje(mensaje = mensaje, onCerrar = viewModel::limpiarMensaje)
                Spacer(Modifier.height(10.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TituloSeccion("Tus avisos")
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${estado.noLeidas} sin leer",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (estado.noLeidas > 0) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    BotonTexto(
                        texto = "Marcar todas como leídas",
                        onClick = viewModel::marcarTodasLeidas
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(estado.notificaciones, key = { it.id }) { aviso ->
                    TarjetaAviso(aviso = aviso, onClick = { viewModel.marcarLeida(aviso.id) })
                }
                if (estado.notificaciones.isEmpty() && !estado.cargando) {
                    item {
                        Text(
                            text = if (estado.usuario == null) {
                                "Inicia sesión para ver tus avisos."
                            } else {
                                "No tienes avisos todavía. Aquí verás los cambios de tus pedidos."
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
}

@Composable
private fun TarjetaAviso(aviso: Notificacion, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (aviso.leida) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (!aviso.leida) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp, end = 10.dp)
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = aviso.titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (aviso.leida) FontWeight.Normal else FontWeight.Bold
                )
                Text(
                    text = aviso.mensaje,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (aviso.creadoEn.isNotBlank()) {
                    Text(
                        text = aviso.creadoEn.fechaLegible(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
