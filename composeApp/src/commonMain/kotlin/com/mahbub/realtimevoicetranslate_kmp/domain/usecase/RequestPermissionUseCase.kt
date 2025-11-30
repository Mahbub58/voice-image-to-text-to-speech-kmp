package com.mahbub.realtimevoicetranslate_kmp.domain.usecase

import com.mahbub.realtimevoicetranslate_kmp.domain.model.Result
import com.mahbub.realtimevoicetranslate_kmp.domain.repository.SpeechToTextRepository

class RequestPermissionUseCase(
    private val speechToTextRepository: SpeechToTextRepository
) {
    suspend operator fun invoke(): Result<Boolean> {
        return speechToTextRepository.requestPermission()
    }
}