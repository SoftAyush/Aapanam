package org.example.aapanam

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform