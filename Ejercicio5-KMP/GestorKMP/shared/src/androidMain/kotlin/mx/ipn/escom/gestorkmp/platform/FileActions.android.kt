package mx.ipn.escom.gestorkmp.platform

import android.content.Intent
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

@Composable
actual fun rememberFileImporter(
    destinoPara: (nombreOriginal: String) -> String,
    onResultado: (Result<String>) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val destinoActual by rememberUpdatedState(destinoPara)
    val resultadoActual by rememberUpdatedState(onResultado)

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult // el usuario canceló
        resultadoActual(runCatching {
            // 1. Obtener el nombre original del archivo
            val nombre = context.contentResolver
                .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
                ?: "archivo"

            // 2. Copiarlo dentro del sandbox de la app
            val destino = File(destinoActual(nombre))
            context.contentResolver.openInputStream(uri)?.use { entrada ->
                destino.outputStream().use { salida -> entrada.copyTo(salida) }
            } ?: error("No se pudo leer el archivo seleccionado")

            destino.name
        })
    }

    return { launcher.launch(arrayOf("*/*")) }
}

actual fun shareFile(path: String, mimeType: String) {
    val context = AndroidContext.context
    // FileProvider genera un content:// seguro para que otras apps lean el archivo
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        File(path)
    )
    val envio = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val selector = Intent.createChooser(envio, "Compartir").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(selector)
}