package com.mahbub.realtimevoicetranslate_kmp.presentation.screen.textToSpeach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahbub.realtimevoicetranslate_kmp.core.platform.getTTSProvider
import com.mahbub.realtimevoicetranslate_kmp.domain.TTSState
import com.mahbub.realtimevoicetranslate_kmp.domain.model.Result
import com.mahbub.realtimevoicetranslate_kmp.domain.usecase.InitializeTTSUseCase
import com.mahbub.realtimevoicetranslate_kmp.domain.usecase.PauseTTSUseCase
import com.mahbub.realtimevoicetranslate_kmp.domain.usecase.ResumeTTSUseCase
import com.mahbub.realtimevoicetranslate_kmp.domain.usecase.SpeakTextUseCase
import com.mahbub.realtimevoicetranslate_kmp.domain.usecase.StopTTSUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TextToSpeechViewModel(
    private val initializeTTSUseCase: InitializeTTSUseCase,
    private val speakTextUseCase: SpeakTextUseCase,
    private val pauseTTSUseCase: PauseTTSUseCase,
    private val resumeTTSUseCase: ResumeTTSUseCase,
    private val stopTTSUseCase: StopTTSUseCase
) : ViewModel() {
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
        viewModelScope.launch {
            when (val result = initializeTTSUseCase()) {
                is Result.Success -> {
                    _isInitialized.value = true
                    // Initialize the platform TTS manager
                    ttsManager.initialize {
                        // Platform-specific initialization callback
                    }
                }
                is Result.Error -> {
                    _isInitialized.value = false
                    // Handle initialization error
                }
                Result.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }

    fun speak(text: String) {
        if (text.isBlank()) return
        
        viewModelScope.launch {
            when (val result = speakTextUseCase(text)) {
                is Result.Success -> {
                    _state.update { it.copy(text = text) }
                    _ttsState.value = TTSState.PLAYING
                    
                    // Call platform-specific TTS manager
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
                is Result.Error -> {
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
                Result.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }

    fun pause() {
        if (_ttsState.value == TTSState.PLAYING) {
            viewModelScope.launch {
                when (val result = pauseTTSUseCase()) {
                    is Result.Success -> {
                        ttsManager.pause()
                        _ttsState.value = TTSState.PAUSED
                        _state.update { it.copy(isPlaying = false, isPaused = true) }
                    }
                    is Result.Error -> {
                        // Handle pause error
                    }
                    Result.Loading -> {
                        // Handle loading state if needed
                    }
                }
            }
        }
    }

    fun resume() {
        if (_ttsState.value == TTSState.PAUSED) {
            viewModelScope.launch {
                when (val result = resumeTTSUseCase()) {
                    is Result.Success -> {
                        ttsManager.resume()
                        _ttsState.value = TTSState.PLAYING
                        _state.update { it.copy(isPlaying = true, isPaused = false) }
                    }
                    is Result.Error -> {
                        // Handle resume error
                    }
                    Result.Loading -> {
                        // Handle loading state if needed
                    }
                }
            }
        }
    }

    fun stop() {
        viewModelScope.launch {
            when (val result = stopTTSUseCase()) {
                is Result.Success -> {
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
                is Result.Error -> {
                    // Handle stop error
                }
                Result.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.release()
    }
}