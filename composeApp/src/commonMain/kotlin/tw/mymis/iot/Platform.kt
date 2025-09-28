package tw.mymis.iot

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform