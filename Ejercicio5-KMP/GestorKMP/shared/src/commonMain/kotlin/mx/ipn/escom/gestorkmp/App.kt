package mx.ipn.escom.gestorkmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import mx.ipn.escom.gestorkmp.ui.browser.FileBrowserScreen
import mx.ipn.escom.gestorkmp.ui.theme.AppTheme
import mx.ipn.escom.gestorkmp.ui.theme.GestorTheme

@Composable
fun App() {
    // Temporal: más adelante el tema se guardará con DataStore.
    var tema by remember { mutableStateOf(AppTheme.GUINDA) }

    GestorTheme(theme = tema) {
        FileBrowserScreen(
            temaActual = tema,
            onCambiarTema = {
                tema = if (tema == AppTheme.GUINDA) AppTheme.AZUL else AppTheme.GUINDA
            }
        )
    }
}