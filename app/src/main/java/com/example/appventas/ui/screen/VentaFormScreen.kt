package com.example.appventas.ui.screen

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.appventas.model.fechaDeHoy
import com.example.appventas.ui.components.FotoComprobante
import com.example.appventas.ui.components.SelectorLista
import com.example.appventas.utils.ComprobanteHelper
import com.example.appventas.viewmodel.VentaViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VentaFormScreen(
    navController: NavController,
    viewModel: VentaViewModel,
    hasCameraPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    val estado by viewModel.estado.collectAsState()
    val context = LocalContext.current

    // Ruta de la foto que la cámara está tomando (se confirma cuando vuelve con éxito)
    var rutaFotoPendiente by rememberSaveable { mutableStateOf<String?>(null) }

    // Recurso nativo: cámara del teléfono para fotografiar la boleta o vale
    val tomarFoto = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito ->
        val ruta = rutaFotoPendiente
        if (exito && ruta != null) {
            viewModel.onComprobanteChange(ruta)
        } else {
            ruta?.let { File(it).delete() } // se canceló: se borra el archivo vacío
        }
        rutaFotoPendiente = null
    }

    // Alternativa: elegir una foto de la galería (útil en el emulador)
    val elegirDeGaleria = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val ruta = ComprobanteHelper.copiarDesdeGaleria(context, uri)
            if (ruta != null) viewModel.onComprobanteChange(ruta)
            else Toast.makeText(context, "No se pudo leer la imagen", Toast.LENGTH_SHORT).show()
        }
    }

    // Cuando el ViewModel confirma el envío, la pantalla vuelve a la lista
    LaunchedEffect(estado.guardadoExitosamente) {
        if (estado.guardadoExitosamente) {
            Toast.makeText(context, "Venta enviada: queda en revisión", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
            viewModel.limpiarGuardadoExitoso()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (estado.ventaSeleccionadaId == null) "Registrar venta"
                        else "Corregir venta"
                    )
                },
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
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {

        // Si el encargado la observó, se muestra lo que hay que corregir
        estado.observacionRevision?.let {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Observación del encargado: $it",
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Convenio o servicio: se elige del catálogo (Room, actualizado desde la API)
        SelectorLista(
            etiqueta = "Convenio / servicio",
            valor = estado.tipoServicio,
            opciones = estado.convenios.map { "${it.nombre} (${it.categoria})" },
            onSeleccion = { indice -> viewModel.onConvenioSeleccionado(estado.convenios[indice]) },
            icono = Icons.Default.Handshake,
            error = estado.errores.tipoServicio,
            ayuda = estado.convenios.firstOrNull { it.id == estado.convenioId }?.condiciones
        )

        // Cliente ficticio: se elige de la tabla clientes
        SelectorLista(
            etiqueta = "Cliente (ficticio)",
            valor = estado.cliente,
            opciones = estado.clientes.map { "${it.nombre} · ${it.rut}" },
            onSeleccion = { indice -> viewModel.onClienteSeleccionado(estado.clientes[indice]) },
            icono = Icons.Default.Person,
            error = estado.errores.cliente
        )

        // Campo fecha: se elige en un calendario (no se escribe a mano)
        SelectorFecha(
            valor = estado.fechaOperacion,
            onFechaSeleccionada = viewModel::onFechaChange,
            etiqueta = "Fecha de operación",
            error = estado.errores.fecha
        )

        // Campo monto (teclado numérico)
        CampoVenta(
            valor = estado.monto,
            onValorChange = viewModel::onMontoChange,
            etiqueta = "Monto",
            icono = Icons.Default.AttachMoney,
            error = estado.errores.monto,
            tipoTeclado = KeyboardType.Number
        )

        // Campo código de autorización
        CampoVenta(
            valor = estado.codigoAutorizacion,
            onValorChange = viewModel::onCodigoChange,
            etiqueta = "Código de autorización",
            icono = Icons.Default.QrCode,
            error = estado.errores.codigo,
            ayuda = "Ej: AUT-12345 (6 a 12 letras, números o guiones)"
        )

        // Recurso nativo: la cámara lee un QR ficticio con el código de autorización
        OutlinedButton(
            onClick = { navController.navigate("qrVenta") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.QrCodeScanner, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Escanear QR del código")
        }

        estado.respaldo?.let {
            Text(
                text = "Respaldo adjunto → $it",
                style = MaterialTheme.typography.bodySmall
            )
        }

        // ---------- Comprobante: foto de la boleta o vale ----------
        Spacer(Modifier.padding(4.dp))
        Text("Comprobante (foto de boleta o vale)", style = MaterialTheme.typography.titleSmall)

        FotoComprobante(ruta = estado.comprobante)

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = {
                    if (!hasCameraPermission) {
                        // El permiso se pide con el launcher de MainActivity
                        onRequestPermission()
                    } else {
                        val archivo = ComprobanteHelper.crearArchivo(context)
                        rutaFotoPendiente = archivo.absolutePath
                        try {
                            tomarFoto.launch(ComprobanteHelper.uriParaCamara(context, archivo))
                        } catch (e: Exception) {
                            rutaFotoPendiente = null
                            Toast.makeText(context, "No hay app de cámara disponible", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Tomar foto")
            }

            OutlinedButton(
                onClick = {
                    elegirDeGaleria.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Galería")
            }
        }

        estado.errores.comprobante?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        // Campo observaciones (opcional)
        CampoVenta(
            valor = estado.observaciones,
            onValorChange = viewModel::onObservacionesChange,
            etiqueta = "Observaciones",
            icono = Icons.Default.Description,
            unaLinea = false
        )

        estado.errorGeneral?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        // Botón: enviar (la venta queda "En revisión" para el encargado)
        Button(
            onClick = { viewModel.guardarVenta() },
            enabled = !estado.guardando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (estado.ventaSeleccionadaId == null) "Enviar venta" else "Reenviar venta")
        }
    }
    }
} // fin VentaFormScreen

// El DatePicker trabaja en milisegundos UTC: se usa UTC para no correr la fecha un día
private fun formatoFechaUtc() = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
    timeZone = TimeZone.getTimeZone("UTC")
    isLenient = false
}

// "dd/mm/aaaa" -> milisegundos UTC (null si el texto no es una fecha válida)
private fun fechaAMillis(fecha: String): Long? =
    try {
        formatoFechaUtc().parse(fecha.trim())?.time
    } catch (e: Exception) {
        null
    }

// Campo de fecha: al tocarlo abre un calendario; no se puede elegir una fecha futura
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorFecha(
    valor: String,
    onFechaSeleccionada: (String) -> Unit,
    etiqueta: String,
    error: String? = null
) {
    var mostrarCalendario by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = valor,
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta) },
            leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = etiqueta) },
            trailingIcon = {
                if (error != null) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            },
            isError = error != null,
            supportingText = {
                if (error != null) {
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                } else {
                    Text(text = "Toca para elegir la fecha")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        // Capa transparente encima del campo: al tocarlo se abre el calendario
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { mostrarCalendario = true }
        )
    }

    if (mostrarCalendario) {
        // Hoy (en la zona del teléfono) pasado a UTC: es el último día que se puede elegir
        val hoyMillis = fechaAMillis(fechaDeHoy()) ?: System.currentTimeMillis()

        val estadoCalendario = rememberDatePickerState(
            initialSelectedDateMillis = fechaAMillis(valor) ?: hoyMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis <= hoyMillis
            }
        )

        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        estadoCalendario.selectedDateMillis?.let {
                            onFechaSeleccionada(formatoFechaUtc().format(Date(it)))
                        }
                        mostrarCalendario = false
                    }
                ) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCalendario = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = estadoCalendario)
        }
    }
} // fin SelectorFecha

// Campo de texto con ícono y mensaje de error, igual en todo el formulario
@Composable
private fun CampoVenta(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    error: String? = null,
    ayuda: String? = null,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    unaLinea: Boolean = true
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        label = { Text(etiqueta) },
        leadingIcon = {
            Icon(icono, contentDescription = etiqueta)
        },
        // Ícono de error si el campo es inválido
        trailingIcon = {
            if (error != null) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        isError = error != null,
        // Muestra el error; si no hay, el texto de ayuda (si existe)
        supportingText = {
            if (error != null) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            } else if (ayuda != null) {
                Text(text = ayuda)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado),
        singleLine = unaLinea,
        minLines = if (unaLinea) 1 else 3,
        modifier = Modifier.fillMaxWidth()
    )
} // fin CampoVenta
