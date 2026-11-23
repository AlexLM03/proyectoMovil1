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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.devnoway.posproyect.di.ContenedorApp
import com.devnoway.posproyect.ui.components.BandaMensaje
import com.devnoway.posproyect.ui.components.BarraSuperior
import com.devnoway.posproyect.ui.components.BotonPrincipal
import com.devnoway.posproyect.ui.components.BotonTexto
import com.devnoway.posproyect.ui.components.CampoTexto
import com.devnoway.posproyect.ui.components.MarcaAcambapan
import com.devnoway.posproyect.ui.viewmodel.RegistroViewModel

/**
 * Registro de un cliente nuevo.
 *
 * `POST /auth/registro` crea la cuenta en MySQL, guarda la sesión y entra al
 * catálogo. Si falta conexión se avisa: el registro necesita red.
 */
@Composable
fun RegistroScreen(
    contenedor: ContenedorApp,
    onRegistrado: () -> Unit,
    onVolverLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegistroViewModel = viewModel(factory = RegistroViewModel.fabrica(contenedor))
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val sesion by viewModel.sesion.collectAsStateWithLifecycle()

    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    LaunchedEffect(sesion) {
        if (sesion != null) onRegistrado()
    }

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
            CampoTexto(
                correo,
                { correo = it },
                "Correo electrónico",
                tipoTeclado = KeyboardType.Email
            )
            CampoTexto(telefono, { telefono = it }, "Teléfono", tipoTeclado = KeyboardType.Phone)
            CampoTexto(direccion, { direccion = it }, "Dirección de entrega", lineas = 2)
            CampoTexto(contrasena, { contrasena = it }, "Contraseña", esContrasena = true)

            estado.error?.let { mensaje ->
                BandaMensaje(
                    mensaje = mensaje,
                    esError = true,
                    onCerrar = viewModel::limpiarError
                )
            }

            Spacer(Modifier.height(4.dp))
            BotonPrincipal(
                texto = if (estado.cargando) "Creando cuenta…" else "Crear mi cuenta",
                onClick = {
                    viewModel.registrar(
                        nombre = nombre,
                        correo = correo,
                        telefono = telefono,
                        direccion = direccion,
                        contrasena = contrasena
                    )
                },
                habilitado = !estado.cargando
            )
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
