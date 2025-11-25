package com.mahbub.realtimevoicetranslate_kmp.presentation.screen

import androidx.lifecycle.ViewModel
import com.mahbub.realtimevoicetranslate_kmp.core.platform.TextToSpeech
import com.mahbub.realtimevoicetranslate_kmp.data.TtsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TextToSpeechViewModel(
    private val tts: TextToSpeech
) : ViewModel() {

    val state: MutableStateFlow<TtsState> = tts.ttsState
    private val _uiEvent = MutableStateFlow<UiEvent?>(null)
    val uiEvent: StateFlow<UiEvent?> = _uiEvent

    fun speak(text: String) {
        if (text.isBlank()) {
            _uiEvent.value = UiEvent.ShowSnackbar("Enter text to speak")
        } else {
            tts.speak(text)
        }
    }

    fun pause() { tts.pause() }
    fun resume() { tts.resume() }
    fun stop() { tts.stop() }
    fun onUiEventHandled() { _uiEvent.value = null }

    sealed class UiEvent { data class ShowSnackbar(val message: String) : UiEvent() }
}

