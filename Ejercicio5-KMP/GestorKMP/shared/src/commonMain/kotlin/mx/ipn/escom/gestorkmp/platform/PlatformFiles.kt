package mx.ipn.escom.gestorkmp.platform

import okio.FileSystem

/**
 * Sistema de archivos nativo de cada plataforma.
 * Android: java.io del dispositivo. iOS: POSIX/Foundation.
 */
expect val platformFileSystem: FileSystem

/**
 * Carpeta raíz de la app dentro de su sandbox.
 * Android: context.filesDir (almacenamiento interno privado).
 * iOS: carpeta Documents del contenedor de la app.
 */
expect fun appRootDirectory(): String

/**
 * Ruta del archivo de preferencias de DataStore.
 * Se guarda FUERA de la carpeta raíz para que el usuario no lo vea ni lo borre.
 * Android: noBackupFilesDir. iOS: carpeta Library.
 */
expect fun preferencesFilePath(): String