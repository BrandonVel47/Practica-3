package mx.ipn.escom.gestorkmp.platform

import androidx.compose.runtime.Composable

/**
 * Selector de archivos del sistema para IMPORTAR.
 * Android: selector de documentos (ActivityResultContracts.OpenDocument).
 * iOS: UIDocumentPickerViewController.
 *
 * @param destinoPara recibe el nombre original y devuelve la ruta completa donde copiarlo.
 * @param onResultado recibe el nombre final del archivo importado o el error.
 * @return una función que, al llamarla, abre el selector.
 */
@Composable
expect fun rememberFileImporter(
    destinoPara: (nombreOriginal: String) -> String,
    onResultado: (Result<String>) -> Unit
): () -> Unit

/**
 * Hoja de compartir del sistema para EXPORTAR un archivo.
 * Android: Intent.ACTION_SEND con FileProvider.
 * iOS: UIActivityViewController.
 */
expect fun shareFile(path: String, mimeType: String)

/** Tipo MIME según la extensión (código común para ambas plataformas). */
fun mimeTypeDe(nombre: String): String =
    when (nombre.substringAfterLast('.', "").lowercase()) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "gif" -> "image/gif"
        "webp" -> "image/webp"
        "heic" -> "image/heic"
        "txt", "log", "kt", "swift" -> "text/plain"
        "md" -> "text/markdown"
        "csv" -> "text/csv"
        "json" -> "application/json"
        "xml" -> "text/xml"
        "html" -> "text/html"
        "pdf" -> "application/pdf"
        "mp3" -> "audio/mpeg"
        "m4a" -> "audio/mp4"
        "wav" -> "audio/wav"
        "mp4" -> "video/mp4"
        "mov" -> "video/quicktime"
        else -> "*/*"
    }