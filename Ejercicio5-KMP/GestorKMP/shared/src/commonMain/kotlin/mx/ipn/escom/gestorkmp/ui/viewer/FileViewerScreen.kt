package mx.ipn.escom.gestorkmp.ui.viewer

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mx.ipn.escom.gestorkmp.data.FileItem
import mx.ipn.escom.gestorkmp.data.FileType
import mx.ipn.escom.gestorkmp.platform.decodificarImagen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileViewerScreen(
    item: FileItem,
    onBack: () -> Unit,
    cargarTexto: suspend (FileItem) -> String,
    cargarBytes: suspend (FileItem) -> ByteArray
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("←", style = MaterialTheme.typography.headlineSmall)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (item.type) {
                FileType.TEXT -> TextViewer(item, cargarTexto)
                FileType.IMAGE -> ImageViewer(item, cargarBytes)
                else -> SinVistaPrevia(item)
            }
        }
    }
}

// ---------------- Texto ----------------

@Composable
private fun TextViewer(item: FileItem, cargar: suspend (FileItem) -> String) {
    val resultado by produceState<Result<String>?>(initialValue = null, item) {
        value = runCatching { cargar(item) }
    }

    Box(Modifier.fillMaxSize()) {
        val r = resultado
        when {
            r == null -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            r.isFailure -> Mensaje("No se pudo abrir el archivo:\n${r.exceptionOrNull()?.message}")
            else -> SelectionContainer {
                Text(
                    text = r.getOrDefault(""),
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .horizontalScroll(rememberScrollState())
                        .padding(16.dp)
                )
            }
        }
    }
}

// ---------------- Imagen ----------------

@Composable
private fun ImageViewer(item: FileItem, cargar: suspend (FileItem) -> ByteArray) {
    val resultado by produceState<Result<ImageBitmap>?>(initialValue = null, item) {
        value = runCatching {
            val bytes = cargar(item)
            // Máximo 2048 px por lado: suficiente para zoom y sin agotar la memoria
            withContext(Dispatchers.Default) { decodificarImagen(bytes, 2048) }
        }
    }

    // Estado de los gestos
    var escala by remember { mutableStateOf(1f) }
    var rotacion by remember { mutableStateOf(0f) }
    var desplazamiento by remember { mutableStateOf(Offset.Zero) }
    val ajustar = {
        escala = 1f
        rotacion = 0f
        desplazamiento = Offset.Zero
    }

    Box(Modifier.fillMaxSize().clipToBounds()) {
        val r = resultado
        when {
            r == null -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            r.isFailure -> Mensaje("No se pudo mostrar la imagen:\n${r.exceptionOrNull()?.message}")
            else -> {
                Image(
                    bitmap = r.getOrThrow(),
                    contentDescription = item.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        // Pinza = zoom, dos dedos girando = rotar, arrastrar = mover
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, giro ->
                                escala = (escala * zoom).coerceIn(0.5f, 6f)
                                rotacion += giro
                                desplazamiento += pan
                            }
                        }
                        // Doble toque = ajustar a pantalla
                        .pointerInput(Unit) {
                            detectTapGestures(onDoubleTap = { ajustar() })
                        }
                        .graphicsLayer {
                            scaleX = escala
                            scaleY = escala
                            rotationZ = rotacion
                            translationX = desplazamiento.x
                            translationY = desplazamiento.y
                        }
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                ) {
                    FilledTonalButton(onClick = { rotacion -= 90f }) { Text("⟲ 90°") }
                    FilledTonalButton(onClick = ajustar) { Text("Ajustar") }
                    FilledTonalButton(onClick = { rotacion += 90f }) { Text("⟳ 90°") }
                }
            }
        }
    }
}

// ---------------- Otros tipos ----------------

@Composable
private fun SinVistaPrevia(item: FileItem) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(item.type.icono, style = MaterialTheme.typography.displayLarge)
        Text(item.name, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(
            "Vista previa no disponible para este tipo de archivo.",
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun Mensaje(texto: String) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(texto, textAlign = TextAlign.Center)
    }
}