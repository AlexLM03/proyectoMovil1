package com.devnoway.posproyect.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.devnoway.posproyect.data.modelo.Producto
import com.devnoway.posproyect.data.remote.Resultado
import com.devnoway.posproyect.data.repository.CarritoRepository
import com.devnoway.posproyect.data.repository.CatalogoRepository
import com.devnoway.posproyect.di.ContenedorApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel del detalle de un producto.
 *
 * Muestra primero la copia local y luego intenta refrescar la ficha (y su
 * multimedia) desde el servidor, sin bloquear la pantalla. También agrega al
 * carrito compartido.
 */
class DetalleProductoViewModel(
    private val catalogoRepository: CatalogoRepository,
    private val carritoRepository: CarritoRepository,
    private val productoId: Int
) : ViewModel() {

    data class Estado(
        val producto: Producto? = null,
        val cargando: Boolean = true,
        val mensaje: String? = null
    )

    private val _estado = MutableStateFlow(Estado())
    val estado: StateFlow<Estado> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            catalogoRepository.producto(productoId).collect { producto ->
                _estado.update { it.copy(producto = producto, cargando = false) }
            }
        }
        viewModelScope.launch {
            val resultado = catalogoRepository.refrescarProducto(productoId)
            if (resultado is Resultado.Fallo) {
                _estado.update { it.copy(mensaje = resultado.mensaje) }
            }
        }
    }

    fun agregarAlCarrito(cantidad: Int) {
        val producto = _estado.value.producto ?: return
        carritoRepository.agregar(producto, cantidad)
    }

    fun limpiarMensaje() {
        _estado.update { it.copy(mensaje = null) }
    }

    companion object {
        fun fabrica(contenedor: ContenedorApp, productoId: Int): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    DetalleProductoViewModel(
                        catalogoRepository = contenedor.catalogoRepository,
                        carritoRepository = contenedor.carritoRepository,
                        productoId = productoId
                    )
                }
            }
    }
}
