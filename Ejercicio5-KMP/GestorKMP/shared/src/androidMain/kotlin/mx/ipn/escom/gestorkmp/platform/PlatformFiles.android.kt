package mx.ipn.escom.gestorkmp.platform

import android.content.Context
import okio.FileSystem

/** Guarda el Context de Android; se inicializa en MainActivity. */
object AndroidContext {
    lateinit var context: Context
}

actual val platformFileSystem: FileSystem = FileSystem.SYSTEM

actual fun appRootDirectory(): String =
    AndroidContext.context.filesDir.absolutePath

actual fun preferencesFilePath(): String =
    AndroidContext.context.noBackupFilesDir
        .resolve("gestor.preferences_pb")
        .absolutePath