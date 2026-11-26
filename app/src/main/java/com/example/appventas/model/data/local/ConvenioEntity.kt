package com.example.appventas.model.data.local


import androidx.room.Entity
import androidx.room.PrimaryKey

// Catálogo de convenios y servicios (se actualiza desde la API REST)
@Entity(tableName = "convenios")
data class ConvenioEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val categoria: String,    // Dental, Automotriz, Veterinario u Otros
    val descripcion: String,
    val condiciones: String,
    val comision: Double      // % de comisión simulada
)
