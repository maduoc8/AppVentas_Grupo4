package com.example.appventas.model.data.local


import androidx.room.Entity
import androidx.room.PrimaryKey

// Clientes ficticios para registrar ventas (no son datos reales)
@Entity(tableName = "clientes")
data class ClienteEntity(
    @PrimaryKey val id: Long,
    val nombre: String,
    val rut: String,      // RUT ficticio
    val telefono: String
)
