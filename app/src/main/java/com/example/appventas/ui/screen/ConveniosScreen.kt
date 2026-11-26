package com.example.appventas.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appventas.ui.components.Dato
import com.example.appventas.ui.components.FiltroOpciones
import com.example.appventas.viewmodel.ConvenioViewModel

// Catálogo de convenios y servicios (API REST + Room)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConveniosScreen(
    navController: NavController,
    viewModel: ConvenioViewModel
) {
    // Observamos el flujo de datos del ViewModel
    val estado = viewModel.estado.collectAsState().value

    val categorias = estado.convenios.map { it.categoria }.distinct()
    val conveniosMostrados = estado.convenios.filter {
        estado.filtroCategoria == null || it.categoria == estado.filtroCategoria
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catálogo de convenios") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Vuelve a consultar la API REST
                    IconButton(onClick = viewModel::sincronizar, enabled = !estado.cargando) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar desde la API")
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (estado.cargando) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            estado.mensaje?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (estado.desdeApi) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                )
            }

            // Filtro por categoría: dentales, automotrices, veterinarios, otros
            FiltroOpciones(
                opciones = categorias,
                seleccionado = estado.filtroCategoria,
                onSeleccion = viewModel::onFiltroCategoriaChange
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(conveniosMostrados, key = { it.id }) { convenio ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(convenio.nombre, style = MaterialTheme.typography.titleMedium)
                                Text("#${convenio.id}", style = MaterialTheme.typography.labelMedium)
                            }
                            Text(
                                text = convenio.categoria,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(convenio.descripcion)
                            Dato("Condiciones", convenio.condiciones)
                            Dato("Comisión vendedor", "${convenio.comision}%")
                        }
                    }
                }
            }
        }
    }
} // fin ConveniosScreen
