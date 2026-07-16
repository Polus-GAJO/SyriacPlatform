package org.syriacplatform

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform