package com.mahbub.realtimevoicetranslate_kmp.core.platform

import com.mahbub.realtimevoicetranslate_kmp.data.PermissionRequestStatus
import com.mahbub.realtimevoicetranslate_kmp.data.TranscriptState
import kotlinx.coroutines.flow.MutableStateFlow

expect class SpeechToText {
    val transcriptState: MutableStateFlow<TranscriptState>
    fun startTranscribing()
    fun stopTranscribing()
    fun requestPermission(onPermissionResult: (PermissionRequestStatus) -> Unit)
    fun getSupportedLanguages(onLanguagesResult: (List<String>) -> Unit)
    fun setLanguage(languageCode: String)
    fun copyText(text: String)
    fun showNeedPermission()
    fun dismissPermissionDialog()
    fun openAppSettings()
}