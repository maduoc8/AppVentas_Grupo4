package com.example.appventas

import com.example.appventas.model.ESTADO_EN_REVISION
import com.example.appventas.model.ESTADO_RECHAZADA
import com.example.appventas.model.ESTADO_VALIDADA
import com.example.appventas.model.data.local.ConvenioEntity
import com.example.appventas.model.data.local.VentaEntity
import com.example.appventas.repository.ConvenioRepository
import com.example.appventas.repository.VentaRepository
import com.example.appventas.viewmodel.RevisionViewModel

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class RevisionViewModelTest : StringSpec({

    val dispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(dispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    // Venta enviada por el vendedor, esperando revisión
    val ventaEnRevision = VentaEntity(
        id = 7,
        convenioId = 3,
        tipoServicio = "Clínica VetAmigo",
        fechaOperacion = "26/11/2026",
        cliente = "Carla Muñoz",
        monto = 20000.0,
        codigoAutorizacion = "VET-77777",
        observaciones = "",
        respaldo = null,
        comprobante = "/fotos/vale.jpg",
        estado = ESTADO_EN_REVISION,
        vendedorNombre = "vendedor",
        vendedorCorreo = "vendedor@appventas.cl"
    )

    // Repositorios simulados: la venta 7 existe y su código es único
    fun crearRepositorio(): VentaRepository {
        val fakeRepository = mockk<VentaRepository>(relaxed = true)
        every { fakeRepository.obtenerEstados() } returns flowOf(emptyList())
        every { fakeRepository.obtenerTodasLasVentas() } returns flowOf(listOf(ventaEnRevision))
        every { fakeRepository.observarVenta(7) } returns flowOf(ventaEnRevision)
        every { fakeRepository.obtenerHistorial(7) } returns flowOf(emptyList())
        coEvery { fakeRepository.contarPorCodigo("VET-77777") } returns 1
        return fakeRepository
    }

    fun crearConvenios(): ConvenioRepository {
        val fakeConvenios = mockk<ConvenioRepository>(relaxed = true)
        coEvery { fakeConvenios.obtenerPorId(3) } returns
                ConvenioEntity(3, "Clínica VetAmigo", "Veterinario", "Consultas", "Máx. 2 mascotas", 6.0)
        return fakeConvenios
    }

    "al abrir una venta se valida el código de autorización automáticamente" {
        runTest(dispatcher) {
            val testViewModel = RevisionViewModel(crearRepositorio(), crearConvenios())
            testViewModel.seleccionarVenta(7)
            advanceUntilIdle()

            testViewModel.estado.value.ventaSeleccionada shouldBe ventaEnRevision
            val verificacion = testViewModel.estado.value.verificacion
            verificacion.shouldNotBeNull()
            verificacion.formatoValido shouldBe true
            verificacion.codigoUnico shouldBe true
            verificacion.tieneComprobante shouldBe true
        }
    }

    "rechazar sin escribir el motivo muestra error y no cambia la venta" {
        runTest(dispatcher) {
            val fakeRepository = crearRepositorio()
            val testViewModel = RevisionViewModel(fakeRepository, crearConvenios())
            testViewModel.seleccionarVenta(7)
            advanceUntilIdle()

            testViewModel.rechazar()
            advanceUntilIdle()

            testViewModel.estado.value.errorObservacion.shouldNotBeNull()
            coVerify(exactly = 0) { fakeRepository.revisarVenta(any(), any(), any(), any(), any()) }
        }
    }

    "rechazar con motivo deja la venta rechazada con la observación" {
        runTest(dispatcher) {
            val fakeRepository = crearRepositorio()
            val testViewModel = RevisionViewModel(fakeRepository, crearConvenios())
            testViewModel.iniciarEncargado("admin")
            testViewModel.seleccionarVenta(7)
            advanceUntilIdle()

            testViewModel.onObservacionChange("La boleta no corresponde al cliente")
            testViewModel.rechazar()
            advanceUntilIdle()

            coVerify {
                fakeRepository.revisarVenta(
                    ventaEnRevision,
                    ESTADO_RECHAZADA,
                    "La boleta no corresponde al cliente",
                    "admin",
                    6.0
                )
            }
        }
    }

    "aprobar valida la venta usando el % de comisión de su convenio" {
        runTest(dispatcher) {
            val fakeRepository = crearRepositorio()
            val testViewModel = RevisionViewModel(fakeRepository, crearConvenios())
            testViewModel.iniciarEncargado("admin")
            testViewModel.seleccionarVenta(7)
            advanceUntilIdle()

            testViewModel.aprobar()
            advanceUntilIdle()

            // El convenio 3 (VetAmigo) tiene 6% de comisión simulada
            coVerify { fakeRepository.revisarVenta(ventaEnRevision, ESTADO_VALIDADA, "", "admin", 6.0) }
            testViewModel.estado.value.mensaje.shouldNotBeNull()
        }
    }
})
