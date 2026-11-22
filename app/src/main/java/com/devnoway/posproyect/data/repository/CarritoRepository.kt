package com.devnoway.posproyect.data.repository

import com.devnoway.posproyect.data.modelo.LineaCarrito
import com.devnoway.posproyect.data.modelo.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Carrito de compras en memoria.
 *
 * Vive en el contenedor de dependencias, así que el detalle del producto y el
 * carrito comparten el mismo estado. Al confirmar, sus líneas se convierten en
 * un pedido que se guarda en Room.
 */
class CarritoRepository {

    private val _lineas = MutableStateFlow<List<LineaCarrito>>(emptyList())
    val lineas: StateFlow<List<LineaCarrito>> = _lineas.asStateFlow()

    fun agregar(producto: Producto, cantidad: Int = 1) {
        if (cantidad <= 0) return
        _lineas.update { actuales ->
            val existente = actuales.firstOrNull { it.producto.id == producto.id }
            if (existente == null) {
                actuales + LineaCarrito(producto, cantidad)
            } else {
                actuales.map { linea ->
                    if (linea.producto.id == producto.id) {
                        linea.copy(cantidad = linea.cantidad + cantidad)
                    } else {
                        linea
                    }
                }
            }
        }
    }

    fun cambiarCantidad(productoId: Int, cantidad: Int) {
        if (cantidad <= 0) {
            quitar(productoId)
            return
        }
        _lineas.update { actuales ->
            actuales.map { linea ->
                if (linea.producto.id == productoId) linea.copy(cantidad = cantidad) else linea
            }
        }
    }

    fun quitar(productoId: Int) {
        _lineas.update { actuales -> actuales.filterNot { it.producto.id == productoId } }
    }

    fun vaciar() {
        _lineas.value = emptyList()
    }

    fun subtotal(): Double = _lineas.value.sumOf { it.subtotal }
}
