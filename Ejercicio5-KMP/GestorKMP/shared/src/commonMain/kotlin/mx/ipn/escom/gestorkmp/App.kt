package mx.ipn.escom.gestorkmp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mx.ipn.escom.gestorkmp.ui.theme.AppTheme
import mx.ipn.escom.gestorkmp.ui.theme.GestorTheme

@Composable
fun App() {
    // Temporal: más adelante el tema se guardará con DataStore.
    var tema by remember { mutableStateOf(AppTheme.GUINDA) }

    GestorTheme(theme = tema) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeContentPadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Gestor de Archivos",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("Tema actual: ${tema.etiqueta}")
                Button(onClick = { tema = AppTheme.GUINDA }) { Text("Tema Guinda") }
                OutlinedButton(onClick = { tema = AppTheme.AZUL }) { Text("Tema Azul") }
            }
        }
    }
}