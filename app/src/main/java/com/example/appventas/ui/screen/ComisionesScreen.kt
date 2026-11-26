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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appventas.model.formatoPesos
import com.example.appventas.ui.components.Dato
import com.example.appventas.viewmodel.ComisionViewModel

// Comisiones simuladas: se generan cuando el encargado valida una venta
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComisionesScreen(
    navController: NavController,
    viewModel: ComisionViewModel
) {
    val estado by viewModel.estado.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(estado.titulo) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Resumen general
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Comisión total", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = formatoPesos(estado.totalComision),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text("Ventas validadas: ${estado.comisiones.size} · Total vendido: ${formatoPesos(estado.totalVendido)}")
                        Text(
                            text = "Montos simulados (ficticios), según el % de cada convenio",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Total por vendedor (útil para el encargado)
            if (estado.porVendedor.size > 1) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Por vendedor", style = MaterialTheme.typography.titleMedium)
                            estado.porVendedor.forEach { (vendedor, total) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(vendedor)
                                    Text(formatoPesos(total))
                                }
                            }
                        }
                    }
                }
            }

            if (estado.comisiones.isEmpty()) {
                item { Text("Aún no hay ventas validadas") }
            }

            items(estado.comisiones, key = { it.id }) { comision ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(comision.convenio, style = MaterialTheme.typography.titleSmall)
                        Dato("Vendedor", comision.vendedorNombre)
                        Dato("Venta", formatoPesos(comision.montoVenta))
                        Dato("Comisión", "${formatoPesos(comision.montoComision)} (${comision.porcentaje}%)")
                        Text(comision.fecha, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
} // fin ComisionesScreen
