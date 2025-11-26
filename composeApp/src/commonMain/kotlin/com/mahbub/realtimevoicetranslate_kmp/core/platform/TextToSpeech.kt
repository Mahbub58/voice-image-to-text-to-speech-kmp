package com.mahbub.realtimevoicetranslate_kmp.core.platform

import com.mahbub.realtimevoicetranslate_kmp.data.TtsState
import kotlinx.coroutines.flow.MutableStateFlow

expect class TextToSpeech {
    val ttsState: MutableStateFlow<TtsState>
    fun initialize(onInitialized: () -> Unit = {})
    fun speak(text: String)
    fun stop()
    fun pause()
    fun resume()
    fun release()
}

