package com.mahbub.realtimevoicetranslate_kmp.domain.model

data class Transcript(
    val text: String = "",
    val isFinal: Boolean = false,
    val confidence: Float = 0f,
    val timestamp: Long = 0L // Will be set by platform-specific implementations
)