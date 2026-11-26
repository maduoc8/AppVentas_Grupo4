package com.example.appventas.model.data.local


import androidx.room.Entity
import androidx.room.PrimaryKey

// Comisión simulada: se crea cuando el encargado aprueba (valida) una venta
@Entity(tableName = "comisiones")
data class ComisionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ventaId: Long,
    val vendedorNombre: String,
    val vendedorCorreo: String,
    val convenio: String,
    val montoVenta: Double,
    val porcentaje: Double,
    val montoComision: Double,
    val fecha: String
)
