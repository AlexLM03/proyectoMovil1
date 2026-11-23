package com.devnoway.posproyect.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.devnoway.posproyect.data.modelo.MetodoPago
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
 * ViewModel de los métodos de pago.
 *
 * La app no guarda números de tarjeta: solo se elige la forma de pago que se
 * propondrá al confirmar el pedido (efectivo, terminal o transferencia) y se
 * guarda en el perfil del servidor.
 */
class MetodosPagoViewModel(private val sesionRepository: SesionRepository) : ViewModel() {

    data class Estado(
        val usuario: Usuario? = null,
        val guardando: Boolean = false,
        val mensaje: String? = null,
        val error: String? = null
    ) {
        val seleccionado: MetodoPago get() = usuario?.metodoPagoPreferido ?: MetodoPago.EFECTIVO
    }

    private val _estado = MutableStateFlow(Estado())
    val estado: StateFlow<Estado> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            sesionRepository.sesion.collect { usuario ->
                _estado.update { it.copy(usuario = usuario) }
            }
        }
    }

    fun elegir(metodo: MetodoPago) {
        if (_estado.value.guardando) return
        if (_estado.value.usuario?.metodoPagoPreferido == metodo) return
        viewModelScope.launch {
            _estado.update { it.copy(guardando = true, mensaje = null, error = null) }
            when (val resultado = sesionRepository.cambiarMetodoPago(metodo)) {
                is Resultado.Exito -> _estado.update {
                    it.copy(
                        guardando = false,
                        mensaje = "Método de pago: ${metodo.etiqueta}."
                    )
                }

                is Resultado.Fallo -> _estado.update {
                    it.copy(guardando = false, error = resultado.mensaje)
                }
            }
        }
    }

    fun limpiarMensajes() {
        _estado.update { it.copy(mensaje = null, error = null) }
    }

    companion object {
        fun fabrica(contenedor: ContenedorApp): ViewModelProvider.Factory = viewModelFactory {
            initializer { MetodosPagoViewModel(contenedor.sesionRepository) }
        }
    }
}
