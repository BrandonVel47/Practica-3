package mx.ipn.escom.gestorkmp.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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

/** Preferencias de la sesión: tema, orden y última carpeta visitada. */
class PreferencesRepository(
    private val ds: DataStore<Preferences> = AppDataStore.instance
) {
    private object Keys {
        val TEMA = stringPreferencesKey("tema")
        val ORDEN = stringPreferencesKey("orden")
        val ULTIMA_CARPETA = stringPreferencesKey("ultima_carpeta")
    }

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
}