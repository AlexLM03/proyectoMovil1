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

/** ViewModel del registro de clientes nuevos. */
class RegistroViewModel(private val sesionRepository: SesionRepository) : ViewModel() {

    data class Estado(
        val cargando: Boolean = false,
        val error: String? = null
    )

    private val _estado = MutableStateFlow(Estado())
    val estado: StateFlow<Estado> = _estado.asStateFlow()

    val sesion: StateFlow<Usuario?> = sesionRepository.sesion

    fun registrar(
        nombre: String,
        correo: String,
        telefono: String,
        direccion: String,
        contrasena: String
    ) {
        if (_estado.value.cargando) return
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            when (val resultado = sesionRepository.registrar(
                nombre = nombre,
                correo = correo,
                telefono = telefono,
                direccion = direccion,
                contrasena = contrasena
            )) {
                is Resultado.Exito -> _estado.update { it.copy(cargando = false) }
                is Resultado.Fallo -> _estado.update {
                    it.copy(cargando = false, error = resultado.mensaje)
                }
            }
        }
    }

    fun limpiarError() {
        _estado.update { it.copy(error = null) }
    }

    companion object {
        fun fabrica(contenedor: ContenedorApp): ViewModelProvider.Factory = viewModelFactory {
            initializer { RegistroViewModel(contenedor.sesionRepository) }
        }
    }
}
