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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devnoway.posproyect.BuildConfig
import com.devnoway.posproyect.R
import com.devnoway.posproyect.navigation.Rutas
import com.devnoway.posproyect.ui.components.BotonSecundario
import com.devnoway.posproyect.ui.components.MarcoCliente
import com.devnoway.posproyect.ui.components.TituloSeccion

/** Ayuda y soporte: preguntas frecuentes, contacto directo y datos de la app. */
@Composable
fun AyudaScreen(
    onAtras: () -> Unit,
    onNavegar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current
    var abierta by remember { mutableStateOf<Int?>(0) }

    val asuntoCorreo = stringResource(R.string.ayu_asunto_correo)
    val preguntas = listOf(
        stringResource(R.string.ayu_faq_pedido_pregunta) to
            stringResource(R.string.ayu_faq_pedido_respuesta),
        stringResource(R.string.ayu_faq_sin_internet_pregunta) to
            stringResource(R.string.ayu_faq_sin_internet_respuesta),
        stringResource(R.string.ayu_faq_seguimiento_pregunta) to
            stringResource(R.string.ayu_faq_seguimiento_respuesta),
        stringResource(R.string.ayu_faq_pagos_pregunta) to
            stringResource(R.string.ayu_faq_pagos_respuesta),
        stringResource(R.string.ayu_faq_envio_pregunta) to
            stringResource(R.string.ayu_faq_envio_respuesta),
        stringResource(R.string.ayu_faq_direccion_pregunta) to
            stringResource(R.string.ayu_faq_direccion_respuesta),
        stringResource(R.string.ayu_faq_no_llega_pregunta) to
            stringResource(R.string.ayu_faq_no_llega_respuesta)
    )

    MarcoCliente(
        titulo = stringResource(R.string.ayu_titulo),
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
                text = stringResource(R.string.ayu_encabezado),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            TituloSeccion(stringResource(R.string.ayu_faq_titulo))
            preguntas.forEachIndexed { indice, (pregunta, respuesta) ->
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
            TituloSeccion(stringResource(R.string.ayu_contacto_titulo))
            Text(
                text = stringResource(R.string.ayu_contacto_direccion),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            BotonSecundario(
                texto = stringResource(R.string.ayu_boton_llamar),
                onClick = {
                    contexto.startActivity(
                        Intent(Intent.ACTION_DIAL, Uri.parse("tel:4450000001"))
                    )
                }
            )
            BotonSecundario(
                texto = stringResource(R.string.ayu_boton_correo),
                onClick = {
                    val correo = Intent(
                        Intent.ACTION_SENDTO,
                        Uri.parse("mailto:soporte@acambapan.devnoway.com")
                    ).apply {
                        putExtra(Intent.EXTRA_SUBJECT, asuntoCorreo)
                    }
                    contexto.startActivity(correo)
                }
            )

            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(
                    R.string.ayu_pie_version,
                    BuildConfig.VERSION_NAME,
                    BuildConfig.API_BASE_URL
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}
