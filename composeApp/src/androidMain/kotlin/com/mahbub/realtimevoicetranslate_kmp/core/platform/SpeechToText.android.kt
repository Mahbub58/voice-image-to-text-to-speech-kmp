package com.mahbub.realtimevoicetranslate_kmp.core.platform

import com.mahbub.realtimevoicetranslate_kmp.data.ListeningStatus
import com.mahbub.realtimevoicetranslate_kmp.data.PermissionRequestStatus
import com.mahbub.realtimevoicetranslate_kmp.data.TranscriptState
import kotlinx.coroutines.flow.MutableStateFlow

actual class SpeechToText {
    private val _transcriptState = MutableStateFlow(
        TranscriptState(
            listeningStatus = ListeningStatus.INACTIVE
        )
    )
    actual val transcriptState: MutableStateFlow<TranscriptState>
        get() = _transcriptState

    actual fun startTranscribing() {
        _transcriptState.value = _transcriptState.value.copy(
            listeningStatus = ListeningStatus.LISTENING,
            error = _transcriptState.value.error
        )
    }

    actual fun stopTranscribing() {
        _transcriptState.value = _transcriptState.value.copy(
            listeningStatus = ListeningStatus.INACTIVE
        )
    }

    actual fun requestPermission(onPermissionResult: (PermissionRequestStatus) -> Unit) {
        onPermissionResult(PermissionRequestStatus.ALLOWED)
    }

    actual fun getSupportedLanguages(onLanguagesResult: (List<String>) -> Unit) {
        onLanguagesResult(listOf("en-US", "bn-BD"))
    }

    actual fun setLanguage(languageCode: String) {
        _transcriptState.value = _transcriptState.value.copy(
            selectedLanguage = languageCode
        )
    }

    actual fun copyText(text: String) {
    }

    actual fun showNeedPermission() {
        _transcriptState.value = _transcriptState.value.copy(
            showPermissionNeedDialog = true
        )
    }

    actual fun dismissPermissionDialog() {
        _transcriptState.value = _transcriptState.value.copy(
            showPermissionNeedDialog = false
        )
    }

    actual fun openAppSettings() {
    }
}