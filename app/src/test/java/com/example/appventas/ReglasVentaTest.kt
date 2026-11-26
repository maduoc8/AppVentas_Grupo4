package com.example.appventas

import com.example.appventas.model.ESTADO_EN_REVISION
import com.example.appventas.model.ESTADO_OBSERVADA
import com.example.appventas.model.ESTADO_RECHAZADA
import com.example.appventas.model.ESTADO_VALIDADA
import com.example.appventas.model.calcularComision
import com.example.appventas.model.data.local.VentaEntity
import com.example.appventas.model.esCodigoConFormatoValido
import com.example.appventas.model.esEditable
import com.example.appventas.model.esFechaValida
import com.example.appventas.model.esRevisable
import com.example.appventas.model.filtrarVentas
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

// Pruebas de las reglas del negocio (no necesitan Android ni simulaciones)
class ReglasVentaTest : StringSpec({

    // Venta de ejemplo para no repetir todos los campos
    fun venta(id: Long, codigo: String, cliente: String, estado: String) = VentaEntity(
        id = id,
        tipoServicio = "Dental Sonrisa Total",
        fechaOperacion = "26/11/2026",
        cliente = cliente,
        monto = 10000.0,
        codigoAutorizacion = codigo,
        observaciones = "",
        respaldo = null,
        estado = estado,
        vendedorNombre = "vendedor",
        vendedorCorreo = "vendedor@appventas.cl"
    )

    "esFechaValida acepta dd/mm/aaaa y rechaza fechas imposibles o mal escritas" {
        esFechaValida("26/11/2026") shouldBe true
        esFechaValida("31/02/2026") shouldBe false // febrero no tiene 31
        esFechaValida("2026-11-26") shouldBe false
        esFechaValida("") shouldBe false
    }

    "esCodigoConFormatoValido exige de 6 a 12 letras, números o guiones" {
        esCodigoConFormatoValido("AUT-12345") shouldBe true
        esCodigoConFormatoValido("aut-12345") shouldBe true // se compara en mayúsculas
        esCodigoConFormatoValido("A1") shouldBe false       // muy corto
        esCodigoConFormatoValido("AUT 12345") shouldBe false // espacio no permitido
    }

    "calcularComision aplica el porcentaje y redondea a pesos" {
        calcularComision(25990.0, 5.0) shouldBe 1300.0 // 1299,5 -> 1300
        calcularComision(10000.0, 3.5) shouldBe 350.0
    }

    "solo se editan las ventas en revisión u observadas y solo se revisan las en revisión" {
        venta(1, "AUT-000001", "Ana", ESTADO_EN_REVISION).esEditable() shouldBe true
        venta(2, "AUT-000002", "Ana", ESTADO_OBSERVADA).esEditable() shouldBe true
        venta(3, "AUT-000003", "Ana", ESTADO_VALIDADA).esEditable() shouldBe false
        venta(4, "AUT-000004", "Ana", ESTADO_RECHAZADA).esEditable() shouldBe false

        venta(5, "AUT-000005", "Ana", ESTADO_EN_REVISION).esRevisable() shouldBe true
        venta(6, "AUT-000006", "Ana", ESTADO_OBSERVADA).esRevisable() shouldBe false
    }

    "filtrarVentas busca por texto y filtra por estado" {
        val v1 = venta(1, "AUT-111111", "Ana Torres", ESTADO_EN_REVISION)
        val v2 = venta(2, "AUT-222222", "Bruno Díaz", ESTADO_VALIDADA)
        val v3 = venta(3, "DEN-333333", "Ana Torres", ESTADO_VALIDADA)
        val ventas = listOf(v1, v2, v3)

        filtrarVentas(ventas, "", null) shouldContainExactly ventas
        filtrarVentas(ventas, "ana", null) shouldContainExactly listOf(v1, v3)
        filtrarVentas(ventas, "", ESTADO_VALIDADA) shouldContainExactly listOf(v2, v3)
        filtrarVentas(ventas, "333", ESTADO_VALIDADA) shouldContainExactly listOf(v3) // por código
    }
})
