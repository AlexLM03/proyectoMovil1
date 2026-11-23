package com.devnoway.posproyect.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.devnoway.posproyect.data.modelo.Usuario
import com.devnoway.posproyect.data.remote.Resultado
import com.devnoway.posproyect.data.repository.SesionRepository
import com.devnoway.posproyect.di.ContenedorApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel del inicio de sesión y del acceso administrador.
 *
 * La pantalla solo dibuja el estado; toda la validación y la llamada a la API
 * viven aquí. Cuando la sesión cambia, `sesion` lo notifica y la pantalla
 * navega a la siguiente sección según el rol.
 */
class LoginViewModel(private val sesionRepository: SesionRepository) : ViewModel() {

    data class Estado(
        val correo: String = "",
        val contrasena: String = "",
        val cargando: Boolean = false,
        val error: String? = null
    )

    private val _estado = MutableStateFlow(Estado())
    val estado: StateFlow<Estado> = _estado.asStateFlow()

    /**
     * Usuario que acaba de iniciar sesión en esta pantalla. La pantalla lo
     * consume para navegar; así funciona igual desde el login normal y desde
     * el candado del panel de administrador.
     */
    private val _recienEntrado = MutableStateFlow<Usuario?>(null)
    val recienEntrado: StateFlow<Usuario?> = _recienEntrado.asStateFlow()

    val sesion: StateFlow<Usuario?> = sesionRepository.sesion

    fun cambiarCorreo(valor: String) {
        _estado.update { it.copy(correo = valor, error = null) }
    }

    fun cambiarContrasena(valor: String) {
        _estado.update { it.copy(contrasena = valor, error = null) }
    }

    fun limpiarError() {
        _estado.update { it.copy(error = null) }
    }

    /**
     * @param rolRequerido "ADMIN" para el acceso al panel de la panadería.
     */
    fun entrar(rolRequerido: String? = null) {
        val actual = _estado.value
        if (actual.cargando) return
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            when (val resultado = sesionRepository.iniciarSesion(
                correo = actual.correo,
                contrasena = actual.contrasena,
                rolRequerido = rolRequerido
            )) {
                is Resultado.Exito -> {
                    _estado.update { it.copy(cargando = false, contrasena = "") }
                    _recienEntrado.value = resultado.datos
                }

                is Resultado.Fallo -> _estado.update {
                    it.copy(cargando = false, error = resultado.mensaje)
                }
            }
        }
    }

    fun cerrarSesion() = sesionRepository.cerrarSesion()

    /** Marca la navegación como atendida para no repetirla al recomponer. */
    fun consumirEntrada() {
        _recienEntrado.value = null
    }

    companion object {
        fun fabrica(contenedor: ContenedorApp): ViewModelProvider.Factory = viewModelFactory {
            initializer { LoginViewModel(contenedor.sesionRepository) }
        }
    }
}
