package com.example.appventas.model

import com.example.appventas.model.data.local.VentaEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Reglas del negocio que no dependen de Android: se prueban con pruebas unitarias

// Porcentaje que se usa si la venta no tiene un convenio con comisión definida
const val COMISION_POR_DEFECTO = 5.0

// Fecha de hoy en formato dd/mm/aaaa (se propone en el formulario)
fun fechaDeHoy(): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

// La fecha debe venir como dd/mm/aaaa y existir en el calendario (31/02/2026 no sirve)
fun esFechaValida(fecha: String): Boolean {
    val texto = fecha.trim()
    if (!Regex("""\d{2}/\d{2}/\d{4}""").matches(texto)) return false

    val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    formato.isLenient = false // no acepta fechas imposibles
    return try {
        formato.parse(texto) != null
    } catch (e: Exception) {
        false
    }
} // fin esFechaValida

// Código de autorización ficticio: de 6 a 12 letras, números o guiones (ej: AUT-12345)
fun esCodigoConFormatoValido(codigo: String): Boolean =
    Regex("^[A-Z0-9-]{6,12}$").matches(codigo.trim().uppercase())

// Comisión simulada: un porcentaje del monto, redondeado a pesos
fun calcularComision(monto: Double, porcentaje: Double): Double =
    Math.round(monto * porcentaje / 100.0).toDouble()

// Monto con separador de miles: 25990.0 -> "$25.990"
fun formatoPesos(monto: Double): String = "$" + "%,.0f".format(monto)

// Busca por texto (código, cliente, convenio o vendedor) y filtra por estado (null = todos)
fun filtrarVentas(
    ventas: List<VentaEntity>,
    busqueda: String,
    estado: String?
): List<VentaEntity> {
    val texto = busqueda.trim()

    return ventas.filter { venta ->
        val coincideEstado = estado == null || venta.estado == estado
        val coincideTexto = texto.isEmpty() ||
                venta.codigoAutorizacion.contains(texto, ignoreCase = true) ||
                venta.cliente.contains(texto, ignoreCase = true) ||
                venta.tipoServicio.contains(texto, ignoreCase = true) ||
                venta.vendedorNombre.contains(texto, ignoreCase = true)

        coincideEstado && coincideTexto
    }
} // fin filtrarVentas
