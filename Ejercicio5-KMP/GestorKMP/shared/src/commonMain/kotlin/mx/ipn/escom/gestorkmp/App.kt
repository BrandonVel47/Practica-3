package mx.ipn.escom.gestorkmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import mx.ipn.escom.gestorkmp.data.PreferencesRepository
import mx.ipn.escom.gestorkmp.ui.browser.FileBrowserScreen
import mx.ipn.escom.gestorkmp.ui.theme.AppTheme
import mx.ipn.escom.gestorkmp.ui.theme.GestorTheme

@Composable
fun App() {
    val prefs = remember { PreferencesRepository() }
    val scope = rememberCoroutineScope()

    // El tema se lee de DataStore, así se conserva al cerrar la app
    val nombreTema by prefs.tema.collectAsState(initial = null)
    val tema = AppTheme.entries.find { it.name == nombreTema } ?: AppTheme.GUINDA

    GestorTheme(theme = tema) {
        FileBrowserScreen(
            temaActual = tema,
            onCambiarTema = {
                val nuevo = if (tema == AppTheme.GUINDA) AppTheme.AZUL else AppTheme.GUINDA
                scope.launch { prefs.guardarTema(nuevo.name) }
            }
        )
    }
}