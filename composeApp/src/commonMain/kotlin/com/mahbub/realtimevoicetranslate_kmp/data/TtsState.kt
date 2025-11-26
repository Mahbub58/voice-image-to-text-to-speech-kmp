package com.mahbub.realtimevoicetranslate_kmp.data

data class TtsState(
    val isInitialized: Boolean = false,
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val text: String = "",
    val highlightStart: Int = -1,
    val highlightEnd: Int = -1,
    val error: Error = Error(isError = false, message = null)
)

