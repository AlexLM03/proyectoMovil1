package com.devnoway.posproyect.di

import android.content.Context
import com.devnoway.posproyect.data.conectividad.ObservadorConectividad
import com.devnoway.posproyect.data.local.AcambapanDatabase
import com.devnoway.posproyect.data.remote.ApiCliente
import com.devnoway.posproyect.data.remote.ApiServicio
import com.devnoway.posproyect.data.repository.CarritoRepository
import com.devnoway.posproyect.data.repository.CatalogoRepository
import com.devnoway.posproyect.data.repository.DireccionRepository
import com.devnoway.posproyect.data.repository.InsumoRepository
import com.devnoway.posproyect.data.repository.NotificacionRepository
import com.devnoway.posproyect.data.repository.PedidoRepository
import com.devnoway.posproyect.data.repository.SesionRepository
import com.devnoway.posproyect.data.sesion.AlmacenSesion

/**
 * Contenedor de dependencias (inyección manual).
 *
 * Es el eslabón "application" de la arquitectura:
 * Application -> Activity -> Compose -> ViewModel -> Repository -> local/remoto
 *
 * Se construye una sola vez en [com.devnoway.posproyect.AcambapanApplication].
 */
class ContenedorApp(contexto: Context) {

    private val baseDeDatos: AcambapanDatabase = AcambapanDatabase.obtener(contexto)

    val almacenSesion: AlmacenSesion = AlmacenSesion(contexto)
    val conectividad: ObservadorConectividad = ObservadorConectividad(contexto)
    private val api: ApiServicio = ApiCliente.crear(almacenSesion)

    val catalogoRepository = CatalogoRepository(
        catalogoDao = baseDeDatos.catalogoDao(),
        tandaDao = baseDeDatos.tandaDao(),
        multimediaDao = baseDeDatos.multimediaDao(),
        api = api,
        conectividad = conectividad
    )

    val pedidoRepository = PedidoRepository(
        baseDeDatos = baseDeDatos,
        pedidoDao = baseDeDatos.pedidoDao(),
        api = api,
        conectividad = conectividad
    )

    val insumoRepository = InsumoRepository(
        insumoDao = baseDeDatos.insumoDao(),
        api = api,
        conectividad = conectividad
    )

    val direccionRepository = DireccionRepository(
        direccionDao = baseDeDatos.direccionDao(),
        api = api,
        conectividad = conectividad
    )

    val notificacionRepository = NotificacionRepository(
        notificacionDao = baseDeDatos.notificacionDao(),
        api = api,
        conectividad = conectividad
    )

    val sesionRepository = SesionRepository(
        api = api,
        almacenSesion = almacenSesion,
        usuarioDao = baseDeDatos.usuarioDao(),
        conectividad = conectividad
    )

    val carritoRepository = CarritoRepository()
}
