package com.mahbub.realtimevoicetranslate_kmp.presentation.screen.textToSpeach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahbub.realtimevoicetranslate_kmp.domain.TTSState
import com.mahbub.realtimevoicetranslate_kmp.domain.model.Result
import com.mahbub.realtimevoicetranslate_kmp.domain.usecase.InitializeTTSUseCase
import com.mahbub.realtimevoicetranslate_kmp.domain.usecase.PauseTTSUseCase
import com.mahbub.realtimevoicetranslate_kmp.domain.usecase.ResumeTTSUseCase
import com.mahbub.realtimevoicetranslate_kmp.domain.usecase.SpeakTextUseCase
import com.mahbub.realtimevoicetranslate_kmp.domain.usecase.StopTTSUseCase
import com.mahbub.realtimevoicetranslate_kmp.domain.usecase.ObserveTTSStateUseCase
import com.mahbub.realtimevoicetranslate_kmp.domain.usecase.ReleaseTTSUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.onEach

class TextToSpeechViewModel(
    private val initializeTTSUseCase: InitializeTTSUseCase,
    private val speakTextUseCase: SpeakTextUseCase,
    private val pauseTTSUseCase: PauseTTSUseCase,
    private val resumeTTSUseCase: ResumeTTSUseCase,
    private val stopTTSUseCase: StopTTSUseCase,
    private val observeTTSStateUseCase: ObserveTTSStateUseCase,
    private val releaseTTSUseCase: ReleaseTTSUseCase
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
        observeTTSState()
    }

    private fun observeTTSState() {
        viewModelScope.launch {
            observeTTSStateUseCase().onEach { ttsText ->
                _state.update {
                    it.copy(
                        text = ttsText.text,
                        highlightStart = ttsText.highlightStart,
                        highlightEnd = ttsText.highlightEnd,
                        isPlaying = ttsText.isPlaying,
                        isPaused = ttsText.isPaused
                    )
                }
                _ttsState.value = when {
                    ttsText.isPlaying -> TTSState.PLAYING
                    ttsText.isPaused -> TTSState.PAUSED
                    else -> TTSState.IDLE
                }
            }.collect {}
        }
    }

    private fun initializeTTS() {
        viewModelScope.launch {
            when (val result = initializeTTSUseCase()) {
                is Result.Success -> {
                    _isInitialized.value = true
                }
                is Result.Error -> {
                    _isInitialized.value = false
                }
                Result.Loading -> {
                }
            }
        }
    }

    fun speak(text: String) {
        if (text.isBlank()) return
        
        // Allow replaying after completion, but prevent multiple calls while playing
        if (_ttsState.value == TTSState.PLAYING) {
            println("TextToSpeechViewModel: Already playing, ignoring duplicate call")
            return
        }

        println("TextToSpeechViewModel: speak($text)")
        
        viewModelScope.launch {
            when (val result = speakTextUseCase(text)) {
                is Result.Success -> {
                    // State will be updated through repository observation
                    println("TextToSpeechViewModel: TTS started successfully")
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
                    println("TextToSpeechViewModel: TTS error - ${result.exception}")
                }
                Result.Loading -> {
                    // Loading state - repository will update when ready
                    println("TextToSpeechViewModel: TTS loading...")
                }
            }
        }
    }

    fun pause() {
        if (_ttsState.value == TTSState.PLAYING) {
            viewModelScope.launch {
                when (val result = pauseTTSUseCase()) {
                    is Result.Success -> {
                        _ttsState.value = TTSState.PAUSED
                        _state.update { it.copy(isPlaying = false, isPaused = true) }
                    }
                    is Result.Error -> {
                    }
                    Result.Loading -> {
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
                        _ttsState.value = TTSState.PLAYING
                        _state.update { it.copy(isPlaying = true, isPaused = false) }
                    }
                    is Result.Error -> {
                    }
                    Result.Loading -> {
                    }
                }
            }
        }
    }

    fun stop() {
        viewModelScope.launch {
            when (val result = stopTTSUseCase()) {
                is Result.Success -> {
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
                }
                Result.Loading -> {
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        releaseTTSUseCase()
    }
}
