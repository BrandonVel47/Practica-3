package mx.ipn.escom.gestorkmp.ui.browser

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mx.ipn.escom.gestorkmp.data.FileItem
import mx.ipn.escom.gestorkmp.data.FileRepository
import mx.ipn.escom.gestorkmp.data.PreferencesRepository
import mx.ipn.escom.gestorkmp.data.SortOrder
import mx.ipn.escom.gestorkmp.data.ThumbnailCache
import mx.ipn.escom.gestorkmp.platform.decodificarImagen
import mx.ipn.escom.gestorkmp.platform.mimeTypeDe
import mx.ipn.escom.gestorkmp.platform.shareFile
import okio.Path
import okio.Path.Companion.toPath

/** Pestañas de la barra inferior. */
enum class Pestana(val etiqueta: String, val icono: String) {
    ARCHIVOS("Archivos", "📂"),
    FAVORITOS("Favoritos", "⭐"),
    RECIENTES("Recientes", "🕘")
}

/** Elemento marcado para copiar o mover. */
data class Portapapeles(val item: FileItem, val mover: Boolean)

/** Estado que observa la pantalla. */
data class BrowserState(
    val pestana: Pestana = Pestana.ARCHIVOS,
    val rutaVisible: String = "Inicio",
    val isRoot: Boolean = true,
    val items: List<FileItem> = emptyList(),      // carpeta actual, filtrada y ordenada
    val favoritos: List<FileItem> = emptyList(),
    val recientes: List<FileItem> = emptyList(),
    val favoritosPaths: Set<String> = emptySet(),
    val query: String = "",
    val orden: SortOrder = SortOrder.NOMBRE,
    val abierto: FileItem? = null,                // archivo mostrado en el visor
    val portapapeles: Portapapeles? = null,
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
    private var favPaths: Set<String> = emptySet()
    private var recPaths: List<String> = emptyList()

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
        viewModelScope.launch {
            prefs.orden.collect { orden ->
                _state.update { it.copy(orden = orden) }
                publicar()
            }
        }
        viewModelScope.launch {
            prefs.favoritos.collect { favPaths = it; resolverListas() }
        }
        viewModelScope.launch {
            prefs.recientes.collect { recPaths = it; resolverListas() }
        }
    }

    // ---------- Pestañas, navegación y apertura ----------

    fun setPestana(pestana: Pestana) {
        _state.update { it.copy(pestana = pestana) }
    }

    /** Carpeta: entra en ella. Archivo: lo abre en el visor y lo registra en recientes. */
    fun abrir(item: FileItem) {
        if (item.isDirectory) {
            setPestana(Pestana.ARCHIVOS)
            navegarA(item.path.toPath())
        } else {
            _state.update { it.copy(abierto = item) }
            viewModelScope.launch { prefs.registrarReciente(item.path) }
        }
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

    /** Miniatura de una imagen (160 px), usando la caché si ya existe. */
    suspend fun miniatura(item: FileItem): ImageBitmap? {
        ThumbnailCache.get(item)?.let { return it }
        val imagen = runCatching {
            val bytes = repo.readBytes(item.path.toPath())
            withContext(Dispatchers.Default) { decodificarImagen(bytes, 160) }
        }.getOrNull() ?: return null
        ThumbnailCache.put(item, imagen)
        return imagen
    }

    // ---------- Favoritos y recientes ----------

    fun alternarFavorito(item: FileItem) {
        viewModelScope.launch { prefs.alternarFavorito(item.path) }
    }

    fun limpiarRecientes() {
        viewModelScope.launch { prefs.limpiarRecientes() }
    }

    // ---------- Copiar / mover ----------

    fun copiar(item: FileItem) = marcar(item, mover = false)

    fun mover(item: FileItem) = marcar(item, mover = true)

    private fun marcar(item: FileItem, mover: Boolean) {
        _state.update { it.copy(portapapeles = Portapapeles(item, mover), pestana = Pestana.ARCHIVOS) }
    }

    fun cancelarPortapapeles() {
        _state.update { it.copy(portapapeles = null) }
    }

    /** Pega el elemento marcado en la carpeta actual. */
    fun pegar() {
        val p = _state.value.portapapeles ?: return
        viewModelScope.launch {
            runCatching {
                val origen = p.item.path.toPath()
                if (p.mover) repo.move(origen, current) else repo.copy(origen, current)
            }.onSuccess {
                _state.update { it.copy(portapapeles = null) }
            }.onFailure {
                showError(it.message ?: "No se pudo completar la operación")
            }
            load()
        }
    }

    // ---------- Importar y compartir ----------

    /** Ruta libre en la carpeta actual para un archivo importado. */
    fun rutaParaImportar(nombreOriginal: String): String =
        repo.rutaDisponible(current, nombreOriginal).toString()

    fun onImportado(resultado: Result<String>) {
        resultado.onFailure { showError("No se pudo importar: ${it.message}") }
        refresh()
    }

    fun compartir(item: FileItem) {
        if (item.isDirectory) {
            showError("Solo se pueden compartir archivos, no carpetas")
            return
        }
        runCatching { shareFile(item.path, mimeTypeDe(item.name)) }
            .onFailure { showError("No se pudo compartir: ${it.message}") }
    }

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
                .onSuccess {
                    prefs.quitarFavorito(item.path)
                    prefs.quitarReciente(item.path)
                }
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
        // Por si algo se renombró, movió o eliminó
        resolverListas()
    }

    /** Convierte las rutas guardadas en elementos reales, descartando los que ya no existen. */
    private suspend fun resolverListas() {
        val favs = favPaths.mapNotNull { repo.info(it.toPath()) }
            .sortedWith(compareByDescending<FileItem> { it.isDirectory }.thenBy { it.name.lowercase() })
        val recs = recPaths.mapNotNull { repo.info(it.toPath()) }
        _state.update { it.copy(favoritos = favs, recientes = recs, favoritosPaths = favPaths) }
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