package com.devnoway.posproyect.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.devnoway.posproyect.data.modelo.Direccion
import com.devnoway.posproyect.data.modelo.Usuario
import com.devnoway.posproyect.data.remote.Resultado
import com.devnoway.posproyect.data.repository.DireccionRepository
import com.devnoway.posproyect.data.repository.SesionRepository
import com.devnoway.posproyect.di.ContenedorApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel de las direcciones de entrega.
 *
 * Lista, agrega, edita, elimina y marca la dirección predeterminada. La lista
 * sale de Room (se consulta sin conexión) y las escrituras van a la API.
 */
class DireccionesViewModel(
    private val sesionRepository: SesionRepository,
    private val direccionRepository: DireccionRepository
) : ViewModel() {

    data class Estado(
        val usuario: Usuario? = null,
        val direcciones: List<Direccion> = emptyList(),
        val cargando: Boolean = false,
        val trabajando: Boolean = false,
        val mensaje: String? = null,
        val error: String? = null
    )

    private val _estado = MutableStateFlow(Estado())
    val estado: StateFlow<Estado> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            sesionRepository.sesion.collect { usuario ->
                _estado.update { it.copy(usuario = usuario) }
            }
        }
        viewModelScope.launch {
            sesionRepository.sesion
                .filterNotNull()
                .distinctUntilChangedBy { it.id }
                .collectLatest { usuario ->
                    direccionRepository.direcciones(usuario.id).collect { lista ->
                        _estado.update { it.copy(direcciones = lista, cargando = false) }
                    }
                }
        }
        sincronizar()
    }

    fun sincronizar() {
        val usuario = _estado.value.usuario ?: return
        if (_estado.value.cargando) return
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true) }
            val resultado = direccionRepository.sincronizar(usuario.id)
            _estado.update {
                it.copy(
                    cargando = false,
                    mensaje = (resultado as? Resultado.Fallo)?.mensaje
                )
            }
        }
    }

    fun guardar(direccion: Direccion) {
        val usuario = _estado.value.usuario ?: return
        if (_estado.value.trabajando) return
        viewModelScope.launch {
            _estado.update { it.copy(trabajando = true, mensaje = null, error = null) }
            when (val resultado = direccionRepository.guardar(usuario.id, direccion)) {
                is Resultado.Exito -> _estado.update {
                    it.copy(trabajando = false, mensaje = "Dirección guardada.")
                }

                is Resultado.Fallo -> _estado.update {
                    it.copy(trabajando = false, error = resultado.mensaje)
                }
            }
        }
    }

    fun marcarPredeterminada(direccion: Direccion) {
        if (direccion.predeterminada) return
        guardar(direccion.copy(predeterminada = true))
    }

    fun eliminar(id: Int) {
        val usuario = _estado.value.usuario ?: return
        if (_estado.value.trabajando) return
        viewModelScope.launch {
            _estado.update { it.copy(trabajando = true, mensaje = null, error = null) }
            when (val resultado = direccionRepository.eliminar(usuario.id, id)) {
                is Resultado.Exito -> _estado.update {
                    it.copy(trabajando = false, mensaje = "Dirección eliminada.")
                }

                is Resultado.Fallo -> _estado.update {
                    it.copy(trabajando = false, error = resultado.mensaje)
                }
            }
        }
    }

    fun limpiarMensajes() {
        _estado.update { it.copy(mensaje = null, error = null) }
    }

    companion object {
        fun fabrica(contenedor: ContenedorApp): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                DireccionesViewModel(
                    sesionRepository = contenedor.sesionRepository,
                    direccionRepository = contenedor.direccionRepository
                )
            }
        }
    }
}
