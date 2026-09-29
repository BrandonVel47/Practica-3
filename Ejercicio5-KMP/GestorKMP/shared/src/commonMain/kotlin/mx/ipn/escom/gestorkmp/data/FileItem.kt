package mx.ipn.escom.gestorkmp.data

/** Tipo de archivo según su extensión, con un ícono para la lista. */
enum class FileType(val icono: String) {
    FOLDER("📁"),
    IMAGE("🖼️"),
    TEXT("📄"),
    PDF("📕"),
    AUDIO("🎵"),
    VIDEO("🎬"),
    OTHER("📦");

    companion object {
        fun fromName(name: String, isDirectory: Boolean): FileType {
            if (isDirectory) return FOLDER
            return when (name.substringAfterLast('.', "").lowercase()) {
                "jpg", "jpeg", "png", "gif", "webp", "heic", "bmp" -> IMAGE
                "txt", "md", "json", "xml", "kt", "swift", "csv", "log", "html" -> TEXT
                "pdf" -> PDF
                "mp3", "m4a", "wav", "aac", "ogg" -> AUDIO
                "mp4", "mov", "mkv", "webm" -> VIDEO
                else -> OTHER
            }
        }
    }
}

/** Representa un archivo o carpeta dentro del sandbox de la app. */
data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long
) {
    val type: FileType get() = FileType.fromName(name, isDirectory)
}