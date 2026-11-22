package com.devnoway.posproyect.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.devnoway.posproyect.data.local.daos.CatalogoDao
import com.devnoway.posproyect.data.local.daos.DireccionDao
import com.devnoway.posproyect.data.local.daos.InsumoDao
import com.devnoway.posproyect.data.local.daos.MultimediaDao
import com.devnoway.posproyect.data.local.daos.NotificacionDao
import com.devnoway.posproyect.data.local.daos.PedidoDao
import com.devnoway.posproyect.data.local.daos.RecetaDao
import com.devnoway.posproyect.data.local.daos.TandaDao
import com.devnoway.posproyect.data.local.daos.UsuarioDao
import com.devnoway.posproyect.data.local.entidades.CategoriaEntity
import com.devnoway.posproyect.data.local.entidades.DetallePedidoEntity
import com.devnoway.posproyect.data.local.entidades.DireccionEntity
import com.devnoway.posproyect.data.local.entidades.InsumoEntity
import com.devnoway.posproyect.data.local.entidades.MultimediaEntity
import com.devnoway.posproyect.data.local.entidades.NotificacionEntity
import com.devnoway.posproyect.data.local.entidades.PedidoEntity
import com.devnoway.posproyect.data.local.entidades.ProductoEntity
import com.devnoway.posproyect.data.local.entidades.ProductoInsumoEntity
import com.devnoway.posproyect.data.local.entidades.TandaEntity
import com.devnoway.posproyect.data.local.entidades.UsuarioEntity

/**
 * Base de datos local (SQLite con Room) de Acambapan.
 *
 * Replica las tablas de `pantradicion_db` para que la app funcione sin
 * conexión: el catálogo, los pedidos, el inventario y las notificaciones se
 * leen de aquí; cuando hay red, los repositorios sincronizan contra la Web API.
 */
@Database(
    entities = [
        CategoriaEntity::class,
        UsuarioEntity::class,
        ProductoEntity::class,
        TandaEntity::class,
        PedidoEntity::class,
        DetallePedidoEntity::class,
        InsumoEntity::class,
        ProductoInsumoEntity::class,
        NotificacionEntity::class,
        MultimediaEntity::class,
        DireccionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AcambapanDatabase : RoomDatabase() {

    abstract fun catalogoDao(): CatalogoDao
    abstract fun tandaDao(): TandaDao
    abstract fun pedidoDao(): PedidoDao
    abstract fun insumoDao(): InsumoDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun notificacionDao(): NotificacionDao
    abstract fun multimediaDao(): MultimediaDao
    abstract fun recetaDao(): RecetaDao
    abstract fun direccionDao(): DireccionDao

    companion object {
        private const val NOMBRE_ARCHIVO = "acambapan.db"

        /** Versión 2: direcciones de entrega y método de pago del pedido. */
        private val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `direcciones` (" +
                        "`id` INTEGER NOT NULL, " +
                        "`usuario_id` INTEGER NOT NULL, " +
                        "`alias` TEXT NOT NULL, " +
                        "`calle` TEXT NOT NULL, " +
                        "`referencias` TEXT, " +
                        "`predeterminada` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`))"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_direcciones_usuario_id` " +
                        "ON `direcciones` (`usuario_id`)"
                )
                db.execSQL(
                    "ALTER TABLE `pedidos` ADD COLUMN `metodo_pago` TEXT NOT NULL DEFAULT 'EFECTIVO'"
                )
            }
        }

        @Volatile
        private var instancia: AcambapanDatabase? = null

        fun obtener(contexto: Context): AcambapanDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    contexto.applicationContext,
                    AcambapanDatabase::class.java,
                    NOMBRE_ARCHIVO
                ).addMigrations(MIGRACION_1_2).build().also { instancia = it }
            }
    }
}
