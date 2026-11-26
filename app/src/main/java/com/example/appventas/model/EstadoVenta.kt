package com.example.appventas.model

import com.example.appventas.model.data.local.VentaEntity

// Estados por los que pasa una venta (también se guardan en la tabla "estados" de Room)
const val ESTADO_EN_REVISION = "En revisión"  // el vendedor la envió y espera revisión
const val ESTADO_VALIDADA = "Validada"        // el encargado la aprobó
const val ESTADO_OBSERVADA = "Observada"      // requiere corrección del vendedor
const val ESTADO_RECHAZADA = "Rechazada"      // inválida: queda cerrada

// Regla del caso: el vendedor solo puede modificar o eliminar una venta que no esté cerrada
fun VentaEntity.esEditable(): Boolean =
    estado == ESTADO_EN_REVISION || estado == ESTADO_OBSERVADA

// El encargado solo revisa las ventas que están esperando revisión
fun VentaEntity.esRevisable(): Boolean = estado == ESTADO_EN_REVISION
