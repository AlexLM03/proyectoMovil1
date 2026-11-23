package com.devnoway.posproyect.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.devnoway.posproyect.data.modelo.Pedido
import com.devnoway.posproyect.data.modelo.Usuario
import com.devnoway.posproyect.data.remote.Resultado
import com.devnoway.posproyect.data.repository.NotificacionRepository
import com.devnoway.posproyect.data.repository.PedidoRepository
import com.devnoway.posproyect.data.repository.SesionRepository
import com.devnoway.posproyect.di.ContenedorApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** ViewModel del perfil del cliente: sus datos, su historial y sus avisos. */
class PerfilViewModel(
    private val sesionRepository: SesionRepository,
    private val pedidoRepository: PedidoRepository,
    private val notificacionRepository: NotificacionRepository
) : ViewModel() {

    data class Estado(
        val usuario: Usuario? = null,
        val pedidos: List<Pedido> = emptyList(),
        val noLeidas: Int = 0,
        val enLinea: Boolean = true,
        val sincronizando: Boolean = false,
        val mensaje: String? = null
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
            pedidoRepository.pedidos.collect { pedidos ->
                _estado.update { it.copy(pedidos = pedidos) }
            }
        }
        viewModelScope.launch {
            notificacionRepository.noLeidas.collect { cantidad ->
                _estado.update { it.copy(noLeidas = cantidad) }
            }
        }
        viewModelScope.launch {
            pedidoRepository.enLinea.collect { enLinea ->
                _estado.update { it.copy(enLinea = enLinea) }
            }
        }
        sincronizar()
    }

    /** Trae pedidos y avisos del servidor (silencioso si no hay red). */
    fun sincronizar() {
        if (_estado.value.sincronizando) return
        val usuario = _estado.value.usuario
        if (usuario == null) {
            _estado.update {
                it.copy(mensaje = "Inicia sesión para sincronizar tu historial de pedidos.")
            }
            return
        }
        viewModelScope.launch {
            _estado.update { it.copy(sincronizando = true, mensaje = null) }
            val resultado = pedidoRepository.sincronizar(
                clienteId = usuario.id,
                esAdmin = usuario.esAdmin
            )
            notificacionRepository.sincronizar(usuario.id)
            _estado.update {
                it.copy(
                    sincronizando = false,
                    mensaje = (resultado as? Resultado.Fallo)?.mensaje
                )
            }
        }
    }

    fun cerrarSesion() = sesionRepository.cerrarSesion()

    fun limpiarMensaje() {
        _estado.update { it.copy(mensaje = null) }
    }

    companion object {
        fun fabrica(contenedor: ContenedorApp): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PerfilViewModel(
                    sesionRepository = contenedor.sesionRepository,
                    pedidoRepository = contenedor.pedidoRepository,
                    notificacionRepository = contenedor.notificacionRepository
                )
            }
        }
    }
}
