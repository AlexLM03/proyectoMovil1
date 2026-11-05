package com.devnoway.posproyect.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.devnoway.posproyect.data.modelo.Insumo
import com.devnoway.posproyect.data.remote.Resultado
import com.devnoway.posproyect.data.repository.InsumoRepository
import com.devnoway.posproyect.di.ContenedorApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel del inventario de insumos.
 *
 * Permite listar, agregar, editar y eliminar. Las escrituras pasan por la API
 * (son datos que el administrador comparte con el servidor); la lectura sale de
 * Room y por eso el inventario se consulta sin conexión.
 */
class InventarioViewModel(private val insumoRepository: InsumoRepository) : ViewModel() {

    data class Estado(
        val insumos: List<Insumo> = emptyList(),
        val enLinea: Boolean = true,
        val guardando: Boolean = false,
        val mensaje: String? = null,
        val error: String? = null
    ) {
        val bajos: Int get() = insumos.count { it.bajo }
    }

    private val _estado = MutableStateFlow(Estado())
    val estado: StateFlow<Estado> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            insumoRepository.insumos.collect { insumos ->
                _estado.update { it.copy(insumos = insumos) }
            }
        }
        viewModelScope.launch {
            insumoRepository.enLinea.collect { enLinea ->
                _estado.update { it.copy(enLinea = enLinea) }
            }
        }
        sincronizar()
    }

    fun sincronizar() {
        if (_estado.value.guardando) return
        viewModelScope.launch {
            _estado.update { it.copy(guardando = true, mensaje = null) }
            val resultado = insumoRepository.sincronizar()
            _estado.update {
                it.copy(
                    guardando = false,
                    mensaje = (resultado as? Resultado.Fallo)?.mensaje
                )
            }
        }
    }

    /** Crea el insumo si `id` es 0; lo actualiza si ya existe. */
    fun guardar(insumo: Insumo) {
        if (_estado.value.guardando) return
        viewModelScope.launch {
            _estado.update { it.copy(guardando = true, mensaje = null, error = null) }
            when (val resultado = insumoRepository.guardar(insumo)) {
                is Resultado.Exito -> _estado.update {
                    it.copy(
                        guardando = false,
                        mensaje = "Insumo \"${resultado.datos.nombre}\" guardado."
                    )
                }

                is Resultado.Fallo -> _estado.update {
                    it.copy(guardando = false, error = resultado.mensaje)
                }
            }
        }
    }

    fun eliminar(id: Int) {
        if (_estado.value.guardando) return
        viewModelScope.launch {
            _estado.update { it.copy(guardando = true, mensaje = null, error = null) }
            when (val resultado = insumoRepository.eliminar(id)) {
                is Resultado.Exito -> _estado.update {
                    it.copy(guardando = false, mensaje = "Insumo eliminado.")
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
            initializer { InventarioViewModel(contenedor.insumoRepository) }
        }
    }
}
