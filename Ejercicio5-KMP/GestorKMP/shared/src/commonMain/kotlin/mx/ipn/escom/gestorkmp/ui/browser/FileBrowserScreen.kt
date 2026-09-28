package mx.ipn.escom.gestorkmp.ui.browser

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.ipn.escom.gestorkmp.data.FileItem
import mx.ipn.escom.gestorkmp.data.SortOrder
import mx.ipn.escom.gestorkmp.ui.theme.AppTheme
import mx.ipn.escom.gestorkmp.ui.viewer.FileViewerScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBrowserScreen(
    temaActual: AppTheme,
    onCambiarTema: () -> Unit,
    viewModel: FileBrowserViewModel = viewModel { FileBrowserViewModel() }
) {
    val state by viewModel.state.collectAsState()

    // Si hay un archivo abierto, mostramos el visor en lugar de la lista
    state.abierto?.let { archivo ->
        FileViewerScreen(
            item = archivo,
            onBack = viewModel::cerrarVisor,
            cargarTexto = viewModel::leerTexto,
            cargarBytes = viewModel::leerBytes
        )
        return
    }

    var mostrarNuevaCarpeta by remember { mutableStateOf(false) }
    var opcionesDe by remember { mutableStateOf<FileItem?>(null) }
    var porRenombrar by remember { mutableStateOf<FileItem?>(null) }
    var porEliminar by remember { mutableStateOf<FileItem?>(null) }
    var buscando by remember { mutableStateOf(false) }
    var menuOrden by remember { mutableStateOf(false) }

    // Al cambiar de carpeta se cierra la búsqueda
    LaunchedEffect(state.rutaVisible) { buscando = false }

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
                    TextButton(onClick = {
                        buscando = !buscando
                        if (!buscando) viewModel.setQuery("")
                    }) { Text("🔍") }

                    Box {
                        TextButton(onClick = { menuOrden = true }) { Text("⇅") }
                        DropdownMenu(
                            expanded = menuOrden,
                            onDismissRequest = { menuOrden = false }
                        ) {
                            SortOrder.entries.forEach { orden ->
                                DropdownMenuItem(
                                    text = {
                                        Text((if (orden == state.orden) "✓ " else "    ") + orden.etiqueta)
                                    },
                                    onClick = {
                                        viewModel.setOrden(orden)
                                        menuOrden = false
                                    }
                                )
                            }
                        }
                    }

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
        Column(Modifier.fillMaxSize().padding(padding)) {

            if (buscando) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::setQuery,
                    placeholder = { Text("Buscar en esta carpeta") },
                    singleLine = true,
                    trailingIcon = {
                        TextButton(onClick = {
                            viewModel.setQuery("")
                            buscando = false
                        }) { Text("✕") }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Box(Modifier.fillMaxWidth().weight(1f)) {
                when {
                    state.isLoading && state.items.isEmpty() ->
                        CircularProgressIndicator(Modifier.align(Alignment.Center))

                    state.items.isEmpty() ->
                        Text(
                            if (state.query.isNotBlank()) "Sin resultados" else "Carpeta vacía",
                            Modifier.align(Alignment.Center)
                        )

                    else -> LazyColumn(Modifier.fillMaxSize()) {
                        items(state.items, key = { it.path }) { item ->
                            FileRow(
                                item = item,
                                onClick = { viewModel.abrir(item) },
                                onLongClick = { opcionesDe = item }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    // ---------- Diálogo: opciones (mantener presionado) ----------
    opcionesDe?.let { item ->
        AlertDialog(
            onDismissRequest = { opcionesDe = null },
            title = { Text("${item.type.icono}  ${item.name}") },
            text = {
                Column {
                    TextButton(onClick = {
                        opcionesDe = null
                        viewModel.abrir(item)
                    }) { Text("Abrir") }
                    TextButton(onClick = {
                        opcionesDe = null
                        porRenombrar = item
                    }) { Text("Renombrar") }
                    TextButton(onClick = {
                        opcionesDe = null
                        porEliminar = item
                    }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(onClick = { opcionesDe = null }) { Text("Cerrar") }
            }
        )
    }

    // ---------- Diálogo: nueva carpeta ----------
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

    // ---------- Diálogo: renombrar ----------
    porRenombrar?.let { item ->
        var nombre by remember(item) { mutableStateOf(item.name) }
        AlertDialog(
            onDismissRequest = { porRenombrar = null },
            title = { Text("Renombrar") },
            text = {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nuevo nombre") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    enabled = nombre.isNotBlank(),
                    onClick = {
                        viewModel.rename(item, nombre)
                        porRenombrar = null
                    }
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { porRenombrar = null }) { Text("Cancelar") }
            }
        )
    }

    // ---------- Diálogo: confirmar eliminación ----------
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

    // ---------- Diálogo: errores ----------
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