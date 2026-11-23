package com.devnoway.posproyect

import android.app.Application
import com.devnoway.posproyect.di.ContenedorApp

/**
 * Punto de entrada de la aplicación.
 *
 * Crea el contenedor de dependencias una sola vez; la Activity y las pantallas
 * lo reciben desde aquí. Es el primer eslabón de la arquitectura.
 */
class AcambapanApplication : Application() {

    lateinit var contenedor: ContenedorApp
        private set

    override fun onCreate() {
        super.onCreate()
        contenedor = ContenedorApp(this)
    }
}
