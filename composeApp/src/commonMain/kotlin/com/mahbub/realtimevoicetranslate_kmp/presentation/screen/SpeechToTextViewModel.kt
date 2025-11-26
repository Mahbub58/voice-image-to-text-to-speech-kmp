package com.mahbub.realtimevoicetranslate_kmp.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahbub.realtimevoicetranslate_kmp.core.platform.SpeechToText
import com.mahbub.realtimevoicetranslate_kmp.data.ListeningStatus
import com.mahbub.realtimevoicetranslate_kmp.data.PermissionRequestStatus
import com.mahbub.realtimevoicetranslate_kmp.domain.model.Result
import com.mahbub.realtimevoicetranslate_kmp.domain.usecase.CopyTranscriptUseCase
import com.mahbub.realtimevoicetranslate_kmp.domain.usecase.RequestPermissionUseCase
import com.mahbub.realtimevoicetranslate_kmp.domain.usecase.StartSpeechRecognitionUseCase
import com.mahbub.realtimevoicetranslate_kmp.domain.usecase.StopSpeechRecognitionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class SpeechToTextViewModel(
    private val speechToText: SpeechToText,
    private val startSpeechRecognitionUseCase: StartSpeechRecognitionUseCase,
    private val stopSpeechRecognitionUseCase: StopSpeechRecognitionUseCase,
    private val requestPermissionUseCase: RequestPermissionUseCase,
    private val copyTranscriptUseCase: CopyTranscriptUseCase
) : ViewModel() {

    val transcriptState = speechToText.transcriptState

    private val _uiEvent = MutableStateFlow<UiEvent?>(null)
    val uiEvent: StateFlow<UiEvent?> = _uiEvent

    fun onClickMic() {
        if (transcriptState.value.listeningStatus == ListeningStatus.INACTIVE) {
            handlePermissionRequest()
        } else {
            stopListening()
        }
    }

    private fun handlePermissionRequest() {
        viewModelScope.launch {
            when (val result = requestPermissionUseCase()) {
                is Result.Success -> {
                    if (result.data) {
                        startListening()
                    } else {
                        _uiEvent.value = UiEvent.ShowSnackbar("Permission denied")
                    }
                }
                is Result.Error -> {
                    _uiEvent.value = UiEvent.ShowSnackbar("Permission request failed")
                }
                Result.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }

    private fun startListening() {
        viewModelScope.launch {
            when (val result = startSpeechRecognitionUseCase()) {
                is Result.Success -> {
                    speechToText.startTranscribing()
                }
                is Result.Error -> {
                    _uiEvent.value = UiEvent.ShowSnackbar("Failed to start speech recognition")
                }
                Result.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }

    private fun stopListening() {
        viewModelScope.launch {
            when (val result = stopSpeechRecognitionUseCase()) {
                is Result.Success -> {
                    speechToText.stopTranscribing()
                }
                is Result.Error -> {
                    _uiEvent.value = UiEvent.ShowSnackbar("Failed to stop speech recognition")
                }
                Result.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }

    fun onClickCopy() {
        transcriptState.value.transcript?.let { text ->
            viewModelScope.launch {
                when (val result = copyTranscriptUseCase(text)) {
                    is Result.Success -> {
                        _uiEvent.value = UiEvent.ShowSnackbar("Text copied to clipboard")
                    }
                    is Result.Error -> {
                        _uiEvent.value = UiEvent.ShowSnackbar("Failed to copy text")
                    }
                    Result.Loading -> {
                        // Handle loading state if needed
                    }
                }
            }
        }
    }

    fun onLanguageSelected(language: String) {
        speechToText.setLanguage(language)
    }

    fun onUiEventHandled() {
        _uiEvent.value = null
    }

    fun onDismissRequest() {
        speechToText.dismissPermissionDialog()
    }

    fun openAppSettings() {
        speechToText.openAppSettings()
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
    }
} 