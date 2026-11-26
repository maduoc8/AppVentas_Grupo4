package com.example.appventas.model.data.local


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ventas")
data class VentaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val convenioId: Int? = null,     // convenio elegido del catálogo (Room / API)
    val tipoServicio: String,        // nombre del convenio o servicio
    val fechaOperacion: String,
    val cliente: String,             // cliente ficticio
    val monto: Double,
    val codigoAutorizacion: String,
    val observaciones: String,       // comentario del vendedor
    val respaldo: String?,           // contenido del QR leído con la cámara
    val comprobante: String? = null, // ruta de la foto de la boleta o vale
    val estado: String,
    val observacionRevision: String? = null, // comentario del encargado al revisar
    val vendedorNombre: String = "",
    val vendedorCorreo: String       // vendedor que registró la venta
)
