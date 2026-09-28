package mx.ipn.escom.gestorkmp.ui.browser

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.ipn.escom.gestorkmp.data.FileItem
import mx.ipn.escom.gestorkmp.data.SortOrder
import mx.ipn.escom.gestorkmp.platform.rememberFileImporter
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

    // Selector de archivos del sistema (expect/actual)
    val importar = rememberFileImporter(
        destinoPara = viewModel::rutaParaImportar,
        onResultado = viewModel::onImportado
    )

    var menuAgregar by remember { mutableStateOf(false) }
    var mostrarNuevaCarpeta by remember { mutableStateOf(false) }
    var opcionesDe by remember { mutableStateOf<FileItem?>(null) }
    var porRenombrar by remember { mutableStateOf<FileItem?>(null) }
    var porEliminar by remember { mutableStateOf<FileItem?>(null) }
    var buscando by remember { mutableStateOf(false) }
    var menuOrden by remember { mutableStateOf(false) }

    // Al cambiar de carpeta o de pestaña se cierra la búsqueda
    LaunchedEffect(state.rutaVisible, state.pestana) { buscando = false }

    val enArchivos = state.pestana == Pestana.ARCHIVOS

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (state.pestana) {
                        Pestana.ARCHIVOS -> Column {
                            Text("Gestor de Archivos")
                            Text(state.rutaVisible, style = MaterialTheme.typography.bodySmall)
                        }
                        Pestana.FAVORITOS -> Text("Favoritos")
                        Pestana.RECIENTES -> Text("Recientes")
                    }
                },
                navigationIcon = {
                    if (enArchivos && !state.isRoot) {
                        TextButton(onClick = viewModel::goUp) {
                            Text("←", style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                },
                actions = {
                    if (enArchivos) {
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
                    }
                    if (state.pestana == Pestana.RECIENTES && state.recientes.isNotEmpty()) {
                        TextButton(onClick = viewModel::limpiarRecientes) { Text("Limpiar") }
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
        bottomBar = {
            NavigationBar {
                Pestana.entries.forEach { p ->
                    NavigationBarItem(
                        selected = state.pestana == p,
                        onClick = { viewModel.setPestana(p) },
                        icon = { Text(p.icono) },
                        label = { Text(p.etiqueta) }
                    )
                }
            }
        },
        floatingActionButton = {
            // Oculto si no estamos en Archivos o si hay algo por pegar (para no tapar la barra)
            if (enArchivos && state.portapapeles == null) {
                Box {
                    FloatingActionButton(onClick = { menuAgregar = true }) {
                        Text("+", style = MaterialTheme.typography.headlineSmall)
                    }
                    DropdownMenu(
                        expanded = menuAgregar,
                        onDismissRequest = { menuAgregar = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("📁  Nueva carpeta") },
                            onClick = {
                                menuAgregar = false
                                mostrarNuevaCarpeta = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("📥  Importar archivo") },
                            onClick = {
                                menuAgregar = false
                                importar()
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            when (state.pestana) {
                Pestana.ARCHIVOS -> {
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

                    ListaArchivos(
                        items = state.items,
                        favoritos = state.favoritosPaths,
                        cargando = state.isLoading,
                        textoVacio = if (state.query.isNotBlank()) "Sin resultados" else "Carpeta vacía",
                        onClick = viewModel::abrir,
                        onLongClick = { opcionesDe = it }
                    )

                    // Barra para pegar lo que se copió o movió
                    state.portapapeles?.let { p ->
                        Surface(color = MaterialTheme.colorScheme.secondaryContainer) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    (if (p.mover) "Mover: " else "Copiar: ") + p.item.name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = viewModel::cancelarPortapapeles) { Text("Cancelar") }
                                Button(onClick = viewModel::pegar) { Text("Pegar aquí") }
                            }
                        }
                    }
                }

                Pestana.FAVORITOS -> ListaArchivos(
                    items = state.favoritos,
                    favoritos = state.favoritosPaths,
                    cargando = false,
                    textoVacio = "Aún no tienes favoritos.\nMantén presionado un archivo o carpeta\ny elige \"Añadir a favoritos\".",
                    onClick = viewModel::abrir,
                    onLongClick = { opcionesDe = it }
                )

                Pestana.RECIENTES -> ListaArchivos(
                    items = state.recientes,
                    favoritos = state.favoritosPaths,
                    cargando = false,
                    textoVacio = "Aún no has abierto archivos.",
                    onClick = viewModel::abrir,
                    onLongClick = { opcionesDe = it }
                )
            }
        }
    }

    // ---------- Diálogo: opciones (mantener presionado) ----------
    opcionesDe?.let { item ->
        val esFavorito = item.path in state.favoritosPaths
        fun cerrarY(accion: () -> Unit) {
            opcionesDe = null
            accion()
        }
        AlertDialog(
            onDismissRequest = { opcionesDe = null },
            title = {
                Text(
                    "${item.type.icono}  ${item.name}",
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            },
            text = {
                Column {
                    TextButton(onClick = { cerrarY { viewModel.abrir(item) } }) { Text("Abrir") }
                    TextButton(onClick = { cerrarY { viewModel.alternarFavorito(item) } }) {
                        Text(if (esFavorito) "★  Quitar de favoritos" else "☆  Añadir a favoritos")
                    }
                    if (!item.isDirectory) {
                        TextButton(onClick = { cerrarY { viewModel.compartir(item) } }) {
                            Text("Compartir / Exportar")
                        }
                    }
                    TextButton(onClick = { cerrarY { viewModel.copiar(item) } }) { Text("Copiar") }
                    TextButton(onClick = { cerrarY { viewModel.mover(item) } }) { Text("Mover") }
                    TextButton(onClick = { cerrarY { porRenombrar = item } }) { Text("Renombrar") }
                    TextButton(onClick = { cerrarY { porEliminar = item } }) {
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
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

/** Lista reutilizable para Archivos, Favoritos y Recientes. */
@Composable
private fun ColumnScope.ListaArchivos(
    items: List<FileItem>,
    favoritos: Set<String>,
    cargando: Boolean,
    textoVacio: String,
    onClick: (FileItem) -> Unit,
    onLongClick: (FileItem) -> Unit
) {
    Box(Modifier.fillMaxWidth().weight(1f)) {
        when {
            cargando && items.isEmpty() ->
                CircularProgressIndicator(Modifier.align(Alignment.Center))

            items.isEmpty() ->
                Text(
                    textoVacio,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp)
                )

            else -> LazyColumn(Modifier.fillMaxSize()) {
                items(items, key = { it.path }) { item ->
                    FileRow(
                        item = item,
                        esFavorito = item.path in favoritos,
                        onClick = { onClick(item) },
                        onLongClick = { onLongClick(item) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileRow(
    item: FileItem,
    esFavorito: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(item.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = {
            Text(if (item.isDirectory) "Carpeta" else formatSize(item.size))
        },
        leadingContent = {
            Text(item.type.icono, style = MaterialTheme.typography.headlineSmall)
        },
        trailingContent = if (esFavorito) {
            { Text("⭐") }
        } else null,
        modifier = Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)
    )
}

private fun formatSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> "${bytes / (1024 * 1024)} MB"
}