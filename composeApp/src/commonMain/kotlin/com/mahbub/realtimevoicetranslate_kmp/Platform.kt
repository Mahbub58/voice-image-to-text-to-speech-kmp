package com.mahbub.realtimevoicetranslate_kmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform