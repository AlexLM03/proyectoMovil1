package com.devnoway.posproyect.data.repository

import com.devnoway.posproyect.data.conectividad.ObservadorConectividad
import com.devnoway.posproyect.data.local.daos.CatalogoDao
import com.devnoway.posproyect.data.local.daos.MultimediaDao
import com.devnoway.posproyect.data.local.daos.TandaDao
import com.devnoway.posproyect.data.local.entidades.MultimediaEntity
import com.devnoway.posproyect.data.mapeadores.aEntidad
import com.devnoway.posproyect.data.mapeadores.aModelo
import com.devnoway.posproyect.data.modelo.Categoria
import com.devnoway.posproyect.data.modelo.Producto
import com.devnoway.posproyect.data.modelo.TandaHorneado
import com.devnoway.posproyect.data.remote.ApiServicio
import com.devnoway.posproyect.data.remote.Resultado
import com.devnoway.posproyect.data.remote.ejecutarRed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repositorio del catálogo.
 *
 * Patrón *offline-first*: la pantalla siempre lee de Room (rápido y funciona
 * sin red) y `sincronizar()` trae la versión del servidor cuando hay internet.
 */
class CatalogoRepository(
    private val catalogoDao: CatalogoDao,
    private val tandaDao: TandaDao,
    private val multimediaDao: MultimediaDao,
    private val api: ApiServicio,
    private val conectividad: ObservadorConectividad
) {

    val categorias: Flow<List<Categoria>> =
        catalogoDao.observarCategorias().map { lista -> lista.map { it.aModelo() } }

    val productos: Flow<List<Producto>> =
        catalogoDao.observarProductos().map { lista -> lista.map { it.aModelo() } }

    val tandas: Flow<List<TandaHorneado>> =
        tandaDao.observarTandas().map { lista -> lista.map { it.aModelo() } }

    fun producto(id: Int): Flow<Producto?> =
        catalogoDao.observarProducto(id).map { it?.aModelo() }

    fun multimedia(productoId: Int): Flow<List<MultimediaEntity>> =
        multimediaDao.observarMultimediaDeProducto(productoId)

    val enLinea: Flow<Boolean> get() = conectividad.observar()

    fun hayConexion(): Boolean = conectividad.estaEnLinea()

    /**
     * Descarga categorías, productos y tandas, y reemplaza la copia local.
     * Devuelve cuántos productos quedaron guardados.
     */
    suspend fun sincronizar(): Resultado<Int> {
        if (!conectividad.estaEnLinea()) {
            return Resultado.Fallo("Sin conexión: se muestra el catálogo guardado.")
        }
        return ejecutarRed {
            val categoriasDto = api.categorias().categorias.orEmpty()
            val productosDto = api.productos().productos.orEmpty()
            val tandasDto = api.tandas().tandas.orEmpty()

            catalogoDao.guardarCategorias(categoriasDto.map { it.aEntidad() })
            catalogoDao.guardarProductos(productosDto.map { it.aEntidad() })
            tandaDao.guardarTandas(tandasDto.map { it.aEntidad() })

            productosDto.size
        }
    }

    /** Refresca un producto concreto y su multimedia desde el servidor. */
    suspend fun refrescarProducto(id: Int): Resultado<Unit> {
        if (!conectividad.estaEnLinea()) {
            return Resultado.Fallo("Sin conexión con el servidor.")
        }
        return ejecutarRed {
            val respuesta = api.producto(id)
            val producto = respuesta.producto
                ?: throw IllegalStateException("El producto $id no existe en el servidor.")
            catalogoDao.guardarProductos(listOf(producto.aEntidad()))
            respuesta.multimedia?.let { multimedia ->
                multimediaDao.borrarMultimediaDeProducto(id)
                multimediaDao.guardarMultimedia(multimedia.map { it.aEntidad() })
            }
        }
    }
}
