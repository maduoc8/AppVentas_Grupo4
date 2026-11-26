package com.example.appventas

import com.example.appventas.model.Convenio
import com.example.appventas.model.data.local.ConvenioEntity
import com.google.gson.Gson
import java.io.File
import com.example.appventas.repository.ConvenioRepository
import com.example.appventas.viewmodel.ConvenioViewModel

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

import io.mockk.coEvery
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
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ConvenioViewModelTest : StringSpec({

    val dispatcher = StandardTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(dispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    val fakeConvenios = listOf(
        ConvenioEntity(1, "Dental Sonrisa Total", "Dental", "Limpieza", "Carencia 30 días", 5.0),
        ConvenioEntity(2, "Taller AutoCheck", "Automotriz", "Mantención", "Solo particulares", 4.0)
    )

    "convenios debe contener los datos esperados después de consultar la API" {

        runTest(dispatcher) {

            val fakeRepository = mockk<ConvenioRepository>(relaxed = true)

            every { fakeRepository.obtenerConvenios() } returns flowOf(fakeConvenios)
            coEvery { fakeRepository.sincronizarDesdeApi() } returns 2

            val testViewModel = ConvenioViewModel(fakeRepository)

            advanceUntilIdle()

            testViewModel.estado.value.convenios shouldContainExactly fakeConvenios
            testViewModel.estado.value.desdeApi shouldBe true
            testViewModel.estado.value.mensaje!! shouldContain "API (2 convenios)"
        }
    }

    "si la API falla se siguen mostrando los convenios guardados en Room" {

        runTest(dispatcher) {

            val fakeRepository = mockk<ConvenioRepository>(relaxed = true)

            every { fakeRepository.obtenerConvenios() } returns flowOf(fakeConvenios)
            coEvery { fakeRepository.sincronizarDesdeApi() } throws IOException("Sin internet")

            val testViewModel = ConvenioViewModel(fakeRepository)

            advanceUntilIdle()

            testViewModel.estado.value.convenios shouldContainExactly fakeConvenios
            testViewModel.estado.value.desdeApi shouldBe false
            testViewModel.estado.value.cargando shouldBe false
            testViewModel.estado.value.mensaje!! shouldContain "No se pudo conectar"
        }
    }

    "el JSON publicado como API se convierte en convenios (igual que lo hace Retrofit con Gson)" {

        // Archivo que la app descarga: api/convenios.json en la raíz del repositorio
        val json = File("../api/convenios.json").readText()

        val convenios = Gson().fromJson(json, Array<Convenio>::class.java).toList()

        convenios.size shouldBe 8
        convenios.first().nombre shouldBe "Dental Sonrisa Total"
        convenios.map { it.categoria }.distinct() shouldContainExactly
                listOf("Dental", "Automotriz", "Veterinario", "Otros")
    }
})
