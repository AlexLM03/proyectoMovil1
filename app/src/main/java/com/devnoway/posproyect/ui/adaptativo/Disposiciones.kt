package com.devnoway.posproyect.ui.adaptativo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Dos columnas en pantalla ancha, una sola en teléfono vertical.
 *
 * En horizontal (mediana y expandida) el contenido se reparte en dos paneles
 * lado a lado, cada uno con su propio scroll; en compacta se apila en una
 * columna respetando el mismo orden de lectura.
 *
 * [pesoIzquierda] y [pesoDerecha] permiten dar más espacio a un panel (por
 * ejemplo 1.2 para la lista y 1.0 para el resumen de pago).
 */
@Composable
fun DosColumnas(
    modifier: Modifier = Modifier,
    pesoIzquierda: Float = 1f,
    pesoDerecha: Float = 1f,
    separacion: Int = 20,
    izquierda: @Composable ColumnScope.() -> Unit,
    derecha: @Composable ColumnScope.() -> Unit
) {
    val clase = LocalClaseVentana.current

    if (!clase.usaNavegacionLateral) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(separacion.dp)
        ) {
            izquierda()
            derecha()
        }
    } else {
        Row(
            modifier = modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(separacion.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(pesoIzquierda)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = izquierda
            )
            Column(
                modifier = Modifier
                    .weight(pesoDerecha)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = derecha
            )
        }
    }
}
