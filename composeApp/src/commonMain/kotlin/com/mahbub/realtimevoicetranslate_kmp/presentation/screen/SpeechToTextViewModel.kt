package com.mahbub.realtimevoicetranslate_kmp.presentation.screen

import androidx.lifecycle.ViewModel
import com.mahbub.realtimevoicetranslate_kmp.core.platform.SpeechToText
import com.mahbub.realtimevoicetranslate_kmp.data.ListeningStatus
import com.mahbub.realtimevoicetranslate_kmp.data.PermissionRequestStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class SpeechToTextViewModel(
    private val speechToText: SpeechToText
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
        speechToText.requestPermission { status ->
            when (status) {
                PermissionRequestStatus.ALLOWED -> {
                    startListening()
                }

                PermissionRequestStatus.NOT_ALLOWED -> {
                    _uiEvent.value = UiEvent.ShowSnackbar("Permission denied")
                }

                PermissionRequestStatus.NEVER_ASK_AGAIN -> {
                    speechToText.showNeedPermission()
                }
            }
        }
    }

    private fun startListening() {
        speechToText.startTranscribing()
    }

    private fun stopListening() {
        speechToText.stopTranscribing()
    }

    fun onClickCopy() {
        transcriptState.value.transcript?.let { text ->
            speechToText.copyText(text)
            _uiEvent.value = UiEvent.ShowSnackbar("Text copied to clipboard")
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