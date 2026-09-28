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
import mx.ipn.escom.gestorkmp.data.PreferencesRepository
import mx.ipn.escom.gestorkmp.data.SortOrder
import okio.Path
import okio.Path.Companion.toPath

/** Estado que observa la pantalla. */
data class BrowserState(
    val rutaVisible: String = "Inicio",
    val isRoot: Boolean = true,
    val items: List<FileItem> = emptyList(), // ya filtrados y ordenados
    val query: String = "",
    val orden: SortOrder = SortOrder.NOMBRE,
    val abierto: FileItem? = null,           // archivo mostrado en el visor
    val isLoading: Boolean = false,
    val error: String? = null
)

class FileBrowserViewModel(
    private val repo: FileRepository = FileRepository(),
    private val prefs: PreferencesRepository = PreferencesRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(BrowserState())
    val state: StateFlow<BrowserState> = _state.asStateFlow()

    private var current: Path = repo.rootPath
    private var todos: List<FileItem> = emptyList()

    init {
        viewModelScope.launch {
            runCatching { repo.seedIfEmpty() }
            // Restaurar la última carpeta visitada, si todavía existe
            prefs.ultimaCarpeta()?.toPath()?.let { guardada ->
                val dentroDeRaiz = guardada.toString().startsWith(repo.rootPath.toString())
                if (dentroDeRaiz && repo.exists(guardada)) current = guardada
            }
            load()
        }
        // Observar el criterio de orden guardado
        viewModelScope.launch {
            prefs.orden.collect { orden ->
                _state.update { it.copy(orden = orden) }
                publicar()
            }
        }
    }

    // ---------- Navegación y apertura ----------

    /** Carpeta: entra en ella. Archivo: lo abre en el visor. */
    fun abrir(item: FileItem) {
        if (item.isDirectory) navegarA(item.path.toPath())
        else _state.update { it.copy(abierto = item) }
    }

    fun cerrarVisor() {
        _state.update { it.copy(abierto = null) }
    }

    fun goUp() {
        if (current != repo.rootPath) navegarA(current.parent ?: repo.rootPath)
    }

    fun refresh() {
        viewModelScope.launch { load() }
    }

    private fun navegarA(destino: Path) {
        current = destino
        _state.update { it.copy(query = "") }
        viewModelScope.launch {
            prefs.guardarUltimaCarpeta(destino.toString())
            load()
        }
    }

    // ---------- Lectura para los visores ----------

    suspend fun leerTexto(item: FileItem): String = repo.readText(item.path.toPath())

    suspend fun leerBytes(item: FileItem): ByteArray = repo.readBytes(item.path.toPath())

    // ---------- Búsqueda y orden ----------

    fun setQuery(texto: String) {
        _state.update { it.copy(query = texto) }
        publicar()
    }

    fun setOrden(orden: SortOrder) {
        viewModelScope.launch { prefs.guardarOrden(orden) }
    }

    // ---------- Operaciones ----------

    fun createFolder(name: String) {
        val limpio = validarNombre(name) ?: return
        viewModelScope.launch {
            runCatching { repo.createFolder(current, limpio) }
                .onFailure { showError("No se pudo crear la carpeta (¿ya existe?)") }
            load()
        }
    }

    fun rename(item: FileItem, nuevoNombre: String) {
        val limpio = validarNombre(nuevoNombre) ?: return
        if (limpio == item.name) return
        viewModelScope.launch {
            runCatching { repo.rename(item.path.toPath(), limpio) }
                .onFailure { showError(it.message ?: "No se pudo renombrar") }
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

    /** Devuelve el nombre limpio o null (y muestra error) si no es válido. */
    private fun validarNombre(nombre: String): String? {
        val limpio = nombre.trim()
        if (limpio.isEmpty() || limpio.contains('/') || limpio.contains('\\') ||
            limpio == "." || limpio == ".."
        ) {
            showError("Nombre no válido")
            return null
        }
        return limpio
    }

    // ---------- Internos ----------

    private suspend fun load() {
        _state.update { it.copy(isLoading = true) }
        runCatching { repo.list(current) }
            .onSuccess { lista ->
                todos = lista
                _state.update {
                    it.copy(
                        rutaVisible = rutaVisible(),
                        isRoot = current == repo.rootPath,
                        isLoading = false
                    )
                }
                publicar()
            }
            .onFailure { e ->
                _state.update {
                    it.copy(isLoading = false, error = "No se pudo abrir la carpeta: ${e.message}")
                }
            }
    }

    /** Aplica búsqueda y orden sobre la lista completa y publica el resultado. */
    private fun publicar() {
        val s = _state.value
        val texto = s.query.trim()
        val filtrados = if (texto.isEmpty()) todos
        else todos.filter { it.name.contains(texto, ignoreCase = true) }

        val carpetasPrimero = compareByDescending<FileItem> { it.isDirectory }
        val ordenados = when (s.orden) {
            SortOrder.NOMBRE -> filtrados.sortedWith(carpetasPrimero.thenBy { it.name.lowercase() })
            SortOrder.FECHA -> filtrados.sortedWith(carpetasPrimero.thenByDescending { it.lastModified })
            SortOrder.TAMANO -> filtrados.sortedWith(carpetasPrimero.thenByDescending { it.size })
        }
        _state.update { it.copy(items = ordenados) }
    }

    /** Muestra la ruta relativa a la raíz, p. ej. "Inicio / Documentos". */
    private fun rutaVisible(): String {
        val relativa = current.toString()
            .removePrefix(repo.rootPath.toString())
            .trimStart('/')
        return if (relativa.isEmpty()) "Inicio" else "Inicio / " + relativa.replace("/", " / ")
    }
}