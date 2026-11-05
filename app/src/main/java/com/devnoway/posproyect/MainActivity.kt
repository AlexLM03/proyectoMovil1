package com.devnoway.posproyect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.devnoway.posproyect.navigation.AcambapanApp
import com.devnoway.posproyect.ui.theme.AcambapanTheme

/**
 * Acambapan: ventas en linea de una panaderia.
 *
 * Esta es la maqueta de pantallas: todas las pantallas existen y se navega
 * entre ellas, pero todavia no hay base de datos ni API.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AcambapanTheme {
                AcambapanApp()
            }
        }
    }
}
