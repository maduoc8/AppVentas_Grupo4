package com.example.appventas.model

import com.example.appventas.model.data.local.ClienteEntity
import com.example.appventas.model.data.local.ConvenioEntity
import com.example.appventas.model.data.local.EstadoEntity
import com.example.appventas.model.data.local.VentaEntity

// Modelo que representa el estado de la sección de ventas del vendedor
data class VentaUiState(
    // Lo que se está escribiendo en el formulario
    val convenioId: Int? = null,
    val tipoServicio: String = "",   // nombre del convenio o servicio elegido
    val fechaOperacion: String = "",
    val cliente: String = "",
    val monto: String = "",
    val codigoAutorizacion: String = "",
    val observaciones: String = "",
    val respaldo: String? = null,    // contenido del QR leído con la cámara
    val comprobante: String? = null, // ruta de la foto de la boleta o vale
    val observacionRevision: String? = null, // lo que pidió corregir el encargado
    val errores: VentaErrores = VentaErrores(), // Objeto que contiene los errores por campo
    val errorGeneral: String? = null,

    // Catálogos guardados en Room para elegir en el formulario
    val convenios: List<ConvenioEntity> = emptyList(),
    val clientes: List<ClienteEntity> = emptyList(),
    val estados: List<EstadoEntity> = emptyList(),

    // Vendedor conectado y su lista de ventas
    val vendedorNombre: String = "",
    val vendedorCorreo: String = "",
    val ventas: List<VentaEntity> = emptyList(),
    val busqueda: String = "",
    val filtroEstado: String? = null, // null = todos los estados
    val ventaSeleccionadaId: Long? = null,

    val guardando: Boolean = false,
    val guardadoExitosamente: Boolean = false
)
