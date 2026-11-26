package com.example.appventas.ui.screen

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appventas.model.ESTADO_EN_REVISION
import com.example.appventas.model.ESTADO_OBSERVADA
import com.example.appventas.viewmodel.VentaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioVendedorScreen(
    nombre: String,
    correo: String,
    navController: NavController,
    viewModel: VentaViewModel
) {
    val estado by viewModel.estado.collectAsState()

    // Cuántas ventas esperan revisión y cuántas debe corregir
    val enRevision = estado.ventas.count { it.estado == ESTADO_EN_REVISION }
    val observadas = estado.ventas.count { it.estado == ESTADO_OBSERVADA }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inicio vendedor") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Bienvenido, $nombre",
                style = MaterialTheme.typography.headlineMedium
            )

            Text("Ventas enviadas: ${estado.ventas.size} · En revisión: $enRevision")

            // Aviso: el encargado pidió corregir algunas ventas
            if (observadas > 0) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Tienes $observadas venta(s) observada(s): revísalas en \"Registro de ventas\" y corrígelas.",
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Button(
                onClick = {
                    navController.navigate("listaVentas")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PointOfSale, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Registro de ventas")
            }

            Button(
                onClick = {
                    navController.navigate("comisiones?correo=${Uri.encode(correo)}")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Paid, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Mis comisiones")
            }

            Button(
                onClick = {
                    navController.navigate("convenios")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Catálogo de convenios")
            }

            OutlinedButton(
                onClick = {
                    viewModel.cerrarSesion()
                    navController.navigate("login") {
                        // Borra todo el historial para no volver atrás sin sesión
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión")
            }
        }
    }
} // fin InicioVendedorScreen
