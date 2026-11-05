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
import com.devnoway.posproyect.data.Insumo
import com.devnoway.posproyect.navigation.Rutas
import com.devnoway.posproyect.ui.components.BotonPrincipal
import com.devnoway.posproyect.ui.components.DialogoPendiente
import com.devnoway.posproyect.ui.components.EtiquetaEstado
import com.devnoway.posproyect.ui.components.MarcoAdmin
import com.devnoway.posproyect.ui.components.TarjetaIndicador
import com.devnoway.posproyect.ui.theme.AcambapanTheme
import com.devnoway.posproyect.ui.theme.RojoAlerta
import com.devnoway.posproyect.ui.theme.VerdeExito

/**
 * Inventario de la panaderia: insumos y existencias.
 *
 * Solo lectura por ahora; agregar o editar insumos avisa que sigue pendiente.
 */
@Composable
fun InventarioScreen(
    onNavegar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var dialogoPendiente by remember { mutableStateOf<String?>(null) }
    val insumos = DatosMock.inventario
    val bajos = insumos.count { it.bajo }

    MarcoAdmin(
        titulo = "Inventario",
        rutaActual = Rutas.ADMIN_INVENTARIO,
        onNavegar = onNavegar,
        modifier = modifier
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
                    valor = insumos.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                TarjetaIndicador(
                    titulo = "Bajos de stock",
                    valor = bajos.toString(),
                    modifier = Modifier.weight(1f),
                    colorValor = RojoAlerta
                )
            }

            Spacer(Modifier.height(12.dp))
            BotonPrincipal(
                texto = "Agregar insumo",
                onClick = { dialogoPendiente = "Agregar insumo" }
            )

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
                items(insumos, key = { it.nombre }) { insumo ->
                    TarjetaInsumo(
                        insumo = insumo,
                        onClick = { dialogoPendiente = "Editar ${insumo.nombre}" }
                    )
                }
            }
        }
    }

    dialogoPendiente?.let { opcion ->
        DialogoPendiente(
            titulo = opcion,
            mensaje = "El inventario todavía es de solo lectura: esta acción se conectará a la base de datos más adelante.",
            onCerrar = { dialogoPendiente = null }
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
                    text = "${insumo.stock} ${insumo.unidad} · mínimo ${insumo.minimo}",
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

@Preview(showBackground = true)
@Composable
fun InventarioScreenPreview() {
    AcambapanTheme {
        InventarioScreen(onNavegar = {})
    }
}
