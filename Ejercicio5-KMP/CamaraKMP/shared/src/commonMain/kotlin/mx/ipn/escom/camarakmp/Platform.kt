package mx.ipn.escom.camarakmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform