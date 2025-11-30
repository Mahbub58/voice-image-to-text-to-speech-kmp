package com.mahbub.realtimevoicetranslate_kmp.domain.usecase

import com.mahbub.realtimevoicetranslate_kmp.domain.model.Result
import com.mahbub.realtimevoicetranslate_kmp.domain.repository.TextToSpeechRepository

class StopTTSUseCase(
    private val textToSpeechRepository: TextToSpeechRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return textToSpeechRepository.stop()
    }
}