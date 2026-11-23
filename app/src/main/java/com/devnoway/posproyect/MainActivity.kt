package com.devnoway.posproyect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.devnoway.posproyect.navigation.AcambapanApp
import com.devnoway.posproyect.ui.theme.AcambapanTheme

/**
 * Acambapan: ventas en línea de una panadería.
 *
 * La Activity solo toma el contenedor de dependencias de la Application y
 * monta la interfaz. Toda la lógica (sesión, catálogo, pedidos) vive en los
 * ViewModel y los repositorios.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val contenedor = (application as AcambapanApplication).contenedor

        setContent {
            AcambapanTheme {
                AcambapanApp(contenedor = contenedor)
            }
        }
    }
}
