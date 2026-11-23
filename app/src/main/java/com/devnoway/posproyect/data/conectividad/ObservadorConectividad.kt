package com.devnoway.posproyect.data.conectividad

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Detecta si hay internet. La app es *offline-first*: sin red sigue leyendo de
 * Room y encola los pedidos; al recuperar la conexión se sincroniza.
 */
class ObservadorConectividad(contexto: Context) {

    private val gestor =
        contexto.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun estaEnLinea(): Boolean {
        val red = gestor.activeNetwork ?: return false
        val capacidades = gestor.getNetworkCapabilities(red) ?: return false
        return capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /** Emite `true`/`false` cada vez que cambia la red disponible. */
    fun observar(): Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(estaEnLinea())
            }

            override fun onLost(network: Network) {
                trySend(estaEnLinea())
            }

            override fun onCapabilitiesChanged(
                network: Network,
                capacidades: NetworkCapabilities
            ) {
                trySend(capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
            }
        }

        trySend(estaEnLinea())
        gestor.registerDefaultNetworkCallback(callback)
        awaitClose { gestor.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()
}
