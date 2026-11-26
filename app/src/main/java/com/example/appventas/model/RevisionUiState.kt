package com.example.appventas.model

import com.example.appventas.model.data.local.EstadoEntity
import com.example.appventas.model.data.local.HistorialEntity
import com.example.appventas.model.data.local.VentaEntity

// Modelo que representa el estado de la sección del encargado de digitación
data class RevisionUiState(
    val encargadoNombre: String = "",

    // Ventas recibidas (de todos los vendedores) con su búsqueda y filtro
    val ventas: List<VentaEntity> = emptyList(),
    val estados: List<EstadoEntity> = emptyList(),
    val busqueda: String = "",
    val filtroEstado: String? = null, // null = todos los estados

    // Venta abierta en el detalle
    val ventaSeleccionada: VentaEntity? = null,
    val historial: List<HistorialEntity> = emptyList(),
    val verificacion: VerificacionCodigo? = null,
    val observacion: String = "",          // lo que escribe el encargado
    val errorObservacion: String? = null,

    val procesando: Boolean = false,
    val mensaje: String? = null            // aviso después de aprobar, observar o rechazar
)

// Resultado de la validación automática del código de autorización
data class VerificacionCodigo(
    val formatoValido: Boolean,    // 6 a 12 letras, números o guiones
    val codigoUnico: Boolean,      // ninguna otra venta usa el mismo código
    val tieneComprobante: Boolean  // el vendedor adjuntó la foto
)
