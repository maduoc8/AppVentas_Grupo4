package com.example.appventas.ui.screen

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appventas.model.data.local.VentaEntity
import com.example.appventas.model.esRevisable
import com.example.appventas.model.filtrarVentas
import com.example.appventas.model.formatoPesos
import com.example.appventas.ui.components.EstadoChip
import com.example.appventas.ui.components.FiltroOpciones
import com.example.appventas.viewmodel.RevisionViewModel

// Lista de ventas recibidas de todos los vendedores: filtrar, buscar y abrir para revisar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VentasRecibidasScreen(
    navController: NavController,
    viewModel: RevisionViewModel
) {
    val estado by viewModel.estado.collectAsState()

    val ventasMostradas = filtrarVentas(estado.ventas, estado.busqueda, estado.filtroEstado)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ventas recibidas") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            OutlinedTextField(
                value = estado.busqueda,
                onValueChange = viewModel::onBusquedaChange,
                label = { Text("Buscar código, cliente, convenio o vendedor") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Buscar")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            FiltroOpciones(
                opciones = estado.estados.map { it.nombre },
                seleccionado = estado.filtroEstado,
                onSeleccion = viewModel::onFiltroEstadoChange
            )

            Text(
                text = "Total: ${ventasMostradas.size}",
                style = MaterialTheme.typography.labelLarge
            )

            if (ventasMostradas.isEmpty()) {
                Text("No hay ventas para mostrar")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(ventasMostradas, key = { it.id }) { venta ->
                        VentaRecibidaCard(
                            venta = venta,
                            onClick = { navController.navigate("revisionVenta/${venta.id}") }
                        )
                    }
                }
            }
        }
    }
} // fin VentasRecibidasScreen

@Composable
private fun VentaRecibidaCard(venta: VentaEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = venta.codigoAutorizacion,
                    style = MaterialTheme.typography.titleMedium
                )
                EstadoChip(venta.estado)
            }

            Text("Vendedor: ${venta.vendedorNombre}")
            Text("Convenio: ${venta.tipoServicio}")
            Text("Cliente: ${venta.cliente}")
            Text("Monto: ${formatoPesos(venta.monto)} · ${venta.fechaOperacion}")
            Text(
                text = if (venta.comprobante != null) "Comprobante adjunto" else "Sin comprobante",
                style = MaterialTheme.typography.bodySmall
            )

            if (venta.esRevisable()) {
                Text(
                    text = "Toca para revisar →",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
} // fin VentaRecibidaCard
