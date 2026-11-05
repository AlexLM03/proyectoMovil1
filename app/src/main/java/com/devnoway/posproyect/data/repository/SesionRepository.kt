package com.devnoway.posproyect.data.repository

import com.devnoway.posproyect.data.conectividad.ObservadorConectividad
import com.devnoway.posproyect.data.local.daos.UsuarioDao
import com.devnoway.posproyect.data.mapeadores.aEntidad
import com.devnoway.posproyect.data.mapeadores.aModelo
import com.devnoway.posproyect.data.modelo.MetodoPago
import com.devnoway.posproyect.data.modelo.Usuario
import com.devnoway.posproyect.data.remote.ApiServicio
import com.devnoway.posproyect.data.remote.Resultado
import com.devnoway.posproyect.data.remote.dto.LoginSolicitud
import com.devnoway.posproyect.data.remote.dto.MetodoPagoSolicitud
import com.devnoway.posproyect.data.remote.dto.RegistroSolicitud
import com.devnoway.posproyect.data.remote.dto.SesionRespuestaDto
import com.devnoway.posproyect.data.remote.ejecutarRed
import com.devnoway.posproyect.data.sesion.AlmacenSesion
import kotlinx.coroutines.flow.StateFlow

/**
 * Repositorio de sesión: login, registro y cierre de sesión.
 *
 * El servidor valida las credenciales y devuelve un token HMAC; aquí se guarda
 * junto con el perfil para poder seguir usando la app sin conexión después.
 */
class SesionRepository(
    private val api: ApiServicio,
    private val almacenSesion: AlmacenSesion,
    private val usuarioDao: UsuarioDao,
    private val conectividad: ObservadorConectividad
) {

    val sesion: StateFlow<Usuario?> = almacenSesion.usuario

    fun cerrarSesion() = almacenSesion.cerrarSesion()

    /** Guarda el método de pago preferido del cliente en el servidor y en local. */
    suspend fun cambiarMetodoPago(metodo: MetodoPago): Resultado<Usuario> {
        if (!conectividad.estaEnLinea()) {
            return Resultado.Fallo("Necesitas conexión para cambiar tu método de pago.")
        }
        return when (
            val resultado = ejecutarRed {
                api.cambiarMetodoPago(MetodoPagoSolicitud(metodo = metodo.codigo))
            }
        ) {
            is Resultado.Fallo -> resultado
            is Resultado.Exito -> {
                val dto = resultado.datos.usuario
                if (dto == null) {
                    Resultado.Fallo("El servidor no devolvió el perfil actualizado.")
                } else {
                    val usuario = dto.aModelo()
                    almacenSesion.actualizarUsuario(usuario)
                    usuarioDao.guardarUsuario(usuario.aEntidad())
                    Resultado.Exito(usuario)
                }
            }
        }
    }

    fun hayConexion(): Boolean = conectividad.estaEnLinea()

    /**
     * Inicia sesión. Si se pide [rolRequerido], la cuenta debe tener ese rol
     * (por ejemplo ADMIN para el panel de la panadería).
     */
    suspend fun iniciarSesion(
        correo: String,
        contrasena: String,
        rolRequerido: String? = null
    ): Resultado<Usuario> {
        if (correo.isBlank() || contrasena.isBlank()) {
            return Resultado.Fallo("Escribe tu correo y tu contraseña.")
        }
        if (!conectividad.estaEnLinea()) {
            return Resultado.Fallo("Necesitas conexión para iniciar sesión. Tus datos locales siguen disponibles.")
        }

        val resultado = ejecutarRed {
            api.iniciarSesion(LoginSolicitud(correo = correo.trim(), password = contrasena))
        }
        return cuandoSesion(resultado, rolRequerido)
    }

    suspend fun registrar(
        nombre: String,
        correo: String,
        telefono: String,
        direccion: String,
        contrasena: String
    ): Resultado<Usuario> {
        if (nombre.isBlank() || correo.isBlank() || telefono.isBlank() || direccion.isBlank()) {
            return Resultado.Fallo("Completa todos los campos del registro.")
        }
        if (!correo.contains("@")) {
            return Resultado.Fallo("Escribe un correo electrónico válido.")
        }
        if (contrasena.length < 6) {
            return Resultado.Fallo("La contraseña debe tener al menos 6 caracteres.")
        }
        if (!conectividad.estaEnLinea()) {
            return Resultado.Fallo("Necesitas conexión para crear tu cuenta.")
        }

        val resultado = ejecutarRed {
            api.registrar(
                RegistroSolicitud(
                    nombre = nombre.trim(),
                    correo = correo.trim(),
                    password = contrasena,
                    telefono = telefono.trim(),
                    direccion = direccion.trim()
                )
            )
        }
        return cuandoSesion(resultado, rolRequerido = null)
    }

    private suspend fun cuandoSesion(
        resultado: Resultado<SesionRespuestaDto>,
        rolRequerido: String?
    ): Resultado<Usuario> = when (resultado) {
        is Resultado.Fallo -> resultado
        is Resultado.Exito -> {
            val respuesta = resultado.datos
            val token = respuesta.token
            val usuarioDto = respuesta.usuario
            if (token.isNullOrBlank() || usuarioDto == null) {
                Resultado.Fallo(
                    respuesta.error?.mensaje ?: "El servidor no devolvió la sesión."
                )
            } else {
                val usuario = usuarioDto.aModelo()
                if (rolRequerido != null && usuario.rol != rolRequerido) {
                    Resultado.Fallo("Esa cuenta no tiene el rol de $rolRequerido.")
                } else {
                    almacenSesion.guardar(usuario, token)
                    usuarioDao.guardarUsuario(usuario.aEntidad())
                    Resultado.Exito(usuario)
                }
            }
        }
    }
}
