@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package mx.ipn.escom.gestorkmp.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UniformTypeIdentifiers.UTTypeItem
import platform.darwin.NSObject

/** Pantalla visible en este momento, para presentar encima el selector o la hoja de compartir. */
private fun controladorVisible(): UIViewController? {
    var vc = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (vc?.presentedViewController != null) vc = vc.presentedViewController
    return vc
}

@Composable
actual fun rememberFileImporter(
    destinoPara: (nombreOriginal: String) -> String,
    onResultado: (Result<String>) -> Unit
): () -> Unit {
    val destinoActual by rememberUpdatedState(destinoPara)
    val resultadoActual by rememberUpdatedState(onResultado)

    // El delegado se guarda con remember para que no lo borre el recolector de memoria
    val delegado = remember {
        object : NSObject(), UIDocumentPickerDelegateProtocol {
            override fun documentPicker(
                controller: UIDocumentPickerViewController,
                didPickDocumentsAtURLs: List<*>
            ) {
                val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL ?: return
                resultadoActual(runCatching {
                    val nombre = url.lastPathComponent ?: "archivo"
                    val destino = destinoActual(nombre)
                    val acceso = url.startAccessingSecurityScopedResource()
                    try {
                        val ok = NSFileManager.defaultManager.copyItemAtURL(
                            url, NSURL.fileURLWithPath(destino), null
                        )
                        if (!ok) error("No se pudo copiar el archivo")
                    } finally {
                        if (acceso) url.stopAccessingSecurityScopedResource()
                    }
                    destino.substringAfterLast('/')
                })
            }
        }
    }

    return remember {
        {
            val picker = UIDocumentPickerViewController(
                forOpeningContentTypes = listOf(UTTypeItem),
                asCopy = true
            )
            picker.delegate = delegado
            controladorVisible()?.presentViewController(picker, animated = true, completion = null)
        }
    }
}

actual fun shareFile(path: String, mimeType: String) {
    val url = NSURL.fileURLWithPath(path)
    val hoja = UIActivityViewController(activityItems = listOf(url), applicationActivities = null)
    controladorVisible()?.presentViewController(hoja, animated = true, completion = null)
}