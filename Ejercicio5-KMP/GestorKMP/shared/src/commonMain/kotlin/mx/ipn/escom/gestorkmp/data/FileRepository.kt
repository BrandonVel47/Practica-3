package mx.ipn.escom.gestorkmp.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import mx.ipn.escom.gestorkmp.platform.appRootDirectory
import mx.ipn.escom.gestorkmp.platform.platformFileSystem
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

/**
 * Acceso al sistema de archivos. Todo el código es común:
 * solo la raíz y el FileSystem vienen de expect/actual.
 */
class FileRepository(
    private val fs: FileSystem = platformFileSystem
) {
    val rootPath: Path = appRootDirectory().toPath()

    fun exists(path: Path): Boolean = fs.exists(path)

    /** Lista el contenido de una carpeta: primero carpetas, luego archivos, por nombre. */
    suspend fun list(dir: Path): List<FileItem> = withContext(Dispatchers.IO) {
        fs.list(dir).mapNotNull { crearItem(it) }.sortedWith(
            compareByDescending<FileItem> { it.isDirectory }.thenBy { it.name.lowercase() }
        )
    }

    /** Información de un solo archivo o carpeta; null si ya no existe. */
    suspend fun info(path: Path): FileItem? = withContext(Dispatchers.IO) { crearItem(path) }

    /** Lee un archivo de texto (máximo 1 MB para no saturar la memoria). */
    suspend fun readText(path: Path, maxBytes: Long = 1_000_000): String =
        withContext(Dispatchers.IO) {
            val size = fs.metadataOrNull(path)?.size ?: 0L
            if (size > maxBytes) {
                error("El archivo es demasiado grande para mostrarse (${size / 1024} KB)")
            }
            fs.read(path) { readUtf8() }
        }

    /** Lee un archivo completo como bytes (para imágenes). */
    suspend fun readBytes(path: Path): ByteArray = withContext(Dispatchers.IO) {
        fs.read(path) { readByteArray() }
    }

    suspend fun createFolder(parent: Path, name: String) = withContext(Dispatchers.IO) {
        fs.createDirectory(parent / name, mustCreate = true)
    }

    suspend fun delete(path: Path) = withContext(Dispatchers.IO) {
        fs.deleteRecursively(path)
    }

    suspend fun rename(path: Path, newName: String) = withContext(Dispatchers.IO) {
        val parent = path.parent ?: error("No se puede renombrar la raíz")
        val destino = parent / newName
        if (fs.exists(destino)) error("Ya existe un elemento llamado \"$newName\"")
        fs.atomicMove(path, destino)
    }

    /** Copia un archivo o carpeta (con todo su contenido) dentro de otra carpeta. */
    suspend fun copy(origen: Path, carpetaDestino: Path): Path = withContext(Dispatchers.IO) {
        validarDestino(origen, carpetaDestino)
        val destino = rutaDisponible(carpetaDestino, origen.name)
        copiarRecursivo(origen, destino)
        destino
    }

    /** Mueve un archivo o carpeta a otra carpeta. */
    suspend fun move(origen: Path, carpetaDestino: Path): Path = withContext(Dispatchers.IO) {
        validarDestino(origen, carpetaDestino)
        if (origen.parent == carpetaDestino) return@withContext origen // ya está ahí
        val destino = rutaDisponible(carpetaDestino, origen.name)
        fs.atomicMove(origen, destino)
        destino
    }

    /**
     * Devuelve una ruta libre dentro de [dir]. Si ya existe [nombre],
     * agrega " (1)", " (2)", etc. antes de la extensión.
     */
    fun rutaDisponible(dir: Path, nombre: String): Path {
        val limpio = nombre.replace('/', '_').replace('\\', '_').ifBlank { "archivo" }
        val base = limpio.substringBeforeLast('.', limpio)
        val ext = limpio.substringAfterLast('.', "").let { if (it.isEmpty()) "" else ".$it" }
        var destino = dir / limpio
        var i = 1
        while (fs.exists(destino)) {
            destino = dir / "$base ($i)$ext"
            i++
        }
        return destino
    }

    /** Crea contenido de ejemplo la primera vez, para no ver la app vacía. */
    suspend fun seedIfEmpty() = withContext(Dispatchers.IO) {
        if (fs.list(rootPath).isEmpty()) {
            fs.createDirectory(rootPath / "Documentos")
            fs.createDirectory(rootPath / "Imagenes")
            fs.write(rootPath / "Bienvenida.txt") {
                writeUtf8("Bienvenido al Gestor de Archivos KMP.\n")
            }
            fs.write(rootPath / "Documentos" / "notas.md") {
                writeUtf8("# Notas\n\nArchivo de ejemplo.\n")
            }
        }
    }

    // ---------- Internos ----------

    private fun crearItem(p: Path): FileItem? {
        val meta = fs.metadataOrNull(p) ?: return null
        return FileItem(
            name = p.name,
            path = p.toString(),
            isDirectory = meta.isDirectory,
            size = meta.size ?: 0L,
            lastModified = meta.lastModifiedAtMillis ?: 0L
        )
    }

    private fun validarDestino(origen: Path, carpetaDestino: Path) {
        if (!fs.exists(origen)) error("El elemento ya no existe")
        val o = origen.toString()
        val d = carpetaDestino.toString()
        if (d == o || d.startsWith("$o/")) {
            error("No puedes copiar o mover una carpeta dentro de sí misma")
        }
    }

    private fun copiarRecursivo(origen: Path, destino: Path) {
        if (fs.metadata(origen).isDirectory) {
            fs.createDirectory(destino)
            fs.list(origen).forEach { hijo -> copiarRecursivo(hijo, destino / hijo.name) }
        } else {
            fs.copy(origen, destino)
        }
    }
}