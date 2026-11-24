package com.devnoway.posproyect.data.remote

import android.content.Context
import androidx.annotation.StringRes
import com.devnoway.posproyect.R
import com.devnoway.posproyect.data.remote.dto.ErrorRespuestaDto
import com.google.gson.Gson
import java.io.IOException
import retrofit2.HttpException

/**
 * Resultado de una operación de red: éxito con datos o fallo con un mensaje
 * listo para mostrar en pantalla. Evita que las excepciones de Retrofit/OkHttp
 * lleguen a los ViewModel.
 *
 * Un fallo puede describirse de dos maneras:
 *  - con un recurso de texto ([recurso]) cuando el mensaje lo escribe la app;
 *  - con texto plano ([mensaje]) cuando lo redacta el servidor.
 * La pantalla siempre llama a [texto] para obtener el texto en el idioma activo.
 */
sealed interface Resultado<out T> {
    data class Exito<out T>(val datos: T) : Resultado<T>

    data class Fallo(
        val mensaje: String = "",
        @param:StringRes val recurso: Int = 0,
        val argumentos: List<Any> = emptyList()
    ) : Resultado<Nothing> {

        /** Atajo para los mensajes que la propia app redacta en strings.xml. */
        constructor(@StringRes recurso: Int, vararg argumentos: Any) :
            this("", recurso, argumentos.toList())

        /** Texto final en el idioma del usuario. */
        fun texto(contexto: Context): String =
            if (recurso != 0) {
                contexto.getString(recurso, *argumentos.toTypedArray())
            } else {
                mensaje
            }
    }

    val esExito: Boolean get() = this is Exito<T>
}

/**
 * Error de una regla de negocio que ya sabe cómo mostrarse traducido.
 *
 * Los repositorios lo lanzan dentro de [ejecutarRed] para no perder el recurso
 * de texto al pasar por el manejo genérico de excepciones.
 */
class ErrorLocalizado(
    @param:StringRes val recurso: Int,
    vararg val argumentos: Any
) : Exception()

private val gson = Gson()

/** Extrae el mensaje del cuerpo de error de la API, si viene. */
private fun mensajeDelCuerpo(cuerpo: String?): String? =
    runCatching { gson.fromJson(cuerpo, ErrorRespuestaDto::class.java)?.error?.mensaje }
        .getOrNull()

/**
 * Ejecuta una llamada a la API y la convierte en [Resultado].
 *
 * Los mensajes que redacta el servidor se conservan tal cual; los que redacta
 * la app se devuelven como recurso para que se traduzcan en pantalla.
 */
suspend fun <T> ejecutarRed(bloque: suspend () -> T): Resultado<T> =
    try {
        Resultado.Exito(bloque())
    } catch (error: HttpException) {
        val texto = runCatching { error.response()?.errorBody()?.string() }.getOrNull()
        val delServidor = mensajeDelCuerpo(texto)
        if (delServidor != null) {
            Resultado.Fallo(delServidor)
        } else {
            when (error.code()) {
                401 -> Resultado.Fallo(R.string.err_sesion_expirada)
                403 -> Resultado.Fallo(R.string.err_sin_permiso)
                404 -> Resultado.Fallo(R.string.err_no_existe)
                409 -> Resultado.Fallo(R.string.err_conflicto)
                else -> Resultado.Fallo(R.string.err_servidor_codigo, error.code())
            }
        }
    } catch (error: IOException) {
        Resultado.Fallo(R.string.err_sin_conexion)
    } catch (error: ErrorLocalizado) {
        Resultado.Fallo(error.recurso, *error.argumentos)
    } catch (error: Exception) {
        error.message?.let { Resultado.Fallo(it) }
            ?: Resultado.Fallo(R.string.err_inesperado)
    }
