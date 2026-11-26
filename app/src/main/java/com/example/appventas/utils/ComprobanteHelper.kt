package com.example.appventas.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

// Maneja los archivos de las fotos de boletas y vales (se guardan dentro de la app)
object ComprobanteHelper {

    // Carpeta privada de la app: files/comprobantes
    private fun carpeta(context: Context): File =
        File(context.filesDir, "comprobantes").apply { mkdirs() }

    // Archivo nuevo y vacío donde la cámara guardará la foto
    fun crearArchivo(context: Context): File =
        File(carpeta(context), "comprobante_${System.currentTimeMillis()}.jpg")

    // La cámara necesita una dirección "content://" para poder escribir en el archivo
    fun uriParaCamara(context: Context, archivo: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)

    // Copia una imagen elegida de la galería a la carpeta de la app y devuelve su ruta
    fun copiarDesdeGaleria(context: Context, uri: Uri): String? {
        return try {
            val destino = crearArchivo(context)
            context.contentResolver.openInputStream(uri)?.use { entrada ->
                destino.outputStream().use { salida -> entrada.copyTo(salida) }
            }
            destino.absolutePath
        } catch (e: Exception) {
            null
        }
    } // fin copiarDesdeGaleria
}
