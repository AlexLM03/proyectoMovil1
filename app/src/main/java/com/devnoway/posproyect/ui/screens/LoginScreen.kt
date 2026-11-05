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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devnoway.posproyect.data.modelo.Usuario
import com.devnoway.posproyect.di.ContenedorApp
import com.devnoway.posproyect.ui.components.BandaMensaje
import com.devnoway.posproyect.ui.components.BotonPrincipal
import com.devnoway.posproyect.ui.components.BotonSecundario
import com.devnoway.posproyect.ui.components.BotonTexto
import com.devnoway.posproyect.ui.components.CampoTexto
import com.devnoway.posproyect.ui.components.MarcaAcambapan
import com.devnoway.posproyect.ui.viewmodel.LoginViewModel

/**
 * Pantalla de inicio de sesión.
 *
 * Valida contra la Web API (`POST /auth/login`) y guarda el token. Si la cuenta
 * es de administrador entra al panel; si es cliente, al catálogo. También se
 * puede entrar como invitado (sin sesión, solo catálogo local).
 */
@Composable
fun LoginScreen(
    contenedor: ContenedorApp,
    onEntrarCliente: () -> Unit,
    onEntrarAdmin: () -> Unit,
    onCrearCuenta: () -> Unit,
    onInvitado: () -> Unit,
    modifier: Modifier = Modifier,
    entrarAutomatico: Boolean = true,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.fabrica(contenedor))
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val sesion by viewModel.sesion.collectAsStateWithLifecycle()
    val recienEntrado by viewModel.recienEntrado.collectAsStateWithLifecycle()

    // Sesión que ya estaba guardada al abrir la pantalla: se entra sin pedir la
    // contraseña, salvo que se haya abierto el login a propósito (candado del
    // panel de administrador).
    LaunchedEffect(Unit) {
        val actual: Usuario = sesion ?: return@LaunchedEffect
        if (!entrarAutomatico) return@LaunchedEffect
        if (actual.esAdmin) onEntrarAdmin() else onEntrarCliente()
    }

    // Login recién hecho (desde aquí o desde el candado): se navega según el rol.
    LaunchedEffect(recienEntrado) {
        val usuario: Usuario = recienEntrado ?: return@LaunchedEffect
        viewModel.consumirEntrada()
        if (usuario.esAdmin) onEntrarAdmin() else onEntrarCliente()
    }

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
            Spacer(Modifier.height(28.dp))

            CampoTexto(
                valor = estado.correo,
                onValorCambio = viewModel::cambiarCorreo,
                etiqueta = "Correo o teléfono",
                tipoTeclado = KeyboardType.Email
            )
            Spacer(Modifier.height(12.dp))
            CampoTexto(
                valor = estado.contrasena,
                onValorCambio = viewModel::cambiarContrasena,
                etiqueta = "Contraseña",
                esContrasena = true
            )

            estado.error?.let { mensaje ->
                Spacer(Modifier.height(12.dp))
                BandaMensaje(
                    mensaje = mensaje,
                    esError = true,
                    onCerrar = viewModel::limpiarError
                )
            }

            Spacer(Modifier.height(20.dp))
            BotonPrincipal(
                texto = if (estado.cargando) "Entrando…" else "Iniciar sesión",
                onClick = { viewModel.entrar() },
                habilitado = !estado.cargando
            )
            Spacer(Modifier.height(10.dp))
            BotonSecundario(texto = "Crear cuenta", onClick = onCrearCuenta)
            Spacer(Modifier.height(4.dp))
            BotonTexto(texto = "Entrar como invitado", onClick = onInvitado)

            Spacer(Modifier.height(28.dp))
            Text(
                text = "Panel de la panadería",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            BotonTexto(
                texto = "Acceso administrador",
                onClick = { viewModel.entrar(rolRequerido = "ADMIN") }
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Demo · admin@pantradicion.com / 123456\n" +
                    "Cliente · s23120227@alumnos.itsur.edu.mx / 123456",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}
