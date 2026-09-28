package mx.ipn.escom.gestorkmp.ui.browser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.ipn.escom.gestorkmp.data.FileItem
import mx.ipn.escom.gestorkmp.data.FileRepository
import okio.Path
import okio.Path.Companion.toPath

/** Estado que observa la pantalla. */
data class BrowserState(
    val rutaVisible: String = "Inicio",
    val isRoot: Boolean = true,
    val items: List<FileItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FileBrowserViewModel(
    private val repo: FileRepository = FileRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(BrowserState())
    val state: StateFlow<BrowserState> = _state.asStateFlow()

    private var current: Path = repo.rootPath

    init {
        viewModelScope.launch {
            runCatching { repo.seedIfEmpty() }
            load()
        }
    }

    fun openFolder(item: FileItem) {
        if (!item.isDirectory) return
        current = item.path.toPath()
        refresh()
    }

    fun goUp() {
        if (current == repo.rootPath) return
        current = current.parent ?: repo.rootPath
        refresh()
    }

    fun refresh() {
        viewModelScope.launch { load() }
    }

    fun createFolder(name: String) {
        val limpio = name.trim()
        if (limpio.isEmpty() || limpio.contains('/') || limpio.contains('\\')) {
            showError("Nombre de carpeta no válido")
            return
        }
        viewModelScope.launch {
            runCatching { repo.createFolder(current, limpio) }
                .onFailure { showError("No se pudo crear la carpeta (¿ya existe?)") }
            load()
        }
    }

    fun delete(item: FileItem) {
        viewModelScope.launch {
            runCatching { repo.delete(item.path.toPath()) }
                .onFailure { showError("No se pudo eliminar: ${it.message}") }
            load()
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun showError(msg: String) {
        _state.update { it.copy(error = msg) }
    }

    private suspend fun load() {
        _state.update { it.copy(isLoading = true) }
        runCatching { repo.list(current) }
            .onSuccess { items ->
                _state.update {
                    it.copy(
                        rutaVisible = rutaVisible(),
                        isRoot = current == repo.rootPath,
                        items = items,
                        isLoading = false
                    )
                }
            }
            .onFailure { e ->
                _state.update {
                    it.copy(isLoading = false, error = "No se pudo abrir la carpeta: ${e.message}")
                }
            }
    }

    /** Muestra la ruta relativa a la raíz, p. ej. "Inicio / Documentos". */
    private fun rutaVisible(): String {
        val relativa = current.toString()
            .removePrefix(repo.rootPath.toString())
            .trimStart('/')
        return if (relativa.isEmpty()) "Inicio" else "Inicio / " + relativa.replace("/", " / ")
    }
}