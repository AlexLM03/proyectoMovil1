package com.devnoway.posproyect.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.devnoway.posproyect.data.local.entidades.CategoriaEntity
import com.devnoway.posproyect.data.local.entidades.DetallePedidoEntity
import com.devnoway.posproyect.data.local.entidades.DireccionEntity
import com.devnoway.posproyect.data.local.entidades.InsumoEntity
import com.devnoway.posproyect.data.local.entidades.MultimediaEntity
import com.devnoway.posproyect.data.local.entidades.NotificacionEntity
import com.devnoway.posproyect.data.local.entidades.PedidoConLineas
import com.devnoway.posproyect.data.local.entidades.PedidoEntity
import com.devnoway.posproyect.data.local.entidades.ProductoConCategoria
import com.devnoway.posproyect.data.local.entidades.ProductoEntity
import com.devnoway.posproyect.data.local.entidades.ProductoInsumoEntity
import com.devnoway.posproyect.data.local.entidades.TandaEntity
import com.devnoway.posproyect.data.local.entidades.UsuarioEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAOs de Room.
 *
 * Lecturas = `Flow` (la interfaz se entera sola de los cambios).
 * Escrituras = funciones `suspend`, siempre dentro de una transacción cuando
 * tocan más de una tabla (lo hace el repositorio con `withTransaction`).
 */

@Dao
interface CatalogoDao {

    @Query("SELECT * FROM categorias WHERE activo = 1 ORDER BY id")
    fun observarCategorias(): Flow<List<CategoriaEntity>>

    @Query(
        """
        SELECT p.*, c.nombre AS categoria_nombre
        FROM productos p
        LEFT JOIN categorias c ON c.id = p.categoria_id
        ORDER BY p.categoria_id, p.id
        """
    )
    fun observarProductos(): Flow<List<ProductoConCategoria>>

    @Query(
        """
        SELECT p.*, c.nombre AS categoria_nombre
        FROM productos p
        LEFT JOIN categorias c ON c.id = p.categoria_id
        WHERE p.id = :id
        """
    )
    fun observarProducto(id: Int): Flow<ProductoConCategoria?>

    @Query("SELECT COUNT(*) FROM productos")
    suspend fun contarProductos(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarCategorias(categorias: List<CategoriaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarProductos(productos: List<ProductoEntity>)

    @Query("DELETE FROM productos")
    suspend fun borrarProductos()

    @Query("DELETE FROM categorias")
    suspend fun borrarCategorias()
}

@Dao
interface TandaDao {

    @Query("SELECT * FROM tandas_horneado ORDER BY hora_salida")
    fun observarTandas(): Flow<List<TandaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTandas(tandas: List<TandaEntity>)

    @Query("DELETE FROM tandas_horneado")
    suspend fun borrarTandas()
}

@Dao
interface PedidoDao {

    @Transaction
    @Query("SELECT * FROM pedidos ORDER BY fecha_pedido DESC")
    fun observarPedidos(): Flow<List<PedidoConLineas>>

    @Transaction
    @Query("SELECT * FROM pedidos WHERE cliente_id = :clienteId ORDER BY fecha_pedido DESC")
    fun observarPedidosDeCliente(clienteId: Int): Flow<List<PedidoConLineas>>

    @Transaction
    @Query("SELECT * FROM pedidos ORDER BY fecha_pedido DESC LIMIT 1")
    fun observarUltimoPedido(): Flow<PedidoConLineas?>

    @Transaction
    @Query("SELECT * FROM pedidos WHERE folio = :folio")
    fun observarPedido(folio: String): Flow<PedidoConLineas?>

    @Query("SELECT * FROM pedidos WHERE folio = :folio")
    suspend fun buscarPedido(folio: String): PedidoEntity?

    @Query("SELECT * FROM pedidos WHERE pendiente_sincronizar = 1 ORDER BY fecha_pedido")
    suspend fun pendientesDeSincronizar(): List<PedidoEntity>

    @Query("SELECT * FROM pedidos ORDER BY fecha_pedido DESC")
    suspend fun todosLosPedidos(): List<PedidoEntity>

    @Query("SELECT * FROM detalles_pedido WHERE pedido_folio = :folio ORDER BY id")
    suspend fun detallesDe(folio: String): List<DetallePedidoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarPedido(pedido: PedidoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarPedidos(pedidos: List<PedidoEntity>)

    @Insert
    suspend fun guardarDetalles(detalles: List<DetallePedidoEntity>)

    @Query("DELETE FROM detalles_pedido WHERE pedido_folio = :folio")
    suspend fun borrarDetalles(folio: String)

    @Query("UPDATE pedidos SET pendiente_sincronizar = :pendiente WHERE folio = :folio")
    suspend fun marcarPendiente(folio: String, pendiente: Boolean)

    @Query("UPDATE pedidos SET estado = :estado WHERE folio = :folio")
    suspend fun actualizarEstado(folio: String, estado: String)
}

@Dao
interface InsumoDao {

    @Query("SELECT * FROM insumos ORDER BY nombre")
    fun observarInsumos(): Flow<List<InsumoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarInsumos(insumos: List<InsumoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarInsumo(insumo: InsumoEntity)

    @Query("DELETE FROM insumos WHERE id = :id")
    suspend fun borrarInsumo(id: Int)

    @Query("DELETE FROM insumos")
    suspend fun borrarTodo()

    @Query("SELECT * FROM insumos WHERE id = :id")
    suspend fun buscarInsumo(id: Int): InsumoEntity?
}

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarUsuario(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios WHERE correo = :correo OR telefono = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id")
    fun observarUsuario(id: Int): Flow<UsuarioEntity?>
}

@Dao
interface NotificacionDao {

    @Query("SELECT * FROM notificaciones ORDER BY creado_en DESC")
    fun observarTodas(): Flow<List<NotificacionEntity>>

    @Query("SELECT * FROM notificaciones WHERE usuario_id = :usuarioId ORDER BY creado_en DESC")
    fun observarDeUsuario(usuarioId: Int): Flow<List<NotificacionEntity>>

    @Query("SELECT COUNT(*) FROM notificaciones WHERE leida = 0")
    fun observarNoLeidas(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarNotificaciones(notificaciones: List<NotificacionEntity>)

    @Query("UPDATE notificaciones SET leida = 1 WHERE id = :id")
    suspend fun marcarLeida(id: Int)
}

@Dao
interface MultimediaDao {

    @Query("SELECT * FROM producto_multimedia WHERE producto_id = :productoId ORDER BY orden")
    fun observarMultimediaDeProducto(productoId: Int): Flow<List<MultimediaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarMultimedia(multimedia: List<MultimediaEntity>)

    @Query("DELETE FROM producto_multimedia WHERE producto_id = :productoId")
    suspend fun borrarMultimediaDeProducto(productoId: Int)
}

@Dao
interface DireccionDao {

    @Query("SELECT * FROM direcciones WHERE usuario_id = :usuarioId ORDER BY predeterminada DESC, id")
    fun observarDeUsuario(usuarioId: Int): Flow<List<DireccionEntity>>

    @Query("SELECT * FROM direcciones WHERE usuario_id = :usuarioId ORDER BY predeterminada DESC, id")
    suspend fun listarDeUsuario(usuarioId: Int): List<DireccionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(direcciones: List<DireccionEntity>)

    @Query("DELETE FROM direcciones WHERE usuario_id = :usuarioId")
    suspend fun borrarDeUsuario(usuarioId: Int)
}

@Dao
interface RecetaDao {

    @Query("SELECT * FROM producto_insumos WHERE producto_id = :productoId")
    fun observarReceta(productoId: Int): Flow<List<ProductoInsumoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarReceta(receta: List<ProductoInsumoEntity>)
}
