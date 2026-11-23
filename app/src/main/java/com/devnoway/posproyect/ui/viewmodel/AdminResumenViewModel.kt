package com.devnoway.posproyect.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.devnoway.posproyect.data.modelo.EstadoPedido
import com.devnoway.posproyect.data.modelo.Insumo
import com.devnoway.posproyect.data.modelo.Pedido
import com.devnoway.posproyect.data.modelo.ResumenNegocio
import com.devnoway.posproyect.data.modelo.Usuario
import com.devnoway.posproyect.data.remote.Resultado
import com.devnoway.posproyect.data.repository.InsumoRepository
import com.devnoway.posproyect.data.repository.PedidoRepository
import com.devnoway.posproyect.data.repository.SesionRepository
import com.devnoway.posproyect.di.ContenedorApp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel de los indicadores administrativos.
 *
 * Los números se calculan en el teléfono con lo que ya está guardado en Room,
 * así el panel abre al instante y sigue sirviendo sin conexión. `refrescar()`
 * trae pedidos e inventario del servidor.
 *
 * Lo usan tanto el panel principal como el dash administrativo.
 */
class AdminResumenViewModel(
    private val pedidoRepository: PedidoRepository,
    private val insumoRepository: InsumoRepository,
    sesionRepository: SesionRepository
) : ViewModel() {

    data class Estado(
        val pedidosHoy: Int = 0,
        val ventasHoy: Double = 0.0,
        val ticketPromedio: Double = 0.0,
        val insumosBajos: Int = 0,
        val pedidosPendientes: Int = 0,
        val recientes: List<Pedido> = emptyList(),
        val enLinea: Boolean = true,
        val sincronizando: Boolean = false,
        val mensaje: String? = null
    )

    private val _estado = MutableStateFlow(Estado())
    val estado: StateFlow<Estado> = _estado.asStateFlow()

    /** Cuenta con la que se abrió el panel (se muestra en la cabecera). */
    val sesion: StateFlow<Usuario?> = sesionRepository.sesion

    private var pedidosActuales: List<Pedido> = emptyList()
    private var insumosActuales: List<Insumo> = emptyList()

    /** Últimos números calculados por el servidor (se prefieren sobre el cálculo local). */
    private var resumenRemoto: ResumenNegocio? = null

    init {
        viewModelScope.launch {
            pedidoRepository.pedidos.collect { pedidos ->
                pedidosActuales = pedidos
                recalcular()
            }
        }
        viewModelScope.launch {
            insumoRepository.insumos.collect { insumos ->
                insumosActuales = insumos
                recalcular()
            }
        }
        viewModelScope.launch {
            pedidoRepository.enLinea.collect { enLinea ->
                _estado.update { it.copy(enLinea = enLinea) }
            }
        }
        refrescar()
    }

    /** Descarga pedidos e inventario; si no hay red conserva lo local. */
    fun refrescar() {
        if (_estado.value.sincronizando) return
        viewModelScope.launch {
            _estado.update { it.copy(sincronizando = true, mensaje = null) }
            val pedidos = pedidoRepository.sincronizar(clienteId = null, esAdmin = true)
            val insumos = insumoRepository.sincronizar()
            val remoto = pedidoRepository.resumenServidor()
            if (remoto is Resultado.Exito) {
                resumenRemoto = remoto.datos
                recalcular()
            }
            val fallo = (pedidos as? Resultado.Fallo)?.mensaje
                ?: (insumos as? Resultado.Fallo)?.mensaje
            _estado.update { it.copy(sincronizando = false, mensaje = fallo) }
        }
    }

    fun limpiarMensaje() {
        _estado.update { it.copy(mensaje = null) }
    }

    private fun recalcular() {
        val delDia = pedidosActuales.filter {
            esDeHoy(it.fecha) && it.estado != EstadoPedido.CANCELADO
        }
        val ventas = delDia.sumOf { it.total }
        val remoto = resumenRemoto
        _estado.update {
            it.copy(
                pedidosHoy = remoto?.pedidosHoy ?: delDia.size,
                ventasHoy = remoto?.ventasHoy ?: ventas,
                ticketPromedio = remoto?.ticketPromedio
                    ?: if (delDia.isEmpty()) 0.0 else ventas / delDia.size,
                insumosBajos = remoto?.insumosBajos
                    ?: insumosActuales.count { insumo -> insumo.bajo },
                pedidosPendientes = pedidosActuales.count { pedido -> pedido.pendienteSincronizar },
                recientes = pedidosActuales.take(3)
            )
        }
    }

    /** La fecha del pedido viene como "27/10/2026 10:24". */
    private fun esDeHoy(fechaMostrada: String): Boolean {
        val hoy = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es-MX")).format(Date())
        return fechaMostrada.startsWith(hoy)
    }

    companion object {
        fun fabrica(contenedor: ContenedorApp): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AdminResumenViewModel(
                    pedidoRepository = contenedor.pedidoRepository,
                    insumoRepository = contenedor.insumoRepository,
                    sesionRepository = contenedor.sesionRepository
                )
            }
        }
    }
}
