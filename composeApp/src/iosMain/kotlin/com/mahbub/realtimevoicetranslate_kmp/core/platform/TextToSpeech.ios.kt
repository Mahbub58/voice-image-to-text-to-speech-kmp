package com.mahbub.realtimevoicetranslate_kmp.core.platform

import com.mahbub.realtimevoicetranslate_kmp.data.TtsState
import com.mahbub.realtimevoicetranslate_kmp.tts.TTSProviderHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

actual class TextToSpeech {
    private val _state = MutableStateFlow(TtsState())
    actual val ttsState: MutableStateFlow<TtsState> get() = _state

    actual fun initialize(onInitialized: () -> Unit) {
        TTSProviderHolder.provider?.initialize {
            _state.update { it.copy(isInitialized = true) }
            onInitialized()
        }
    }

    actual fun speak(text: String) {
        _state.update { it.copy(text = text) }
        TTSProviderHolder.provider?.speak(
            text = text,
            onWordBoundary = { start, end ->
                _state.update { it.copy(highlightStart = start, highlightEnd = end) }
            },
            onStart = {
                _state.update { it.copy(isPlaying = true, isPaused = false) }
            },
            onComplete = {
                _state.update { it.copy(isPlaying = false, isPaused = false, highlightStart = -1, highlightEnd = -1) }
            }
        )
    }

    actual fun stop() {
        TTSProviderHolder.provider?.stop()
        _state.update { it.copy(isPlaying = false, isPaused = false) }
    }

    actual fun pause() {
        TTSProviderHolder.provider?.pause()
        _state.update { it.copy(isPaused = true, isPlaying = false) }
    }

    actual fun resume() {
        TTSProviderHolder.provider?.resume()
        _state.update { it.copy(isPaused = false) }
    }

    actual fun release() {
        TTSProviderHolder.provider?.release()
        _state.update { TtsState() }
    }
}
