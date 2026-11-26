package com.example.appventas.model

import com.example.appventas.model.data.local.ConvenioEntity

// Modelo que representa el catálogo de convenios (Room + API REST)
data class ConvenioUiState(
    val convenios: List<ConvenioEntity> = emptyList(),
    val filtroCategoria: String? = null, // null = todas las categorías
    val cargando: Boolean = false,
    val desdeApi: Boolean = false,       // true si la última consulta a la API funcionó
    val mensaje: String? = null
)
