package com.mahbub.realtimevoicetranslate_kmp.presentation.screen.textToSpeach

import androidx.lifecycle.ViewModel
import com.mahbub.realtimevoicetranslate_kmp.core.platform.getTTSProvider
import com.mahbub.realtimevoicetranslate_kmp.domain.TTSState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class TextToSpeechViewModel : ViewModel() {
    // Track highlighted word range while speaking
    private val _currentWordRange = MutableStateFlow(-1..-1)
    val currentWordRange: StateFlow<IntRange> = _currentWordRange

    // Manage TTS state (IDLE, PLAYING, PAUSED)
    private val _ttsState = MutableStateFlow(TTSState.IDLE)
    val ttsState: StateFlow<TTSState> = _ttsState

    // Track initialization status
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized

    // Get platform-specific TTS provider (Android/iOS)
    private val ttsManager = getTTSProvider()

    // UI state
    data class State(
        val text: String = "",
        val highlightStart: Int = 0,
        val highlightEnd: Int = 0,
        val isPlaying: Boolean = false,
        val isPaused: Boolean = false
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    init {
        initializeTTS()
    }

    private fun initializeTTS() {
        ttsManager.initialize {
            _isInitialized.value = true
        }
    }

    fun speak(text: String) {
        if (text.isBlank()) return
        
        _state.update { it.copy(text = text) }
        _ttsState.value = TTSState.PLAYING
        
        ttsManager.speak(
            text = text,
            onWordBoundary = { start, end ->
                _state.update { 
                    it.copy(
                        highlightStart = start,
                        highlightEnd = end + 1
                    )
                }
            },
            onStart = {
                _ttsState.value = TTSState.PLAYING
                _state.update { it.copy(isPlaying = true, isPaused = false) }
            },
            onComplete = {
                _ttsState.value = TTSState.IDLE
                _state.update { 
                    it.copy(
                        isPlaying = false, 
                        isPaused = false,
                        highlightStart = 0,
                        highlightEnd = 0
                    )
                }
            }
        )
    }

    fun pause() {
        if (_ttsState.value == TTSState.PLAYING) {
            ttsManager.pause()
            _ttsState.value = TTSState.PAUSED
            _state.update { it.copy(isPlaying = false, isPaused = true) }
        }
    }

    fun resume() {
        if (_ttsState.value == TTSState.PAUSED) {
            ttsManager.resume()
            _ttsState.value = TTSState.PLAYING
            _state.update { it.copy(isPlaying = true, isPaused = false) }
        }
    }

    fun stop() {
        ttsManager.stop()
        _ttsState.value = TTSState.IDLE
        _state.update { 
            it.copy(
                isPlaying = false, 
                isPaused = false,
                highlightStart = 0,
                highlightEnd = 0
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.release()
    }
}