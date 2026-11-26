package com.example.appventas

import com.example.appventas.model.ESTADO_EN_REVISION
import com.example.appventas.model.data.local.ClienteEntity
import com.example.appventas.model.data.local.ConvenioEntity
import com.example.appventas.model.data.local.VentaEntity
import com.example.appventas.repository.ConvenioRepository
import com.example.appventas.repository.VentaRepository
import com.example.appventas.viewmodel.VentaViewModel

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
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
class VentaViewModelTest : StringSpec({

    val dispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(dispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    // Catálogos simulados (lo que entregaría Room)
    val convenio = ConvenioEntity(1, "Dental Sonrisa Total", "Dental", "Limpieza", "Sin carencia", 5.0)
    val cliente = ClienteEntity(1, "Ana Torres", "11.111.111-1", "+56 9 0000 0001")

    // Crea el ViewModel con repositorios simulados (no usa la base de datos real)
    fun crearViewModel(fakeRepository: VentaRepository): VentaViewModel {
        val fakeConvenios = mockk<ConvenioRepository>(relaxed = true)
        every { fakeConvenios.obtenerConvenios() } returns flowOf(listOf(convenio))

        every { fakeRepository.obtenerClientes() } returns flowOf(listOf(cliente))
        every { fakeRepository.obtenerEstados() } returns flowOf(emptyList())
        every { fakeRepository.obtenerVentasPorVendedor(any()) } returns flowOf(emptyList())

        return VentaViewModel(fakeRepository, fakeConvenios)
    }

    // Llena el formulario como lo haría el vendedor en la pantalla
    fun llenarFormulario(vm: VentaViewModel, codigo: String) {
        vm.onConvenioSeleccionado(convenio)
        vm.onClienteSeleccionado(cliente)
        vm.onFechaChange("26/11/2026")
        vm.onMontoChange("25990")
        vm.onCodigoChange(codigo)
        vm.onComprobanteChange("/fotos/boleta.jpg")
    }

    "validarFormulario marca los campos obligatorios cuando el formulario está vacío" {
        runTest(dispatcher) {
            val testViewModel = crearViewModel(mockk(relaxed = true))
            advanceUntilIdle()

            testViewModel.onFechaChange("")
            testViewModel.validarFormulario() shouldBe false

            val errores = testViewModel.estado.value.errores
            errores.tipoServicio.shouldNotBeNull()
            errores.cliente.shouldNotBeNull()
            errores.fecha.shouldNotBeNull()
            errores.monto.shouldNotBeNull()
            errores.codigo.shouldNotBeNull()
            errores.comprobante.shouldNotBeNull()
        }
    }

    "guardarVenta envía la venta con estado En revisión" {
        runTest(dispatcher) {
            val fakeRepository = mockk<VentaRepository>(relaxed = true)
            coEvery { fakeRepository.obtenerPorCodigo(any()) } returns null

            val testViewModel = crearViewModel(fakeRepository)
            testViewModel.iniciarVendedor("vendedor", "vendedor@appventas.cl")
            advanceUntilIdle()

            llenarFormulario(testViewModel, "aut-12345")
            testViewModel.guardarVenta()
            advanceUntilIdle()

            coVerify {
                fakeRepository.registrarVenta(
                    match {
                        it.estado == ESTADO_EN_REVISION &&
                                it.codigoAutorizacion == "AUT-12345" && // se guarda en mayúsculas
                                it.monto == 25990.0 &&
                                it.convenioId == 1 &&
                                it.comprobante == "/fotos/boleta.jpg"
                    },
                    "vendedor"
                )
            }
            testViewModel.estado.value.guardadoExitosamente shouldBe true
        }
    }

    "guardarVenta no permite un código de autorización repetido" {
        runTest(dispatcher) {
            val ventaExistente = VentaEntity(
                id = 99,
                tipoServicio = "Taller AutoCheck",
                fechaOperacion = "01/11/2026",
                cliente = "Bruno Díaz",
                monto = 5000.0,
                codigoAutorizacion = "AUT-12345",
                observaciones = "",
                respaldo = null,
                estado = ESTADO_EN_REVISION,
                vendedorCorreo = "otro@appventas.cl"
            )
            val fakeRepository = mockk<VentaRepository>(relaxed = true)
            coEvery { fakeRepository.obtenerPorCodigo("AUT-12345") } returns ventaExistente

            val testViewModel = crearViewModel(fakeRepository)
            advanceUntilIdle()

            llenarFormulario(testViewModel, "AUT-12345")
            testViewModel.guardarVenta()
            advanceUntilIdle()

            testViewModel.estado.value.errores.codigo shouldBe "Código ya registrado"
            coVerify(exactly = 0) { fakeRepository.registrarVenta(any(), any()) }
        }
    }

    "al elegir un convenio se limpia el error de ese campo" {
        runTest(dispatcher) {
            val testViewModel = crearViewModel(mockk(relaxed = true))
            advanceUntilIdle()

            testViewModel.validarFormulario()
            testViewModel.onConvenioSeleccionado(convenio)

            testViewModel.estado.value.errores.tipoServicio.shouldBeNull()
            testViewModel.estado.value.tipoServicio shouldBe "Dental Sonrisa Total"
        }
    }
})
