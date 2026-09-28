package mx.ipn.escom.gestorkmp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Temas disponibles en la app. */
enum class AppTheme(val etiqueta: String) {
    GUINDA("Guinda IPN"),
    AZUL("Azul ESCOM")
}

// ---------- Tema Guinda (IPN) ----------
// Ajusta estos valores si tu equipo usa otros códigos de color.
private val GuindaLight = lightColorScheme(
    primary = Color(0xFF6C1D45),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFD9E4),
    onPrimaryContainer = Color(0xFF3E0021),
    secondary = Color(0xFF8C5A6E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF5DCE5),
    onSecondaryContainer = Color(0xFF331520)
)

private val GuindaDark = darkColorScheme(
    primary = Color(0xFFFFB0CB),
    onPrimary = Color(0xFF55102F),
    primaryContainer = Color(0xFF6C1D45),
    onPrimaryContainer = Color(0xFFFFD9E4),
    secondary = Color(0xFFE2BDCA),
    onSecondary = Color(0xFF422933),
    secondaryContainer = Color(0xFF5A3F4A),
    onSecondaryContainer = Color(0xFFF5DCE5)
)

// ---------- Tema Azul (ESCOM) ----------
private val AzulLight = lightColorScheme(
    primary = Color(0xFF003D79),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6E3FF),
    onPrimaryContainer = Color(0xFF001B3D),
    secondary = Color(0xFF4F6078),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD7E3FF),
    onSecondaryContainer = Color(0xFF0B1C31)
)

private val AzulDark = darkColorScheme(
    primary = Color(0xFFA9C7FF),
    onPrimary = Color(0xFF003063),
    primaryContainer = Color(0xFF00468A),
    onPrimaryContainer = Color(0xFFD6E3FF),
    secondary = Color(0xFFB7C8E1),
    onSecondary = Color(0xFF213247),
    secondaryContainer = Color(0xFF38485F),
    onSecondaryContainer = Color(0xFFD7E3FF)
)

/**
 * Tema principal de la app.
 * - theme: Guinda o Azul, elegido por el usuario.
 * - darkTheme: por defecto sigue el modo claro/oscuro del sistema.
 */
@Composable
fun GestorTheme(
    theme: AppTheme = AppTheme.GUINDA,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        AppTheme.GUINDA -> if (darkTheme) GuindaDark else GuindaLight
        AppTheme.AZUL -> if (darkTheme) AzulDark else AzulLight
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
