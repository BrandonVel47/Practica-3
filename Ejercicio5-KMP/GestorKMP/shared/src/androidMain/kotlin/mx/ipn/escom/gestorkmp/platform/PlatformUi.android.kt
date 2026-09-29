package mx.ipn.escom.gestorkmp.platform

import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

@Composable
actual fun ManejarAtras(activo: Boolean, alPresionar: () -> Unit) {
    BackHandler(enabled = activo, onBack = alPresionar)
}

actual fun decodificarImagen(bytes: ByteArray, maxLado: Int): ImageBitmap {
    // 1. Leer solo las dimensiones, sin cargar la imagen en memoria
    val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, limites)
    if (limites.outWidth <= 0 || limites.outHeight <= 0) {
        error("Formato de imagen no soportado")
    }

    // 2. Calcular cuánto reducir (potencias de 2: 1, 2, 4, 8...)
    var muestra = 1
    while (limites.outWidth / (muestra * 2) >= maxLado ||
        limites.outHeight / (muestra * 2) >= maxLado
    ) {
        muestra *= 2
    }

    // 3. Decodificar ya reducida
    val opciones = BitmapFactory.Options().apply { inSampleSize = muestra }
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opciones)
        ?: error("No se pudo decodificar la imagen")
    return bitmap.asImageBitmap()
}