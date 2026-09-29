package mx.ipn.escom.gestorkmp.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import mx.ipn.escom.gestorkmp.platform.preferencesFilePath
import okio.Path.Companion.toPath

/** Criterios de ordenamiento de la lista. */
enum class SortOrder(val etiqueta: String) {
    NOMBRE("Nombre"),
    FECHA("Fecha"),
    TAMANO("Tamaño")
}

/**
 * Única instancia de DataStore en toda la app.
 * DataStore no permite abrir dos instancias sobre el mismo archivo.
 */
object AppDataStore {
    val instance: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.createWithPath(
            produceFile = { preferencesFilePath().toPath() }
        )
    }
}

/**
 * Preferencias persistentes: tema, orden, última carpeta,
 * favoritos y archivos recientes.
 */
class PreferencesRepository(
    private val ds: DataStore<Preferences> = AppDataStore.instance
) {
    private object Keys {
        val TEMA = stringPreferencesKey("tema")
        val ORDEN = stringPreferencesKey("orden")
        val ULTIMA_CARPETA = stringPreferencesKey("ultima_carpeta")
        val FAVORITOS = stringSetPreferencesKey("favoritos")
        val RECIENTES = stringPreferencesKey("recientes") // rutas separadas por salto de línea
    }

    private val maxRecientes = 20

    // ---------- Tema, orden y última carpeta ----------

    /** Nombre del tema guardado (GUINDA / AZUL) o null si nunca se eligió. */
    val tema: Flow<String?> = ds.data.map { it[Keys.TEMA] }

    val orden: Flow<SortOrder> = ds.data.map { prefs ->
        SortOrder.entries.find { it.name == prefs[Keys.ORDEN] } ?: SortOrder.NOMBRE
    }

    suspend fun ultimaCarpeta(): String? = ds.data.first()[Keys.ULTIMA_CARPETA]

    suspend fun guardarTema(nombre: String) {
        ds.edit { it[Keys.TEMA] = nombre }
    }

    suspend fun guardarOrden(orden: SortOrder) {
        ds.edit { it[Keys.ORDEN] = orden.name }
    }

    suspend fun guardarUltimaCarpeta(ruta: String) {
        ds.edit { it[Keys.ULTIMA_CARPETA] = ruta }
    }

    // ---------- Favoritos ----------

    val favoritos: Flow<Set<String>> = ds.data.map { it[Keys.FAVORITOS] ?: emptySet() }

    suspend fun alternarFavorito(ruta: String) {
        ds.edit { prefs ->
            val actuales = prefs[Keys.FAVORITOS] ?: emptySet()
            prefs[Keys.FAVORITOS] = if (ruta in actuales) actuales - ruta else actuales + ruta
        }
    }

    suspend fun quitarFavorito(ruta: String) {
        ds.edit { prefs ->
            prefs[Keys.FAVORITOS] = (prefs[Keys.FAVORITOS] ?: emptySet()) - ruta
        }
    }

    // ---------- Recientes (el más nuevo primero) ----------

    val recientes: Flow<List<String>> = ds.data.map { leerRecientes(it) }

    suspend fun registrarReciente(ruta: String) {
        ds.edit { prefs ->
            val lista = listOf(ruta) + leerRecientes(prefs).filter { it != ruta }
            prefs[Keys.RECIENTES] = lista.take(maxRecientes).joinToString("\n")
        }
    }

    suspend fun quitarReciente(ruta: String) {
        ds.edit { prefs ->
            prefs[Keys.RECIENTES] = leerRecientes(prefs).filter { it != ruta }.joinToString("\n")
        }
    }

    suspend fun limpiarRecientes() {
        ds.edit { it.remove(Keys.RECIENTES) }
    }

    private fun leerRecientes(prefs: Preferences): List<String> =
        prefs[Keys.RECIENTES]?.split('\n')?.filter { it.isNotBlank() } ?: emptyList()
}