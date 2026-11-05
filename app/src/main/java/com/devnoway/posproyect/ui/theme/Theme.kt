package com.devnoway.posproyect.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Esquema de color de Acambapan: base blanca sobre crema.
 *
 * Se usa un solo esquema (claro) para que la marca se vea igual en
 * cualquier telefono; el modo oscuro queda pendiente.
 */
private val EsquemaClaro = lightColorScheme(
    primary = Cafe,
    onPrimary = Blanco,
    primaryContainer = CremaOscura,
    onPrimaryContainer = Cafe,
    secondary = Canela,
    onSecondary = Blanco,
    secondaryContainer = Crema,
    onSecondaryContainer = Cafe,
    tertiary = Terracota,
    onTertiary = Blanco,
    background = Blanco,
    onBackground = Cafe,
    surface = Blanco,
    onSurface = Cafe,
    surfaceVariant = Crema,
    onSurfaceVariant = CafeSuave,
    outline = CremaOscura,
    outlineVariant = CremaOscura,
    error = RojoAlerta,
    onError = Blanco
)

@Composable
fun AcambapanTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaClaro,
        typography = Typography,
        content = content
    )
}
