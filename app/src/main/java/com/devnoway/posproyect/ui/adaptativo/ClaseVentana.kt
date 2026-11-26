package com.devnoway.posproyect.ui.adaptativo

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Clases de tamaño de ventana de Acambapan.
 *
 * Se usan los puntos de corte de Material 3 para decidir la experiencia:
 *
 *  - COMPACTA  → menos de 600 dp  (teléfono vertical)
 *  - MEDIANA   → 600 a 839 dp     (teléfono horizontal, tableta pequeña)
 *  - EXPANDIDA → 840 dp o más     (tableta, ventana grande, escritorio)
 *
 * Se calcula una sola vez arriba del árbol de composición con
 * [ProveedorClaseVentana] y las pantallas la leen con [LocalClaseVentana], de
 * modo que no hay que ir pasando el ancho por parámetro a cada composable.
 */
enum class ClaseVentana {
    COMPACTA,
    MEDIANA,
    EXPANDIDA;

    /** true cuando conviene usar navegación lateral en lugar de barra inferior. */
    val usaNavegacionLateral: Boolean get() = this != COMPACTA

    /** Ancho máximo del contenido para que no se estire de más en pantallas grandes. */
    val anchoMaximoContenido: Dp
        get() = when (this) {
            COMPACTA -> Dp.Unspecified
            MEDIANA -> 820.dp
            EXPANDIDA -> 1080.dp
        }

    /** Columnas del catálogo: una en teléfono, dos en tableta y tres en ventana grande. */
    val columnasCatalogo: Int
        get() = when (this) {
            COMPACTA -> 1
            MEDIANA -> 2
            EXPANDIDA -> 3
        }
}

/** Puntos de corte de Material 3, en dp. */
object PuntosCorte {
    const val MEDIANA = 600
    const val EXPANDIDA = 840
}

/** Clase activa. Por defecto COMPACTA para que una Preview suelta siga viéndose bien. */
val LocalClaseVentana = staticCompositionLocalOf { ClaseVentana.COMPACTA }

/** Calcula la clase a partir del ancho disponible. */
fun claseParaAncho(anchoDp: Float): ClaseVentana = when {
    anchoDp < PuntosCorte.MEDIANA -> ClaseVentana.COMPACTA
    anchoDp < PuntosCorte.EXPANDIDA -> ClaseVentana.MEDIANA
    else -> ClaseVentana.EXPANDIDA
}

/**
 * Mide la ventana y publica la clase de tamaño para todo lo que va adentro.
 *
 * [BoxWithConstraints] da el tamaño en dp que realmente ocupan las pantallas de
 * la app, así que también funciona en pantalla dividida o en una ventana
 * flotante, no solo con el tamaño físico del dispositivo.
 */
@Composable
fun ProveedorClaseVentana(
    modifier: Modifier = Modifier,
    contenido: @Composable () -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        val clase = claseParaAncho(maxWidth.value)
        CompositionLocalProvider(LocalClaseVentana provides clase) {
            contenido()
        }
    }
}
