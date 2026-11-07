package com.dwlee.dalfam

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform