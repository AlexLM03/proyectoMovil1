package com.devnoway.posproyect.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.devnoway.posproyect.ui.components.BotonPrincipal
import com.devnoway.posproyect.ui.components.BotonSecundario
import com.devnoway.posproyect.ui.components.BotonTexto
import com.devnoway.posproyect.ui.components.CampoTexto
import com.devnoway.posproyect.ui.components.MarcaAcambapan
import com.devnoway.posproyect.ui.theme.AcambapanTheme

/**
 * Pantalla de inicio: iniciar sesion, crear cuenta o entrar como invitado.
 * Los botones todavia no validan nada, solo llevan a la siguiente pantalla.
 */
@Composable
fun LoginScreen(
    onIniciarSesion: () -> Unit,
    onCrearCuenta: () -> Unit,
    onInvitado: () -> Unit,
    onAccesoAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(24.dp))
            MarcaAcambapan(tamanoLogo = 130.dp)
            Spacer(Modifier.height(32.dp))

            CampoTexto(
                valor = correo,
                onValorCambio = { correo = it },
                etiqueta = "Correo o teléfono",
                tipoTeclado = KeyboardType.Email
            )
            Spacer(Modifier.height(12.dp))
            CampoTexto(
                valor = contrasena,
                onValorCambio = { contrasena = it },
                etiqueta = "Contraseña",
                esContrasena = true
            )

            Spacer(Modifier.height(24.dp))
            BotonPrincipal(texto = "Iniciar sesión", onClick = onIniciarSesion)
            Spacer(Modifier.height(10.dp))
            BotonSecundario(texto = "Crear cuenta", onClick = onCrearCuenta)
            Spacer(Modifier.height(4.dp))
            BotonTexto(texto = "Entrar como invitado", onClick = onInvitado)

            Spacer(Modifier.height(32.dp))
            Text(
                text = "Panel de la panadería",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            BotonTexto(texto = "Acceso administrador", onClick = onAccesoAdmin)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    AcambapanTheme {
        LoginScreen(
            onIniciarSesion = {},
            onCrearCuenta = {},
            onInvitado = {},
            onAccesoAdmin = {}
        )
    }
}
