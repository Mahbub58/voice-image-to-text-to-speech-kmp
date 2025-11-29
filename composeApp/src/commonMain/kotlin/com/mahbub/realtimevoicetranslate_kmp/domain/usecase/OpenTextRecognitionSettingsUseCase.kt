package com.mahbub.realtimevoicetranslate_kmp.domain.usecase

import com.mahbub.realtimevoicetranslate_kmp.domain.repository.TextRecognitionRepository

class OpenTextRecognitionSettingsUseCase(
    private val repository: TextRecognitionRepository
) {
    operator fun invoke() {
        repository.openAppSettings()
    }
}
