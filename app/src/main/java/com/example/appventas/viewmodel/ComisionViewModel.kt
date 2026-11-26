package com.example.appventas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appventas.model.ComisionUiState
import com.example.appventas.repository.VentaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ViewModel del resumen de comisiones simuladas (se generan al validar una venta)
class ComisionViewModel(
    private val repository: VentaRepository
) : ViewModel() {

    // Estado interno mutable
    private val _estado = MutableStateFlow(ComisionUiState())

    // Estado expuesto para la UI
    val estado: StateFlow<ComisionUiState> = _estado

    private var job: Job? = null

    // correo = null -> todas las comisiones (encargado); con correo -> solo las de ese vendedor
    fun cargar(correo: String?) {
        job?.cancel()

        val flujo = if (correo == null) repository.obtenerComisiones()
        else repository.obtenerComisionesPorVendedor(correo)

        _estado.value = ComisionUiState(
            titulo = if (correo == null) "Comisiones del equipo" else "Mis comisiones"
        )

        job = viewModelScope.launch {
            flujo.collect { lista ->
                _estado.update {
                    it.copy(
                        comisiones = lista,
                        totalVendido = lista.sumOf { c -> c.montoVenta },
                        totalComision = lista.sumOf { c -> c.montoComision },
                        porVendedor = lista
                            .groupBy { c -> c.vendedorNombre }
                            .mapValues { (_, delVendedor) -> delVendedor.sumOf { c -> c.montoComision } }
                    )
                }
            }
        }
    } // fin cargar
}
