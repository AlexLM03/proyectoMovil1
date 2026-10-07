package com.devnoway.posproyect.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devnoway.posproyect.data.DatosMock
import com.devnoway.posproyect.navigation.Rutas
import com.devnoway.posproyect.ui.components.BotonSecundario
import com.devnoway.posproyect.ui.components.DialogoPendiente
import com.devnoway.posproyect.ui.components.FilaDato
import com.devnoway.posproyect.ui.components.FilaNavegacion
import com.devnoway.posproyect.ui.components.LogoAcambapan
import com.devnoway.posproyect.ui.components.MarcoCliente
import com.devnoway.posproyect.ui.components.TituloSeccion
import com.devnoway.posproyect.ui.theme.AcambapanTheme

/**
 * Perfil y configuracion del cliente.
 *
 * "Mis pedidos" y "Modo administrador" navegan; las demas opciones muestran
 * un aviso de que siguen pendientes.
 */
@Composable
fun PerfilScreen(
    onMisPedidos: () -> Unit,
    onModoAdministrador: () -> Unit,
    onCerrarSesion: () -> Unit,
    onNavegar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var dialogoPendiente by remember { mutableStateOf<String?>(null) }
    val usuario = DatosMock.usuario

    MarcoCliente(
        titulo = "Mi perfil",
        rutaActual = Rutas.PERFIL,
        onNavegar = onNavegar,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LogoAcambapan(tamano = 64.dp)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = usuario.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Cliente registrado",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            TituloSeccion("Mis datos")
            Column {
                FilaDato("Correo", usuario.correo)
                FilaDato("Teléfono", usuario.telefono)
                FilaDato("Dirección", usuario.direccion)
            }

            Spacer(Modifier.height(4.dp))
            TituloSeccion("Ajustes")
            FilaNavegacion(
                emoji = "\uD83E\uDDFE",
                titulo = "Mis pedidos",
                subtitulo = "Historial y seguimiento",
                onClick = onMisPedidos
            )
            FilaNavegacion(
                emoji = "\uD83D\uDCCD",
                titulo = "Direcciones de entrega",
                subtitulo = "Agregar o cambiar direcciones",
                onClick = { dialogoPendiente = "Direcciones de entrega" }
            )
            FilaNavegacion(
                emoji = "\uD83D\uDCB3",
                titulo = "Métodos de pago",
                subtitulo = "Tarjetas y pago en efectivo",
                onClick = { dialogoPendiente = "Métodos de pago" }
            )
            FilaNavegacion(
                emoji = "\uD83D\uDD14",
                titulo = "Notificaciones",
                subtitulo = "Avisos del estado del pedido",
                onClick = { dialogoPendiente = "Notificaciones" }
            )
            FilaNavegacion(
                emoji = "\u2699",
                titulo = "Modo administrador",
                subtitulo = "Panel de la panadería",
                onClick = onModoAdministrador
            )
            FilaNavegacion(
                emoji = "\u2753",
                titulo = "Ayuda y soporte",
                subtitulo = "Preguntas frecuentes",
                onClick = { dialogoPendiente = "Ayuda y soporte" }
            )

            Spacer(Modifier.height(8.dp))
            BotonSecundario(texto = "Cerrar sesión", onClick = onCerrarSesion)
            Text(
                text = "Acambapan · versión 1.0 (maqueta)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    dialogoPendiente?.let { opcion ->
        DialogoPendiente(
            titulo = opcion,
            mensaje = "Esta sección todavía no está programada: es parte de la maqueta de pantallas.",
            onCerrar = { dialogoPendiente = null }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PerfilScreenPreview() {
    AcambapanTheme {
        PerfilScreen(
            onMisPedidos = {},
            onModoAdministrador = {},
            onCerrarSesion = {},
            onNavegar = {}
        )
    }
}
