package com.devnoway.posproyect.data.remote

import com.devnoway.posproyect.data.remote.dto.ErrorRespuestaDto
import com.google.gson.Gson
import java.io.IOException
import retrofit2.HttpException

/**
 * Resultado de una operación de red: éxito con datos o fallo con un mensaje
 * listo para mostrar en pantalla. Evita que las excepciones de Retrofit/OkHttp
 * lleguen a los ViewModel.
 */
sealed interface Resultado<out T> {
    data class Exito<out T>(val datos: T) : Resultado<T>
    data class Fallo(val mensaje: String) : Resultado<Nothing>

    val esExito: Boolean get() = this is Exito<T>
}

private val gson = Gson()

/** Extrae el mensaje del cuerpo de error de la API, si viene. */
private fun mensajeDelCuerpo(cuerpo: String?): String? =
    runCatching { gson.fromJson(cuerpo, ErrorRespuestaDto::class.java)?.error?.mensaje }
        .getOrNull()

/**
 * Ejecuta una llamada a la API y la convierte en [Resultado].
 */
suspend fun <T> ejecutarRed(bloque: suspend () -> T): Resultado<T> =
    try {
        Resultado.Exito(bloque())
    } catch (error: HttpException) {
        val texto = runCatching { error.response()?.errorBody()?.string() }.getOrNull()
        val mensaje = mensajeDelCuerpo(texto)
            ?: when (error.code()) {
                401 -> "Tu sesión expiró. Vuelve a iniciar sesión."
                403 -> "No tienes permiso para esta acción."
                404 -> "El recurso no existe en el servidor."
                409 -> "El servidor rechazó el cambio por un conflicto de estado."
                else -> "El servidor respondió con error ${error.code()}."
            }
        Resultado.Fallo(mensaje)
    } catch (error: IOException) {
        Resultado.Fallo("Sin conexión con el servidor. Revisa tu red e intenta de nuevo.")
    } catch (error: Exception) {
        Resultado.Fallo(error.message ?: "Ocurrió un error inesperado.")
    }
