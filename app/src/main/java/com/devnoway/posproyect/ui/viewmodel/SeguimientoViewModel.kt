package com.devnoway.posproyect.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.devnoway.posproyect.data.modelo.Pedido
import com.devnoway.posproyect.data.remote.Resultado
import com.devnoway.posproyect.data.repository.PedidoRepository
import com.devnoway.posproyect.di.ContenedorApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel del seguimiento del pedido.
 *
 * Observa el pedido más reciente guardado y permite refrescarlo contra el
 * servidor o reintentar el envío si se creó sin conexión.
 */
class SeguimientoViewModel(private val pedidoRepository: PedidoRepository) : ViewModel() {

    data class Estado(
        val pedido: Pedido? = null,
        val enLinea: Boolean = true,
        val actualizando: Boolean = false,
        val mensaje: String? = null
    )

    private val _estado = MutableStateFlow(Estado())
    val estado: StateFlow<Estado> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            pedidoRepository.ultimoPedido.collect { pedido ->
                _estado.update { it.copy(pedido = pedido) }
            }
        }
        viewModelScope.launch {
            pedidoRepository.enLinea.collect { enLinea ->
                _estado.update { it.copy(enLinea = enLinea) }
            }
        }
    }

    /** Refresca el estado desde la API o reenvía el pedido si está pendiente. */
    fun refrescar() {
        val pedido = _estado.value.pedido ?: return
        if (_estado.value.actualizando) return
        viewModelScope.launch {
            _estado.update { it.copy(actualizando = true, mensaje = null) }
            val resultado = if (pedido.pendienteSincronizar) {
                pedidoRepository.enviarPendiente(pedido.folio)
            } else {
                pedidoRepository.refrescar(pedido.folio)
            }
            _estado.update {
                it.copy(
                    actualizando = false,
                    mensaje = (resultado as? Resultado.Fallo)?.mensaje
                )
            }
        }
    }

    fun limpiarMensaje() {
        _estado.update { it.copy(mensaje = null) }
    }

    companion object {
        fun fabrica(contenedor: ContenedorApp): ViewModelProvider.Factory = viewModelFactory {
            initializer { SeguimientoViewModel(contenedor.pedidoRepository) }
        }
    }
}
