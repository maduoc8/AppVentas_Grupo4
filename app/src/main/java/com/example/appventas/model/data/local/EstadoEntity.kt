package com.example.appventas.model.data.local


import androidx.room.Entity
import androidx.room.PrimaryKey

// Estados posibles de una venta (En revisión, Observada, Validada, Rechazada)
@Entity(tableName = "estados")
data class EstadoEntity(
    @PrimaryKey val nombre: String,
    val descripcion: String,
    val orden: Int            // orden en que se muestran los filtros
)
