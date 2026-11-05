package com.devnoway.posproyect.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.devnoway.posproyect.data.modelo.EstadoPedido
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
 * ViewModel de la gestión de pedidos del administrador: filtros por estado y
 * cambio de estado (que se refleja en el servidor y en el teléfono).
 */
class PedidosAdminViewModel(private val pedidoRepository: PedidoRepository) : ViewModel() {

    data class Estado(
        val pedidos: List<Pedido> = emptyList(),
        val filtro: EstadoPedido? = null,
        val enLinea: Boolean = true,
        val trabajando: Boolean = false,
        val mensaje: String? = null
    ) {
        val visibles: List<Pedido>
            get() = filtro?.let { estado -> pedidos.filter { it.estado == estado } } ?: pedidos
    }

    private val _estado = MutableStateFlow(Estado())
    val estado: StateFlow<Estado> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            pedidoRepository.pedidos.collect { pedidos ->
                _estado.update { it.copy(pedidos = pedidos) }
            }
        }
        viewModelScope.launch {
            pedidoRepository.enLinea.collect { enLinea ->
                _estado.update { it.copy(enLinea = enLinea) }
            }
        }
        sincronizar()
    }

    fun filtrar(estado: EstadoPedido?) {
        _estado.update { it.copy(filtro = estado) }
    }

    fun sincronizar() {
        if (_estado.value.trabajando) return
        viewModelScope.launch {
            _estado.update { it.copy(trabajando = true, mensaje = null) }
            val resultado = pedidoRepository.sincronizar(clienteId = null, esAdmin = true)
            _estado.update {
                it.copy(
                    trabajando = false,
                    mensaje = (resultado as? Resultado.Fallo)?.mensaje
                )
            }
        }
    }

    /** Mueve el pedido al siguiente estado y avisa del resultado. */
    fun cambiarEstado(folio: String, estado: EstadoPedido) {
        if (_estado.value.trabajando) return
        viewModelScope.launch {
            _estado.update { it.copy(trabajando = true, mensaje = null) }
            val resultado = pedidoRepository.cambiarEstado(folio, estado)
            _estado.update {
                it.copy(
                    trabajando = false,
                    mensaje = when (resultado) {
                        is Resultado.Fallo -> resultado.mensaje
                        is Resultado.Exito -> "Pedido $folio actualizado a ${estado.etiqueta}."
                    }
                )
            }
        }
    }

    fun limpiarMensaje() {
        _estado.update { it.copy(mensaje = null) }
    }

    companion object {
        fun fabrica(contenedor: ContenedorApp): ViewModelProvider.Factory = viewModelFactory {
            initializer { PedidosAdminViewModel(contenedor.pedidoRepository) }
        }
    }
}
