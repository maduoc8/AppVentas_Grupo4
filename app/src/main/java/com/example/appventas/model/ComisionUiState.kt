package com.example.appventas.model

import com.example.appventas.model.data.local.ComisionEntity

// Modelo que representa el resumen de comisiones simuladas
data class ComisionUiState(
    val titulo: String = "Comisiones",
    val comisiones: List<ComisionEntity> = emptyList(),
    val totalVendido: Double = 0.0,
    val totalComision: Double = 0.0,
    val porVendedor: Map<String, Double> = emptyMap() // vendedor -> total de comisión
)
