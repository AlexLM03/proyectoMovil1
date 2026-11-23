package com.devnoway.posproyect.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.devnoway.posproyect.data.modelo.LineaCarrito
import com.devnoway.posproyect.data.modelo.MetodoPago
import com.devnoway.posproyect.data.modelo.NuevoPedido
import com.devnoway.posproyect.data.remote.Resultado
import com.devnoway.posproyect.data.repository.CarritoRepository
import com.devnoway.posproyect.data.repository.DireccionRepository
import com.devnoway.posproyect.data.repository.PedidoRepository
import com.devnoway.posproyect.data.repository.SesionRepository
import com.devnoway.posproyect.di.ContenedorApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel del carrito y del pago.
 *
 * Al confirmar, el pedido se guarda en Room y se intenta enviar. Si no hay red,
 * el pedido queda pendiente y el seguimiento lo muestra así: nunca se pierde.
 */
class CarritoViewModel(
    private val carritoRepository: CarritoRepository,
    private val pedidoRepository: PedidoRepository,
    private val sesionRepository: SesionRepository,
    private val direccionRepository: DireccionRepository
) : ViewModel() {

    data class Estado(
        val lineas: List<LineaCarrito> = emptyList(),
        val aDomicilio: Boolean = true,
        val direccion: String = "",
        val metodoPago: MetodoPago = MetodoPago.EFECTIVO,
        val confirmando: Boolean = false,
        val folioCreado: String? = null,
        val error: String? = null
    ) {
        val subtotal: Double get() = lineas.sumOf { it.subtotal }
        val costoEnvio: Double get() = PedidoRepository.costoEnvioPara(subtotal, aDomicilio)
        val total: Double get() = subtotal + costoEnvio
        val piezas: Int get() = lineas.sumOf { it.cantidad }
        val vacio: Boolean get() = lineas.isEmpty()
    }

    private val _estado = MutableStateFlow(Estado())
    val estado: StateFlow<Estado> = _estado.asStateFlow()

    init {
        val usuario = sesionRepository.sesion.value
        _estado.update {
            it.copy(
                direccion = usuario?.direccion.orEmpty(),
                metodoPago = usuario?.metodoPagoPreferido ?: MetodoPago.EFECTIVO
            )
        }
        viewModelScope.launch {
            carritoRepository.lineas.collect { lineas ->
                _estado.update { it.copy(lineas = lineas) }
            }
        }
        // Si el cliente ya guardó una dirección predeterminada, se propone esa.
        viewModelScope.launch {
            val usuarioId = usuario?.id ?: return@launch
            val predeterminada = direccionRepository.predeterminada(usuarioId)
            if (predeterminada != null) {
                _estado.update {
                    it.copy(direccion = predeterminada.calle)
                }
            }
        }
    }

    fun cambiarCantidad(productoId: Int, cantidad: Int) {
        carritoRepository.cambiarCantidad(productoId, cantidad)
    }

    fun quitar(productoId: Int) {
        carritoRepository.quitar(productoId)
    }

    fun cambiarEntrega(aDomicilio: Boolean) {
        _estado.update { it.copy(aDomicilio = aDomicilio) }
    }

    fun cambiarDireccion(valor: String) {
        _estado.update { it.copy(direccion = valor) }
    }

    fun cambiarMetodoPago(metodo: MetodoPago) {
        _estado.update { it.copy(metodoPago = metodo) }
    }

    fun limpiarError() {
        _estado.update { it.copy(error = null) }
    }

    fun limpiarNavegacion() {
        _estado.update { it.copy(folioCreado = null) }
    }

    /** Crea el pedido: primero en local, después en el servidor si hay red. */
    fun confirmarPedido() {
        val actual = _estado.value
        if (actual.confirmando || actual.vacio) return
        if (actual.aDomicilio && actual.direccion.isBlank()) {
            _estado.update { it.copy(error = "Escribe la dirección de entrega.") }
            return
        }

        val usuario = sesionRepository.sesion.value
        val nuevoPedido = NuevoPedido(
            clienteId = usuario?.id,
            clienteNombre = usuario?.nombre ?: "Cliente invitado",
            clienteTelefono = usuario?.telefono.orEmpty(),
            aDomicilio = actual.aDomicilio,
            direccion = if (actual.aDomicilio) actual.direccion else null,
            metodoPago = actual.metodoPago,
            lineas = actual.lineas
        )

        viewModelScope.launch {
            _estado.update { it.copy(confirmando = true, error = null) }
            when (val resultado = pedidoRepository.crear(nuevoPedido)) {
                is Resultado.Exito -> {
                    carritoRepository.vaciar()
                    _estado.update { it.copy(confirmando = false, folioCreado = resultado.datos) }
                }

                is Resultado.Fallo -> _estado.update {
                    it.copy(confirmando = false, error = resultado.mensaje)
                }
            }
        }
    }

    companion object {
        fun fabrica(contenedor: ContenedorApp): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                CarritoViewModel(
                    carritoRepository = contenedor.carritoRepository,
                    pedidoRepository = contenedor.pedidoRepository,
                    sesionRepository = contenedor.sesionRepository,
                    direccionRepository = contenedor.direccionRepository
                )
            }
        }
    }
}
