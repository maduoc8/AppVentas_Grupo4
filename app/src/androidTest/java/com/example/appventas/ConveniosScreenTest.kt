package com.example.appventas

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController

import com.example.appventas.model.data.local.ConvenioEntity
import com.example.appventas.repository.ConvenioRepository
import com.example.appventas.ui.screen.ConveniosScreen
import com.example.appventas.viewmodel.ConvenioViewModel

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk

import kotlinx.coroutines.flow.flowOf

import org.junit.Rule
import org.junit.Test

// Prueba de interfaz: el catálogo muestra los convenios que entrega el ViewModel
class ConveniosScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun los_convenios_deben_aparecer_en_pantalla() {

        // Datos simulados
        val fakeConvenios = listOf(
            ConvenioEntity(1, "Dental Sonrisa Total", "Dental", "Limpieza", "Carencia 30 días", 5.0),
            ConvenioEntity(2, "Taller AutoCheck", "Automotriz", "Mantención", "Solo particulares", 4.0)
        )

        // Simulamos el repositorio
        val fakeRepository = mockk<ConvenioRepository>(relaxed = true)

        every { fakeRepository.obtenerConvenios() } returns flowOf(fakeConvenios)
        coEvery { fakeRepository.sincronizarDesdeApi() } returns 2

        // Creamos el ViewModel con el repositorio simulado
        val fakeViewModel = ConvenioViewModel(fakeRepository)

        // Renderizamos la interfaz
        composeRule.setContent {
            ConveniosScreen(navController = rememberNavController(), viewModel = fakeViewModel)
        }

        // Esperamos que aparezcan los convenios
        composeRule.waitUntil(timeoutMillis = 10000) {
            composeRule.onAllNodes(hasText("Dental Sonrisa Total"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Verificamos los nombres
        composeRule.onNodeWithText("Dental Sonrisa Total").assertIsDisplayed()
        composeRule.onNodeWithText("Taller AutoCheck").assertIsDisplayed()
    }
}
