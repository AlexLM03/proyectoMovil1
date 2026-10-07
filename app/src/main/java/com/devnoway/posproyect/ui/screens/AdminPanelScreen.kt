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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devnoway.posproyect.data.DatosMock
import com.devnoway.posproyect.data.enPesos
import com.devnoway.posproyect.navigation.Rutas
import com.devnoway.posproyect.ui.components.BotonSecundario
import com.devnoway.posproyect.ui.components.FilaNavegacion
import com.devnoway.posproyect.ui.components.MarcoAdmin
import com.devnoway.posproyect.ui.components.TarjetaIndicador
import com.devnoway.posproyect.ui.components.TituloSeccion
import com.devnoway.posproyect.ui.theme.AcambapanTheme

/**
 * Panel principal del administrador: resumen rapido y accesos a las
 * pantallas de trabajo (dash, pedidos e inventario).
 */
@Composable
fun AdminPanelScreen(
    onNavegar: (String) -> Unit,
    onVolverATienda: () -> Unit,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    MarcoAdmin(
        titulo = "Panel principal",
        rutaActual = Rutas.ADMIN_PANEL,
        onNavegar = onNavegar,
        onAtras = onVolverATienda,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Panadería Acambapan",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Sucursal Acambay · turno de la mañana",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaIndicador(
                    titulo = "Pedidos de hoy",
                    valor = DatosMock.pedidosDelDia.toString(),
                    modifier = Modifier.weight(1f)
                )
                TarjetaIndicador(
                    titulo = "Ventas de hoy",
                    valor = DatosMock.ventasDelDia.enPesos(),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(4.dp))
            TituloSeccion("Accesos rápidos")
            FilaNavegacion(
                emoji = "\uD83D\uDCCA",
                titulo = "Dash administrativo",
                subtitulo = "Indicadores y ventas del día",
                onClick = { onNavegar(Rutas.ADMIN_DASH) }
            )
            FilaNavegacion(
                emoji = "\uD83D\uDCCB",
                titulo = "Gestión de pedidos",
                subtitulo = "Pedidos pendientes, en camino y entregados",
                onClick = { onNavegar(Rutas.ADMIN_PEDIDOS) }
            )
            FilaNavegacion(
                emoji = "\uD83D\uDCE6",
                titulo = "Inventario",
                subtitulo = "Insumos y existencias de la panadería",
                onClick = { onNavegar(Rutas.ADMIN_INVENTARIO) }
            )

            Spacer(Modifier.height(4.dp))
            TituloSeccion("Tienda")
            FilaNavegacion(
                emoji = "\uD83D\uDED2",
                titulo = "Ver catálogo como cliente",
                subtitulo = "Regresa a la tienda de la app",
                onClick = onVolverATienda
            )

            Spacer(Modifier.height(8.dp))
            BotonSecundario(texto = "Cerrar sesión de administrador", onClick = onCerrarSesion)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminPanelScreenPreview() {
    AcambapanTheme {
        AdminPanelScreen(onNavegar = {}, onVolverATienda = {}, onCerrarSesion = {})
    }
}
