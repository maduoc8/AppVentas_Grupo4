package com.example.appventas.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.appventas.model.ESTADO_OBSERVADA
import com.example.appventas.model.ESTADO_RECHAZADA
import com.example.appventas.model.ESTADO_VALIDADA
import java.io.File

// Piezas de pantalla que se repiten en varias secciones de la app

// Etiqueta de color con el estado de la venta
@Composable
fun EstadoChip(estado: String) {
    val (fondo, texto) = when (estado) {
        ESTADO_VALIDADA -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)   // verde
        ESTADO_OBSERVADA -> Color(0xFFFFF3E0) to Color(0xFFE65100)  // naranjo
        ESTADO_RECHAZADA -> Color(0xFFFFEBEE) to Color(0xFFC62828)  // rojo
        else -> Color(0xFFE3F2FD) to Color(0xFF1565C0)              // azul: en revisión
    }

    Surface(color = fondo, contentColor = texto, shape = RoundedCornerShape(50)) {
        Text(
            text = estado,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
} // fin EstadoChip

// Fila de filtros: "Todas" + una opción por cada valor (null = todas)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltroOpciones(
    opciones: List<String>,
    seleccionado: String?,
    onSeleccion: (String?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = seleccionado == null,
            onClick = { onSeleccion(null) },
            label = { Text("Todas") }
        )
        opciones.forEach { opcion ->
            FilterChip(
                selected = seleccionado == opcion,
                onClick = { onSeleccion(opcion) },
                label = { Text(opcion) }
            )
        }
    }
} // fin FiltroOpciones

// Campo que al tocarlo abre una lista para elegir (convenios, clientes)
@Composable
fun SelectorLista(
    etiqueta: String,
    valor: String,
    opciones: List<String>,
    onSeleccion: (Int) -> Unit,
    icono: ImageVector,
    error: String? = null,
    ayuda: String? = null
) {
    var expandido by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = valor,
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta) },
            leadingIcon = { Icon(icono, contentDescription = etiqueta) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = "Abrir lista") },
            isError = error != null,
            supportingText = {
                if (error != null) {
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                } else if (ayuda != null) {
                    Text(text = ayuda)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Capa transparente encima del campo: al tocarlo se abre la lista
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expandido = true }
        )

        DropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            opciones.forEachIndexed { indice, opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        expandido = false
                        onSeleccion(indice)
                    }
                )
            }
        }
    }
} // fin SelectorLista

// Foto de la boleta o vale (o un aviso si no hay)
@Composable
fun FotoComprobante(ruta: String?, modifier: Modifier = Modifier) {
    if (ruta == null) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .height(100.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("Sin comprobante adjunto")
            }
        }
    } else {
        AsyncImage(
            model = File(ruta),
            contentDescription = "Foto del comprobante",
            contentScale = ContentScale.Fit,
            modifier = modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(12.dp))
        )
    }
} // fin FotoComprobante

// Una línea "Etiqueta: valor" con la etiqueta en negrita
@Composable
fun Dato(etiqueta: String, valor: String) {
    Row {
        Text(text = "$etiqueta: ", fontWeight = FontWeight.Bold)
        Text(text = valor)
    }
} // fin Dato
