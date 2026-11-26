package com.example.appventas.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appventas.model.ESTADO_OBSERVADA
import com.example.appventas.model.data.local.VentaEntity
import com.example.appventas.model.esEditable
import com.example.appventas.model.esRevisable
import com.example.appventas.model.formatoPesos
import com.example.appventas.ui.components.Dato
import com.example.appventas.ui.components.EstadoChip
import com.example.appventas.ui.components.FotoComprobante
import com.example.appventas.viewmodel.RevisionViewModel

// Detalle de una venta: datos, comprobante, validación del código e historial de cambios
// puedeRevisar = true  -> encargado (puede aprobar, observar o rechazar)
// puedeRevisar = false -> vendedor (solo mira y, si corresponde, corrige)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleVentaScreen(
    ventaId: Long,
    puedeRevisar: Boolean,
    navController: NavController,
    viewModel: RevisionViewModel,
    onCorregir: (VentaEntity) -> Unit = {}
) {
    val estado by viewModel.estado.collectAsState()
    val context = LocalContext.current
    val venta = estado.ventaSeleccionada

    // Carga la venta al abrir la pantalla
    LaunchedEffect(ventaId) {
        viewModel.seleccionarVenta(ventaId)
    }

    // Después de aprobar, observar o rechazar: aviso y vuelve a la lista
    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensaje()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (puedeRevisar) "Validar operación" else "Detalle de venta") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->

        if (venta == null || venta.id != ventaId) {
            Text("Cargando venta...", modifier = Modifier.padding(innerPadding).padding(16.dp))
        } else {

        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(venta.codigoAutorizacion, style = MaterialTheme.typography.headlineSmall)
                EstadoChip(venta.estado)
            }

            // ---------- Datos de la venta ----------
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Dato("Vendedor", venta.vendedorNombre)
                    Dato("Convenio", venta.tipoServicio)
                    Dato("Cliente", venta.cliente)
                    Dato("Fecha", venta.fechaOperacion)
                    Dato("Monto", formatoPesos(venta.monto))
                    venta.respaldo?.let { Dato("Respaldo", it) }
                    if (venta.observaciones.isNotBlank()) Dato("Obs. vendedor", venta.observaciones)
                }
            }

            // ---------- Revisar comprobante ----------
            Text("Comprobante", style = MaterialTheme.typography.titleMedium)
            FotoComprobante(ruta = venta.comprobante)

            // ---------- Validar código de autorización (solo encargado) ----------
            if (puedeRevisar) {
                estado.verificacion?.let { verificacion ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Validación del código", style = MaterialTheme.typography.titleMedium)
                            Chequeo("Formato válido (6 a 12 letras, números o guiones)", verificacion.formatoValido)
                            Chequeo("Código único (no está en otra venta)", verificacion.codigoUnico)
                            Chequeo("Comprobante adjunto", verificacion.tieneComprobante)
                        }
                    }
                }
            }

            // Lo que escribió el encargado en la última revisión
            venta.observacionRevision?.let {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Observación del encargado: $it", modifier = Modifier.padding(12.dp))
                }
            }

            // ---------- Acciones ----------
            if (puedeRevisar && venta.esRevisable()) {
                OutlinedTextField(
                    value = estado.observacion,
                    onValueChange = viewModel::onObservacionChange,
                    label = { Text("Observaciones de la revisión") },
                    isError = estado.errorObservacion != null,
                    supportingText = {
                        Text(estado.errorObservacion ?: "Obligatorio para observar o rechazar")
                    },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = viewModel::aprobar,
                    enabled = !estado.procesando,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Aprobar (validada)")
                }

                OutlinedButton(
                    onClick = viewModel::observar,
                    enabled = !estado.procesando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ReportProblem, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Observar (requiere corrección)")
                }

                OutlinedButton(
                    onClick = viewModel::rechazar,
                    enabled = !estado.procesando,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Rechazar (inválida)")
                }
            } else if (puedeRevisar) {
                Text(
                    text = if (venta.estado == ESTADO_OBSERVADA) "Esperando la corrección del vendedor"
                    else "Esta venta ya fue revisada",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else if (venta.esEditable()) {
                // El vendedor puede corregir y reenviar
                Button(
                    onClick = { onCorregir(venta) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Corregir venta")
                }
            }

            // ---------- Historial de cambios ----------
            Text("Historial de cambios", style = MaterialTheme.typography.titleMedium)
            if (estado.historial.isEmpty()) {
                Text("Sin movimientos")
            }
            estado.historial.forEach { registro ->
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(registro.accion, style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = "${registro.fecha} · ${registro.usuario} · ${registro.estado}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (registro.detalle.isNotBlank()) {
                        Text(registro.detalle, style = MaterialTheme.typography.bodySmall)
                    }
                    HorizontalDivider(Modifier.padding(top = 6.dp))
                }
            }
        }
        } // fin else
    }
} // fin DetalleVentaScreen

// Una línea con ✓ verde o ✗ roja
@Composable
private fun Chequeo(texto: String, ok: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (ok) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = if (ok) "Correcto" else "Con problema",
            tint = if (ok) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.width(8.dp))
        Text(texto)
    }
} // fin Chequeo
