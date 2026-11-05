package com.devnoway.posproyect.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.devnoway.posproyect.data.modelo.Categoria
import com.devnoway.posproyect.data.modelo.Producto
import com.devnoway.posproyect.data.remote.Resultado
import com.devnoway.posproyect.data.repository.CatalogoRepository
import com.devnoway.posproyect.di.ContenedorApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel del catálogo.
 *
 * Lee de Room (por eso funciona sin internet) y ofrece `sincronizar()` para
 * traer la versión del servidor. El filtro por categoría y la búsqueda se
 * resuelven en memoria sobre los productos ya guardados.
 */
class CatalogoViewModel(private val catalogoRepository: CatalogoRepository) : ViewModel() {

    data class Estado(
        val categorias: List<Categoria> = emptyList(),
        val productos: List<Producto> = emptyList(),
        val busqueda: String = "",
        val categoriaId: Int? = null,
        val cargando: Boolean = true,
        val sincronizando: Boolean = false,
        val enLinea: Boolean = true,
        val mensaje: String? = null
    ) {
        /** Productos que cumplen el filtro y la búsqueda actuales. */
        val visibles: List<Producto>
            get() {
                val texto = busqueda.trim()
                return productos.filter { producto ->
                    (categoriaId == null || producto.categoriaId == categoriaId) &&
                        (texto.isEmpty() ||
                            producto.nombre.contains(texto, ignoreCase = true) ||
                            producto.descripcion.contains(texto, ignoreCase = true))
                }
            }
    }

    private val _estado = MutableStateFlow(Estado())
    val estado: StateFlow<Estado> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            catalogoRepository.productos.collect { productos ->
                _estado.update { it.copy(productos = productos, cargando = false) }
            }
        }
        viewModelScope.launch {
            catalogoRepository.categorias.collect { categorias ->
                _estado.update { it.copy(categorias = categorias) }
            }
        }
        viewModelScope.launch {
            catalogoRepository.enLinea.collect { enLinea ->
                _estado.update { it.copy(enLinea = enLinea) }
            }
        }
        sincronizar()
    }

    fun cambiarBusqueda(valor: String) {
        _estado.update { it.copy(busqueda = valor) }
    }

    fun seleccionarCategoria(id: Int?) {
        _estado.update { it.copy(categoriaId = id) }
    }

    fun limpiarMensaje() {
        _estado.update { it.copy(mensaje = null) }
    }

    /** Trae catálogo, categorías y tandas del servidor. */
    fun sincronizar() {
        if (_estado.value.sincronizando) return
        viewModelScope.launch {
            _estado.update { it.copy(sincronizando = true) }
            val resultado = catalogoRepository.sincronizar()
            _estado.update {
                it.copy(
                    sincronizando = false,
                    mensaje = (resultado as? Resultado.Fallo)?.mensaje
                )
            }
        }
    }

    companion object {
        fun fabrica(contenedor: ContenedorApp): ViewModelProvider.Factory = viewModelFactory {
            initializer { CatalogoViewModel(contenedor.catalogoRepository) }
        }
    }
}
