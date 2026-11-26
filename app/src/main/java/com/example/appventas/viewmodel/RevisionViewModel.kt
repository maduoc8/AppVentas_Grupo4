package com.example.appventas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appventas.model.COMISION_POR_DEFECTO
import com.example.appventas.model.ESTADO_OBSERVADA
import com.example.appventas.model.ESTADO_RECHAZADA
import com.example.appventas.model.ESTADO_VALIDADA
import com.example.appventas.model.RevisionUiState
import com.example.appventas.model.VerificacionCodigo
import com.example.appventas.model.data.local.VentaEntity
import com.example.appventas.model.esCodigoConFormatoValido
import com.example.appventas.model.esRevisable
import com.example.appventas.repository.ConvenioRepository
import com.example.appventas.repository.VentaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ViewModel del encargado de digitación (el admin): ventas recibidas, revisión y validación
// (el vendedor también lo usa para ver el detalle y el historial de sus ventas)
class RevisionViewModel(
    private val repository: VentaRepository,
    private val convenioRepository: ConvenioRepository
) : ViewModel() {

    // Estado interno mutable
    private val _estado = MutableStateFlow(RevisionUiState())

    // Estado expuesto para la UI
    val estado: StateFlow<RevisionUiState> = _estado

    // Escuchan la lista de ventas y la venta abierta en el detalle
    private var listaJob: Job? = null
    private var detalleJob: Job? = null

    init {
        viewModelScope.launch { repository.cargarDatosIniciales() }

        // Estados de Room para los filtros
        viewModelScope.launch {
            repository.obtenerEstados().collect { lista ->
                _estado.update { it.copy(estados = lista) }
            }
        }
    }

    // ---------- Sesión del encargado ----------

    // Se llama al entrar al inicio del admin: escucha todas las ventas recibidas
    fun iniciarEncargado(nombre: String) {
        if (_estado.value.encargadoNombre == nombre && listaJob != null) return

        listaJob?.cancel()
        _estado.update { it.copy(encargadoNombre = nombre, busqueda = "", filtroEstado = null) }

        listaJob = viewModelScope.launch {
            repository.obtenerTodasLasVentas().collect { lista ->
                _estado.update { it.copy(ventas = lista) }
            }
        }
    } // fin iniciarEncargado

    fun cerrarSesion() {
        listaJob?.cancel()
        listaJob = null
        detalleJob?.cancel()
        detalleJob = null
        _estado.update { RevisionUiState(estados = it.estados) }
    } // fin cerrarSesion

    // ---------- Lista: buscar y filtrar ----------

    fun onBusquedaChange(valor: String) {
        _estado.update { it.copy(busqueda = valor) }
    } // fin onBusquedaChange

    // null = mostrar todos los estados
    fun onFiltroEstadoChange(estado: String?) {
        _estado.update { it.copy(filtroEstado = estado) }
    } // fin onFiltroEstadoChange

    // ---------- Detalle de una venta ----------

    // Abre una venta: sus datos, su historial y la verificación del código
    fun seleccionarVenta(id: Long) {
        detalleJob?.cancel()
        _estado.update {
            it.copy(
                ventaSeleccionada = null,
                historial = emptyList(),
                verificacion = null,
                observacion = "",
                errorObservacion = null,
                mensaje = null
            )
        }

        detalleJob = viewModelScope.launch {
            launch {
                repository.observarVenta(id).collect { venta ->
                    _estado.update { it.copy(ventaSeleccionada = venta) }
                    if (venta != null) verificarCodigo(venta)
                }
            }
            launch {
                repository.obtenerHistorial(id).collect { lista ->
                    _estado.update { it.copy(historial = lista) }
                }
            }
        }
    } // fin seleccionarVenta

    // Validación automática del código de autorización (ayuda al encargado a decidir)
    private suspend fun verificarCodigo(venta: VentaEntity) {
        val repeticiones = repository.contarPorCodigo(venta.codigoAutorizacion)

        _estado.update {
            it.copy(
                verificacion = VerificacionCodigo(
                    formatoValido = esCodigoConFormatoValido(venta.codigoAutorizacion),
                    codigoUnico = repeticiones <= 1,
                    tieneComprobante = venta.comprobante != null
                )
            )
        }
    } // fin verificarCodigo

    fun onObservacionChange(valor: String) {
        _estado.update { it.copy(observacion = valor, errorObservacion = null) }
    } // fin onObservacionChange

    // ---------- Validar operación ----------

    fun aprobar() = revisar(ESTADO_VALIDADA)

    fun observar() = revisar(ESTADO_OBSERVADA)

    fun rechazar() = revisar(ESTADO_RECHAZADA)

    private fun revisar(nuevoEstado: String) {
        val actual = _estado.value
        val venta = actual.ventaSeleccionada ?: return

        if (!venta.esRevisable()) {
            _estado.update { it.copy(mensaje = "Esta venta ya fue revisada") }
            return
        }

        // Observar o rechazar exige explicarle el motivo al vendedor
        if (nuevoEstado != ESTADO_VALIDADA && actual.observacion.isBlank()) {
            _estado.update { it.copy(errorObservacion = "Escribe el motivo para el vendedor") }
            return
        }

        viewModelScope.launch {
            _estado.update { it.copy(procesando = true) }

            // La comisión simulada depende del convenio de la venta
            val porcentaje = venta.convenioId
                ?.let { convenioRepository.obtenerPorId(it)?.comision }
                ?: COMISION_POR_DEFECTO

            repository.revisarVenta(
                venta = venta,
                nuevoEstado = nuevoEstado,
                observacion = actual.observacion.trim(),
                usuario = actual.encargadoNombre,
                porcentajeComision = porcentaje
            )

            _estado.update {
                it.copy(
                    procesando = false,
                    observacion = "",
                    mensaje = "Venta ${venta.codigoAutorizacion}: ${nuevoEstado.lowercase()}"
                )
            }
        }
    } // fin revisar

    fun limpiarMensaje() {
        _estado.update { it.copy(mensaje = null) }
    } // fin limpiarMensaje
}
