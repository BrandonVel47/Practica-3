package mx.ipn.escom.gestorkmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform