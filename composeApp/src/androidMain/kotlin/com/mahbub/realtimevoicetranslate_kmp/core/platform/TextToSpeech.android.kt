package com.mahbub.realtimevoicetranslate_kmp.core.platform

import android.app.Activity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.mahbub.realtimevoicetranslate_kmp.data.Error
import com.mahbub.realtimevoicetranslate_kmp.data.TtsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale

actual class TextToSpeech(
    private val activity: Activity
) {
    private val _state = MutableStateFlow(TtsState())
    actual val ttsState: MutableStateFlow<TtsState> get() = _state

    private var tts: TextToSpeech? = null
    private var originalText: String = ""
    private var baseOffset: Int = 0
    private var pausedPosition: Int = 0

    actual fun initialize(onInitialized: () -> Unit) {
        if (tts != null) return
        tts = TextToSpeech(activity) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _state.update { it.copy(isPlaying = true, isPaused = false) }
                    }
                    override fun onDone(utteranceId: String?) {
                        _state.update { it.copy(isPlaying = false, isPaused = false, highlightStart = -1, highlightEnd = -1) }
                    }
                    override fun onError(utteranceId: String?) {
                        _state.update { it.copy(isPlaying = false, error = Error(true, "TTS error")) }
                    }
                    override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                        val globalStart = start + baseOffset
                        val globalEnd = end + baseOffset
                        pausedPosition = globalEnd
                        _state.update { it.copy(highlightStart = globalStart, highlightEnd = globalEnd) }
                    }
                })
                _state.update { it.copy(isInitialized = true) }
                onInitialized()
            } else {
                _state.update { it.copy(error = Error(true, "TTS init failed")) }
            }
        }
    }

    private fun speakInternal(text: String) {
        val params = Bundle().apply { putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "utt") }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "utt")
    }

    actual fun speak(text: String) {
        initialize {}
        originalText = text
        baseOffset = 0
        pausedPosition = 0
        _state.update { it.copy(text = text) }
        speakInternal(text)
    }

    actual fun stop() {
        tts?.stop()
        _state.update { it.copy(isPlaying = false, isPaused = false) }
    }

    actual fun pause() {
        // Emulate pause by stopping and keeping last highlight position
        tts?.stop()
        _state.update { it.copy(isPaused = true, isPlaying = false) }
    }

    actual fun resume() {
        if (_state.value.isPaused && originalText.isNotBlank()) {
            if (pausedPosition >= originalText.length) {
                _state.update { it.copy(isPaused = false, isPlaying = false) }
                return
            }
            baseOffset = pausedPosition
            val remaining = originalText.substring(pausedPosition)
            speakInternal(remaining)
            _state.update { it.copy(isPaused = false) }
        }
    }

    actual fun release() {
        tts?.shutdown()
        tts = null
        _state.update { TtsState() }
    }
}
