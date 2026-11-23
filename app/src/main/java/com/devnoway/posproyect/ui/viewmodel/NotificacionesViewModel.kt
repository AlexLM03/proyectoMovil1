package com.devnoway.posproyect.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.devnoway.posproyect.data.modelo.Notificacion
import com.devnoway.posproyect.data.modelo.Usuario
import com.devnoway.posproyect.data.remote.Resultado
import com.devnoway.posproyect.data.repository.NotificacionRepository
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
 * ViewModel de la bandeja de notificaciones.
 *
 * Lista los avisos del cliente, marca los leídos (en Room y en el servidor) y
 * permite actualizar la bandeja cuando hay conexión.
 */
class NotificacionesViewModel(
    private val sesionRepository: SesionRepository,
    private val notificacionRepository: NotificacionRepository
) : ViewModel() {

    data class Estado(
        val usuario: Usuario? = null,
        val notificaciones: List<Notificacion> = emptyList(),
        val enLinea: Boolean = true,
        val cargando: Boolean = false,
        val mensaje: String? = null
    ) {
        val noLeidas: Int get() = notificaciones.count { !it.leida }
    }

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
                    notificacionRepository.deUsuario(usuario.id).collect { lista ->
                        _estado.update { it.copy(notificaciones = lista, cargando = false) }
                    }
                }
        }
        viewModelScope.launch {
            notificacionRepository.enLinea.collect { enLinea ->
                _estado.update { it.copy(enLinea = enLinea) }
            }
        }
        sincronizar()
    }

    fun sincronizar() {
        val usuario = _estado.value.usuario ?: return
        if (_estado.value.cargando) return
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true) }
            val resultado = notificacionRepository.sincronizar(usuario.id)
            _estado.update {
                it.copy(
                    cargando = false,
                    mensaje = (resultado as? Resultado.Fallo)?.mensaje
                )
            }
        }
    }

    fun marcarLeida(id: Int) {
        viewModelScope.launch { notificacionRepository.marcarLeida(id) }
    }

    fun marcarTodasLeidas() {
        val pendientes = _estado.value.notificaciones.filter { !it.leida }
        if (pendientes.isEmpty()) return
        viewModelScope.launch {
            pendientes.forEach { aviso -> notificacionRepository.marcarLeida(aviso.id) }
            _estado.update { it.copy(mensaje = "Marcaste ${pendientes.size} aviso(s) como leídos.") }
        }
    }

    fun limpiarMensaje() {
        _estado.update { it.copy(mensaje = null) }
    }

    companion object {
        fun fabrica(contenedor: ContenedorApp): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                NotificacionesViewModel(
                    sesionRepository = contenedor.sesionRepository,
                    notificacionRepository = contenedor.notificacionRepository
                )
            }
        }
    }
}
