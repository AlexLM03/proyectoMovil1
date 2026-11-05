package com.devnoway.posproyect.data.sesion

import android.content.Context
import com.devnoway.posproyect.data.modelo.MetodoPago
import com.devnoway.posproyect.data.modelo.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Guarda la sesión del usuario (token + datos) en SharedPreferences.
 *
 * El token se envía en la cabecera `Authorization: Bearer ...` de cada
 * petición. La sesión sobrevive al cierre de la app, así que el cliente puede
 * seguir consultando el catálogo local aunque esté sin conexión.
 */
class AlmacenSesion(contexto: Context) {

    private val preferencias =
        contexto.getSharedPreferences("sesion_acambapan", Context.MODE_PRIVATE)

    private val _usuario = MutableStateFlow(cargar())
    val usuario: StateFlow<Usuario?> = _usuario.asStateFlow()

    val token: String?
        get() = preferencias.getString(CLAVE_TOKEN, null)

    fun guardar(usuario: Usuario, token: String) {
        preferencias.edit()
            .putInt(CLAVE_ID, usuario.id)
            .putString(CLAVE_NOMBRE, usuario.nombre)
            .putString(CLAVE_CORREO, usuario.correo)
            .putString(CLAVE_TELEFONO, usuario.telefono)
            .putString(CLAVE_DIRECCION, usuario.direccion)
            .putString(CLAVE_ROL, usuario.rol)
            .putString(CLAVE_METODO_PAGO, usuario.metodoPagoPreferido.codigo)
            .putBoolean(CLAVE_ACTIVO, usuario.activo)
            .putString(CLAVE_TOKEN, token)
            .apply()
        _usuario.value = usuario
    }

    /** Actualiza los datos del perfil sin tocar el token. */
    fun actualizarUsuario(usuario: Usuario) {
        val tokenActual = token ?: return
        guardar(usuario, tokenActual)
    }

    fun cerrarSesion() {
        preferencias.edit().clear().apply()
        _usuario.value = null
    }

    private fun cargar(): Usuario? {
        val id = preferencias.getInt(CLAVE_ID, 0)
        val nombre = preferencias.getString(CLAVE_NOMBRE, null) ?: return null
        if (id <= 0) return null
        return Usuario(
            id = id,
            nombre = nombre,
            correo = preferencias.getString(CLAVE_CORREO, "") ?: "",
            telefono = preferencias.getString(CLAVE_TELEFONO, "") ?: "",
            direccion = preferencias.getString(CLAVE_DIRECCION, "") ?: "",
            rol = preferencias.getString(CLAVE_ROL, "CLIENTE") ?: "CLIENTE",
            metodoPagoPreferido = MetodoPago.desdeCodigo(
                preferencias.getString(CLAVE_METODO_PAGO, MetodoPago.EFECTIVO.codigo)
            ),
            activo = preferencias.getBoolean(CLAVE_ACTIVO, true)
        )
    }

    private companion object {
        const val CLAVE_ID = "usuario_id"
        const val CLAVE_NOMBRE = "usuario_nombre"
        const val CLAVE_CORREO = "usuario_correo"
        const val CLAVE_TELEFONO = "usuario_telefono"
        const val CLAVE_DIRECCION = "usuario_direccion"
        const val CLAVE_ROL = "usuario_rol"
        const val CLAVE_METODO_PAGO = "usuario_metodo_pago"
        const val CLAVE_ACTIVO = "usuario_activo"
        const val CLAVE_TOKEN = "token"
    }
}
