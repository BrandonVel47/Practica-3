package mx.ipn.escom.gestorkmp.ui.browser

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.ipn.escom.gestorkmp.data.FileItem
import mx.ipn.escom.gestorkmp.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBrowserScreen(
    temaActual: AppTheme,
    onCambiarTema: () -> Unit,
    viewModel: FileBrowserViewModel = viewModel { FileBrowserViewModel() }
) {
    val state by viewModel.state.collectAsState()
    var mostrarNuevaCarpeta by remember { mutableStateOf(false) }
    var porEliminar by remember { mutableStateOf<FileItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Gestor de Archivos")
                        Text(state.rutaVisible, style = MaterialTheme.typography.bodySmall)
                    }
                },
                navigationIcon = {
                    if (!state.isRoot) {
                        TextButton(onClick = viewModel::goUp) {
                            Text("←", style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                },
                actions = {
                    TextButton(onClick = onCambiarTema) {
                        Text(if (temaActual == AppTheme.GUINDA) "Azul" else "Guinda")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarNuevaCarpeta = true }) {
                Text("+", style = MaterialTheme.typography.headlineSmall)
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading && state.items.isEmpty() ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))

                state.items.isEmpty() ->
                    Text("Carpeta vacía", Modifier.align(Alignment.Center))

                else -> LazyColumn(Modifier.fillMaxSize()) {
                    items(state.items, key = { it.path }) { item ->
                        FileRow(
                            item = item,
                            onClick = { viewModel.openFolder(item) },
                            onLongClick = { porEliminar = item }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    // Diálogo: nueva carpeta
    if (mostrarNuevaCarpeta) {
        var nombre by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { mostrarNuevaCarpeta = false },
            title = { Text("Nueva carpeta") },
            text = {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    enabled = nombre.isNotBlank(),
                    onClick = {
                        viewModel.createFolder(nombre)
                        mostrarNuevaCarpeta = false
                    }
                ) { Text("Crear") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarNuevaCarpeta = false }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo: confirmar eliminación
    porEliminar?.let { item ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("¿Eliminar?") },
            text = {
                Text(
                    "Se eliminará \"${item.name}\"" +
                            if (item.isDirectory) " y todo su contenido." else "."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(item)
                    porEliminar = null
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { porEliminar = null }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo: errores
    state.error?.let { mensaje ->
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            title = { Text("Aviso") },
            text = { Text(mensaje) },
            confirmButton = {
                TextButton(onClick = viewModel::clearError) { Text("Aceptar") }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileRow(item: FileItem, onClick: () -> Unit, onLongClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(item.name) },
        supportingContent = {
            Text(if (item.isDirectory) "Carpeta" else formatSize(item.size))
        },
        leadingContent = {
            Text(item.type.icono, style = MaterialTheme.typography.headlineSmall)
        },
        modifier = Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)
    )
}

private fun formatSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> "${bytes / (1024 * 1024)} MB"
}