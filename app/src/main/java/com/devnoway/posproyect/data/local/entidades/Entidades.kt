
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * Entidades de Room: un espejo local de las tablas de `pantradicion_db`.
 *
 * Los nombres de columna conservan el estilo de MySQL (`snake_case`) para que
 * el esquema local y el del servidor se lean igual. El servidor es la fuente
 * de verdad; la base local permite trabajar sin conexión y consultar rápido.
 */

@Entity(tableName = "categorias")
data class CategoriaEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    @ColumnInfo(name = "icono_emoji") val iconoEmoji: String,
    val activo: Boolean
)

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val correo: String,
    val telefono: String,
    val direccion: String,
    val rol: String,
    val activo: Boolean
)

@Entity(
    tableName = "productos",
    indices = [Index("categoria_id")]
)
data class ProductoEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "categoria_id") val categoriaId: Int,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val emoji: String,
    val disponible: Boolean
)

@Entity(tableName = "tandas_horneado", indices = [Index("producto_id")])
data class TandaEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "producto_id") val productoId: Int,
    @ColumnInfo(name = "producto_nombre") val productoNombre: String,
    @ColumnInfo(name = "hora_salida") val horaSalida: String,
    @ColumnInfo(name = "piezas_estimadas") val piezasEstimadas: Int,
    @ColumnInfo(name = "piezas_disponibles") val piezasDisponibles: Int,
    val estado: String,
    @ColumnInfo(name = "dias_semana") val diasSemana: String
)

@Entity(
    tableName = "pedidos",
    indices = [Index("cliente_id"), Index("estado"), Index("pendiente_sincronizar")]
)
data class PedidoEntity(
    @PrimaryKey val folio: String,
    @ColumnInfo(name = "cliente_id") val clienteId: Int?,
    @ColumnInfo(name = "cliente_nombre") val clienteNombre: String,
    @ColumnInfo(name = "cliente_telefono") val clienteTelefono: String,
    @ColumnInfo(name = "tipo_entrega") val tipoEntrega: String,
    @ColumnInfo(name = "direccion_entrega") val direccionEntrega: String?,
    @ColumnInfo(name = "tipo_empaque") val tipoEmpaque: String,
    @ColumnInfo(name = "metodo_pago", defaultValue = "EFECTIVO") val metodoPago: String = "EFECTIVO",
    @ColumnInfo(name = "requiere_rebanado") val requiereRebanado: Boolean,
    @ColumnInfo(name = "notas_panadero") val notasPanadero: String?,
    val subtotal: Double,
    @ColumnInfo(name = "costo_envio") val costoEnvio: Double,
    val total: Double,
    val estado: String,
    @ColumnInfo(name = "repartidor_id") val repartidorId: Int?,
    @ColumnInfo(name = "repartidor_nombre") val repartidorNombre: String?,
    @ColumnInfo(name = "repartidor_telefono") val repartidorTelefono: String?,
    @ColumnInfo(name = "tiempo_estimado") val tiempoEstimado: String,
    @ColumnInfo(name = "fecha_pedido") val fechaPedido: String,
    /** true = se creó sin conexión y todavía no existe en el servidor. */
    @ColumnInfo(name = "pendiente_sincronizar") val pendienteSincronizar: Boolean = false
)

@Entity(
    tableName = "detalles_pedido",
    indices = [Index("pedido_folio"), Index("producto_id")]
)
data class DetallePedidoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "pedido_folio") val pedidoFolio: String,
    @ColumnInfo(name = "producto_id") val productoId: Int,
    @ColumnInfo(name = "producto_nombre") val productoNombre: String,
    val cantidad: Int,
    @ColumnInfo(name = "precio_unitario") val precioUnitario: Double
)

@Entity(tableName = "insumos", indices = [Index(value = ["nombre"], unique = true)])
data class InsumoEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val stock: Double,
    val minimo: Double,
    val unidad: String
)

@Entity(tableName = "producto_insumos", primaryKeys = ["producto_id", "insumo_id"])
data class ProductoInsumoEntity(
    @ColumnInfo(name = "producto_id") val productoId: Int,
    @ColumnInfo(name = "insumo_id") val insumoId: Int,
    @ColumnInfo(name = "cantidad_requerida") val cantidadRequerida: Double
)

@Entity(tableName = "direcciones", indices = [Index("usuario_id")])
data class DireccionEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "usuario_id") val usuarioId: Int,
    val alias: String,
    val calle: String,
    val referencias: String?,
    val predeterminada: Boolean
)

@Entity(tableName = "notificaciones", indices = [Index("usuario_id"), Index("leida")])
data class NotificacionEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "usuario_id") val usuarioId: Int?,
    @ColumnInfo(name = "pedido_folio") val pedidoFolio: String?,
    val titulo: String,
    val mensaje: String,
    val leida: Boolean,
    @ColumnInfo(name = "creado_en") val creadoEn: String
)

@Entity(tableName = "producto_multimedia", indices = [Index("producto_id")])
data class MultimediaEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "producto_id") val productoId: Int,
    val tipo: String,
    val url: String,
    val orden: Int
)

/** Producto con el nombre de su categoría resuelto por JOIN. */
data class ProductoConCategoria(
    @Embedded val producto: ProductoEntity,
    @ColumnInfo(name = "categoria_nombre") val categoriaNombre: String?
)

/** Pedido con sus líneas de detalle. */
data class PedidoConLineas(
    @Embedded val pedido: PedidoEntity,
    @Relation(parentColumn = "folio", entityColumn = "pedido_folio")
    val lineas: List<DetallePedidoEntity>
)
