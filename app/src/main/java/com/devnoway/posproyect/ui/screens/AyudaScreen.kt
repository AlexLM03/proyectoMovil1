package com.devnoway.posproyect.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devnoway.posproyect.BuildConfig
import com.devnoway.posproyect.navigation.Rutas
import com.devnoway.posproyect.ui.components.BotonSecundario
import com.devnoway.posproyect.ui.components.MarcoCliente
import com.devnoway.posproyect.ui.components.TituloSeccion

/** Preguntas frecuentes de la panadería. */
private val PREGUNTAS = listOf(
    "¿Cómo hago un pedido?" to
        "Elige productos en el catálogo, agrégalos al carrito y confirma. Recibirás un folio para seguir tu pedido.",
    "¿Puedo pedir sin internet?" to
        "Sí. El pedido se guarda en el teléfono y se envía a la panadería en cuanto vuelve la conexión.",
    "¿Cómo sigo mi pedido?" to
        "En la pestaña Pedido verás el avance: recibido, en preparación, en camino y entregado. Toca ⟳ para actualizar.",
    "¿Qué formas de pago aceptan?" to
        "Efectivo al recibir, tarjeta con la terminal del repartidor y transferencia SPEI. Se eligen en Perfil → Métodos de pago.",
    "¿Cuánto cuesta el envío?" to
        "El envío a domicilio cuesta $30 dentro de Acámbaro y es gratis en pedidos de $250 o más. Recoger en la panadería no tiene costo.",
    "¿Cómo cambio mi dirección?" to
        "En Perfil → Direcciones de entrega puedes agregar, editar y marcar tu dirección predeterminada.",
    "¿Qué hago si mi pedido no llega?" to
        "Llama a la panadería o escríbenos; con tu folio podemos revisar el estado y al repartidor asignado."
)

/**
 * Ayuda y soporte: preguntas frecuentes, contacto directo y datos de la app.
 */
@Composable
fun AyudaScreen(
    onAtras: () -> Unit,
    onNavegar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current
    var abierta by remember { mutableStateOf<Int?>(0) }

    MarcoCliente(
        titulo = "Ayuda y soporte",
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
                text = "¿En qué te ayudamos?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            TituloSeccion("Preguntas frecuentes")
            PREGUNTAS.forEachIndexed { indice, (pregunta, respuesta) ->
                val expandida = abierta == indice
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { abierta = if (expandida) null else indice },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = pregunta,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (expandida) "−" else "+",
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (expandida) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = respuesta,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
            TituloSeccion("Contacto")
            Text(
                text = "Panadería Acambapan · Av. Juárez 165, Acámbaro, Guanajuato\nLunes a domingo, 6:00 a 21:00",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            BotonSecundario(
                texto = "Llamar a la panadería",
                onClick = {
                    contexto.startActivity(
                        Intent(Intent.ACTION_DIAL, Uri.parse("tel:4450000001"))
                    )
                }
            )
            BotonSecundario(
                texto = "Escribir por correo",
                onClick = {
                    val correo = Intent(
                        Intent.ACTION_SENDTO,
                        Uri.parse("mailto:soporte@acambapan.devnoway.com")
                    ).apply {
                        putExtra(Intent.EXTRA_SUBJECT, "Ayuda con mi pedido")
                    }
                    contexto.startActivity(correo)
                }
            )

            Spacer(Modifier.height(8.dp))
            Text(
                text = "Acambapan ${BuildConfig.VERSION_NAME} · datos de ${BuildConfig.API_BASE_URL}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}
