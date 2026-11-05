package com.devnoway.posproyect.data.remote

import com.devnoway.posproyect.BuildConfig
import com.devnoway.posproyect.data.sesion.AlmacenSesion
import com.google.gson.GsonBuilder
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Fábrica del cliente Retrofit.
 *
 * - Toma la URL base de `BuildConfig.API_BASE_URL`.
 * - Añade el token de sesión (`Authorization: Bearer ...`) si existe.
 * - Registra las peticiones en Logcat solo en compilaciones de depuración.
 */
object ApiCliente {

    fun crear(almacenSesion: AlmacenSesion): ApiServicio {
        val registro = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val clienteHttp = OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor { cadena ->
                val token = almacenSesion.token
                val peticion = if (token.isNullOrBlank()) {
                    cadena.request()
                } else {
                    cadena.request()
                        .newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                }
                cadena.proceed(peticion)
            }
            .addInterceptor(registro)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(clienteHttp)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
            .create(ApiServicio::class.java)
    }
}
