package mx.ipn.escom.gestorkmp.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Botón / gesto "atrás" del sistema.
 * Android: BackHandler de androidx.activity.
 * iOS: no existe un botón atrás del sistema; se usa el botón ← de la barra superior.
 */
@Composable
expect fun ManejarAtras(activo: Boolean, alPresionar: () -> Unit)

/**
 * Decodifica una imagen reduciéndola para que su lado mayor no pase de [maxLado] píxeles.
 * Evita cierres por falta de memoria con fotos grandes y sirve para generar miniaturas.
 * Android: BitmapFactory con inSampleSize.
 * iOS: Skia (motor gráfico de Compose Multiplatform).
 */
expect fun decodificarImagen(bytes: ByteArray, maxLado: Int): ImageBitmap