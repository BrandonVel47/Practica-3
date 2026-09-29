package mx.ipn.escom.gestorkmp.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import kotlin.math.max
import kotlin.math.min

@Composable
actual fun ManejarAtras(activo: Boolean, alPresionar: () -> Unit) {
    // En iOS no existe un botón "atrás" del sistema:
    // la navegación hacia atrás se hace con el botón ← de la barra superior.
}

actual fun decodificarImagen(bytes: ByteArray, maxLado: Int): ImageBitmap {
    val original = Image.makeFromEncoded(bytes)
    val escala = min(1f, maxLado.toFloat() / max(original.width, original.height))
    if (escala >= 1f) return original.toComposeImageBitmap()

    // Dibujar la imagen reducida en una superficie nueva
    val ancho = (original.width * escala).toInt().coerceAtLeast(1)
    val alto = (original.height * escala).toInt().coerceAtLeast(1)
    val superficie = Surface.makeRasterN32Premul(ancho, alto)
    superficie.canvas.drawImageRect(original, Rect.makeWH(ancho.toFloat(), alto.toFloat()))
    return superficie.makeImageSnapshot().toComposeImageBitmap()
}