package mx.ipn.escom.gestorkmp.data

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Caché en memoria de miniaturas (LRU: se descartan las menos usadas).
 * Evita volver a leer y decodificar cada imagen al desplazarse por la lista.
 * La clave incluye la fecha de modificación: si el archivo cambia, se regenera.
 */
object ThumbnailCache {
    private const val MAXIMO = 100
    private val mapa = LinkedHashMap<String, ImageBitmap>()

    private fun clave(item: FileItem) = "${item.path}#${item.lastModified}"

    fun get(item: FileItem): ImageBitmap? {
        val k = clave(item)
        val imagen = mapa.remove(k) ?: return null
        mapa[k] = imagen // la movemos al final: recién usada
        return imagen
    }

    fun put(item: FileItem, imagen: ImageBitmap) {
        val k = clave(item)
        mapa.remove(k)
        mapa[k] = imagen
        while (mapa.size > MAXIMO) {
            mapa.remove(mapa.keys.first()) // la más antigua
        }
    }
}