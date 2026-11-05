package com.devnoway.posproyect.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devnoway.posproyect.ui.components.BarraSuperior
import com.devnoway.posproyect.ui.components.BotonPrincipal
import com.devnoway.posproyect.ui.components.BotonTexto
import com.devnoway.posproyect.ui.components.CampoTexto
import com.devnoway.posproyect.ui.components.MarcaAcambapan
import com.devnoway.posproyect.ui.theme.AcambapanTheme

/**
 * Registro de un cliente nuevo.
 * El boton "Crear mi cuenta" solo navega al catalogo: no guarda nada todavia.
 */
@Composable
fun RegistroScreen(
    onRegistrado: () -> Unit,
    onVolverLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BarraSuperior(titulo = "Crear cuenta", onAtras = onVolverLogin) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MarcaAcambapan(tamanoLogo = 90.dp, mostrarEslogan = false)
            Spacer(Modifier.height(4.dp))

            CampoTexto(nombre, { nombre = it }, "Nombre completo")
            CampoTexto(correo, { correo = it }, "Correo electrónico", tipoTeclado = KeyboardType.Email)
            CampoTexto(telefono, { telefono = it }, "Teléfono", tipoTeclado = KeyboardType.Phone)
            CampoTexto(direccion, { direccion = it }, "Dirección de entrega", lineas = 2)
            CampoTexto(contrasena, { contrasena = it }, "Contraseña", esContrasena = true)

            Spacer(Modifier.height(8.dp))
            BotonPrincipal(texto = "Crear mi cuenta", onClick = onRegistrado)
            BotonTexto(texto = "Ya tengo cuenta, iniciar sesión", onClick = onVolverLogin)

            Text(
                text = "Al crear tu cuenta aceptas los términos del servicio.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegistroScreenPreview() {
    AcambapanTheme {
        RegistroScreen(onRegistrado = {}, onVolverLogin = {})
    }
}
