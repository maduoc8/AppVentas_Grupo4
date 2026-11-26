package com.example.appventas.ui.screen


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appventas.ui.components.EstadoChip
import com.example.appventas.viewmodel.RevisionViewModel
import com.example.appventas.viewmodel.UsuarioViewModel

// Inicio del admin (encargado de digitación): crea vendedores y revisa las ventas recibidas
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuestraDatosScreen(
    username: String,
    navController: NavController,
    viewModel: UsuarioViewModel,
    revisionViewModel: RevisionViewModel
) {
    val estado by viewModel.estado.collectAsState()
    val revision by revisionViewModel.estado.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inicio encargado de digitación") }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Bienvenido, $username",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            // Resumen: cuántas ventas hay en cada estado
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Ventas recibidas: ${revision.ventas.size}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        revision.estados.forEach { estadoVenta ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                EstadoChip(estadoVenta.nombre)
                                Text(
                                    text = "${revision.ventas.count { it.estado == estadoVenta.nombre }}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        navController.navigate("ventasRecibidas")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.AutoMirrored.Filled.FactCheck, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Ventas recibidas")
                }
            }

            item {
                Button(
                    onClick = {
                        // Formulario vacío: no muestra los datos del vendedor anterior
                        viewModel.limpiarFormulario()
                        navController.navigate("registro")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Crear vendedor")
                }
            }

            item {
                Button(
                    onClick = {
                        navController.navigate("comisiones")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Paid, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Comisiones (simulada)")
                }
            }

            item {
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
            }

            item {
                OutlinedButton(
                    onClick = {
                        revisionViewModel.cerrarSesion()
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

            item {
                Text(
                    text = "Vendedores registrados (${estado.usuarios.size})",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Vendedores guardados en Room (se actualiza sola al crear uno nuevo)
            items(estado.usuarios, key = { it.id }) { usuario ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(usuario.nombre, style = MaterialTheme.typography.titleSmall)
                        Text(usuario.correo, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
} // fin MuestraDatosScreen
