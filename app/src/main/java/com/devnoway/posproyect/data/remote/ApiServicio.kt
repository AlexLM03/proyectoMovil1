package com.devnoway.posproyect.data.remote

import com.devnoway.posproyect.data.remote.dto.CategoriasRespuestaDto
import com.devnoway.posproyect.data.remote.dto.CrearPedidoSolicitud
import com.devnoway.posproyect.data.remote.dto.DireccionRespuestaDto
import com.devnoway.posproyect.data.remote.dto.DireccionSolicitud
import com.devnoway.posproyect.data.remote.dto.DireccionesRespuestaDto
import com.devnoway.posproyect.data.remote.dto.EstadoSolicitud
import com.devnoway.posproyect.data.remote.dto.InsumoRespuestaDto
import com.devnoway.posproyect.data.remote.dto.InsumoSolicitud
import com.devnoway.posproyect.data.remote.dto.InsumosRespuestaDto
import com.devnoway.posproyect.data.remote.dto.LoginSolicitud
import com.devnoway.posproyect.data.remote.dto.MetodoPagoSolicitud
import com.devnoway.posproyect.data.remote.dto.NotificacionesRespuestaDto
import com.devnoway.posproyect.data.remote.dto.OkRespuestaDto
import com.devnoway.posproyect.data.remote.dto.PedidoRespuestaDto
import com.devnoway.posproyect.data.remote.dto.PedidosRespuestaDto
import com.devnoway.posproyect.data.remote.dto.PingRespuestaDto
import com.devnoway.posproyect.data.remote.dto.ProductoRespuestaDto
import com.devnoway.posproyect.data.remote.dto.ProductosRespuestaDto
import com.devnoway.posproyect.data.remote.dto.RegistroSolicitud
import com.devnoway.posproyect.data.remote.dto.ResumenRespuestaDto
import com.devnoway.posproyect.data.remote.dto.SesionRespuestaDto
import com.devnoway.posproyect.data.remote.dto.TandasRespuestaDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Contrato Retrofit de la Web API de la panadería.
 *
 * URL base: `BuildConfig.API_BASE_URL`
 * (`http://10.0.2.2:88/api/` en el emulador, que apunta al XAMPP del equipo).
 */
interface ApiServicio {

    @GET("ping")
    suspend fun ping(): PingRespuestaDto

    @POST("auth/login")
    suspend fun iniciarSesion(@Body cuerpo: LoginSolicitud): SesionRespuestaDto

    @POST("auth/registro")
    suspend fun registrar(@Body cuerpo: RegistroSolicitud): SesionRespuestaDto

    @GET("categorias")
    suspend fun categorias(): CategoriasRespuestaDto

    @GET("productos")
    suspend fun productos(
        @Query("categoriaId") categoriaId: Int? = null,
        @Query("buscar") buscar: String? = null
    ): ProductosRespuestaDto

    @GET("productos/{id}")
    suspend fun producto(@Path("id") id: Int): ProductoRespuestaDto

    @GET("tandas")
    suspend fun tandas(): TandasRespuestaDto

    @GET("pedidos")
    suspend fun pedidos(
        @Query("clienteId") clienteId: Int? = null,
        @Query("estado") estado: String? = null
    ): PedidosRespuestaDto

    @GET("pedidos/{folio}")
    suspend fun pedido(@Path("folio") folio: String): PedidoRespuestaDto

    @POST("pedidos")
    suspend fun crearPedido(@Body cuerpo: CrearPedidoSolicitud): PedidoRespuestaDto

    @PUT("pedidos/{folio}/estado")
    suspend fun cambiarEstadoPedido(
        @Path("folio") folio: String,
        @Body cuerpo: EstadoSolicitud
    ): PedidoRespuestaDto

    @DELETE("pedidos/{folio}")
    suspend fun cancelarPedido(@Path("folio") folio: String): PedidoRespuestaDto

    @GET("insumos")
    suspend fun insumos(): InsumosRespuestaDto

    @POST("insumos")
    suspend fun crearInsumo(@Body cuerpo: InsumoSolicitud): InsumoRespuestaDto

    @PUT("insumos/{id}")
    suspend fun actualizarInsumo(
        @Path("id") id: Int,
        @Body cuerpo: InsumoSolicitud
    ): InsumoRespuestaDto

    @DELETE("insumos/{id}")
    suspend fun eliminarInsumo(@Path("id") id: Int): OkRespuestaDto

    @GET("resumen")
    suspend fun resumen(): ResumenRespuestaDto

    @GET("notificaciones")
    suspend fun notificaciones(@Query("clienteId") clienteId: Int? = null): NotificacionesRespuestaDto

    @GET("direcciones")
    suspend fun direcciones(): DireccionesRespuestaDto

    @POST("direcciones")
    suspend fun crearDireccion(@Body cuerpo: DireccionSolicitud): DireccionRespuestaDto

    @PUT("direcciones/{id}")
    suspend fun actualizarDireccion(
        @Path("id") id: Int,
        @Body cuerpo: DireccionSolicitud
    ): DireccionRespuestaDto

    @DELETE("direcciones/{id}")
    suspend fun eliminarDireccion(@Path("id") id: Int): OkRespuestaDto

    @PUT("auth/metodo-pago")
    suspend fun cambiarMetodoPago(@Body cuerpo: MetodoPagoSolicitud): SesionRespuestaDto

    @PUT("notificaciones/{id}/leida")
    suspend fun marcarNotificacionLeida(@Path("id") id: Int): OkRespuestaDto
}
