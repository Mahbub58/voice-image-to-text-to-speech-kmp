package com.mahbub.realtimevoicetranslate_kmp.domain.model

data class TextRecognitionState(
    val recognizedText: String = "",
    val isProcessing: Boolean = false,
    val error: String? = null,
    val showPermissionNeedDialog: Boolean = false
)
