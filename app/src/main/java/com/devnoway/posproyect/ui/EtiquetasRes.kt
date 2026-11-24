package com.devnoway.posproyect.ui

import androidx.annotation.StringRes
import com.devnoway.posproyect.R
import com.devnoway.posproyect.data.modelo.EstadoPedido
import com.devnoway.posproyect.data.modelo.MetodoPago

/**
 * Textos de los enums del dominio, resueltos contra los recursos de idioma.
 *
 * Los modelos (`EstadoPedido`, `MetodoPago`) siguen siendo independientes de la
 * interfaz: conservan su `codigo` para hablar con la API y con Room, pero el
 * texto que ve el usuario se toma de `strings.xml` / `values-en/strings.xml`
 * a través de estas extensiones.
 */

@get:StringRes
val EstadoPedido.etiquetaRes: Int
    get() = when (this) {
        EstadoPedido.RECIBIDO -> R.string.estado_recibido
        EstadoPedido.EN_PREPARACION -> R.string.estado_en_preparacion
        EstadoPedido.EN_CAMINO -> R.string.estado_en_camino
        EstadoPedido.ENTREGADO -> R.string.estado_entregado
        EstadoPedido.CANCELADO -> R.string.estado_cancelado
    }

@get:StringRes
val MetodoPago.etiquetaRes: Int
    get() = when (this) {
        MetodoPago.EFECTIVO -> R.string.metodo_efectivo
        MetodoPago.TARJETA_TERMINAL -> R.string.metodo_tarjeta
        MetodoPago.TRANSFERENCIA -> R.string.metodo_transferencia
    }

@get:StringRes
val MetodoPago.descripcionRes: Int
    get() = when (this) {
        MetodoPago.EFECTIVO -> R.string.metodo_efectivo_desc
        MetodoPago.TARJETA_TERMINAL -> R.string.metodo_tarjeta_desc
        MetodoPago.TRANSFERENCIA -> R.string.metodo_transferencia_desc
    }
