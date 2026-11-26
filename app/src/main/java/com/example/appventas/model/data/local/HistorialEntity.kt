package com.example.appventas.model.data.local


import androidx.room.Entity
import androidx.room.PrimaryKey

// Historial de cambios: cada vez que una venta se envía, se corrige o se revisa
@Entity(tableName = "historial")
data class HistorialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ventaId: Long,
    val fecha: String,    // dd/mm/aaaa hh:mm
    val usuario: String,  // quién hizo el cambio
    val accion: String,   // ej: "Venta registrada y enviada", "Venta aprobada"
    val estado: String,   // estado en que quedó la venta
    val detalle: String   // observación o dato extra
)
