package mx.ipn.escom.gestorkmp.platform

import okio.FileSystem
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSLibraryDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

actual val platformFileSystem: FileSystem = FileSystem.SYSTEM

actual fun appRootDirectory(): String =
    NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
        .first() as String

actual fun preferencesFilePath(): String {
    val library = NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, true)
        .first() as String
    return "$library/gestor.preferences_pb"
}