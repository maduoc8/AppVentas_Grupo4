package com.example.appventas.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appventas.model.ConvenioUiState
import com.example.appventas.repository.ConvenioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ViewModel del catálogo de convenios: consulta la API REST y muestra lo guardado en Room
class ConvenioViewModel(
    private val repository: ConvenioRepository
) : ViewModel() {

    // Estado interno mutable
    private val _estado = MutableStateFlow(ConvenioUiState())

    // Estado expuesto para la UI
    val estado: StateFlow<ConvenioUiState> = _estado

    // Se llama automáticamente al iniciar
    init {
        viewModelScope.launch { repository.cargarConveniosIniciales() }

        // La lista viene de Room y se actualiza sola cuando llegan datos de la API
        viewModelScope.launch {
            repository.obtenerConvenios().collect { lista ->
                _estado.update { it.copy(convenios = lista) }
            }
        }

        sincronizar()
    }

    // Consulta la API en segundo plano; si falla, quedan los convenios guardados en Room
    fun sincronizar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, mensaje = null) }
            try {
                val cantidad = repository.sincronizarDesdeApi()
                _estado.update {
                    it.copy(
                        cargando = false,
                        desdeApi = true,
                        mensaje = "Catálogo actualizado desde la API ($cantidad convenios)"
                    )
                }
            } catch (e: Exception) {
                println("Error al obtener datos: ${e.localizedMessage}")
                _estado.update {
                    it.copy(
                        cargando = false,
                        desdeApi = false,
                        mensaje = "No se pudo conectar con la API: se muestran los convenios guardados"
                    )
                }
            }
        }
    } // fin sincronizar

    // null = todas las categorías
    fun onFiltroCategoriaChange(categoria: String?) {
        _estado.update { it.copy(filtroCategoria = categoria) }
    } // fin onFiltroCategoriaChange
}
