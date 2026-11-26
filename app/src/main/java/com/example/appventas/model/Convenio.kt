package com.example.appventas.model

// Convenio tal como llega desde la API REST (JSON)
data class Convenio(
    val id: Int,             // ID del convenio
    val nombre: String,      // Nombre del convenio o servicio
    val categoria: String,   // Dental, Automotriz, Veterinario u Otros
    val descripcion: String, // Qué incluye
    val condiciones: String, // Condiciones del convenio
    val comision: Double     // % de comisión simulada para el vendedor
)
