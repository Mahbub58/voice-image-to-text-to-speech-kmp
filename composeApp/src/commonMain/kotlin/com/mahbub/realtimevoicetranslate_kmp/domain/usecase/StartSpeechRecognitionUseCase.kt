package com.mahbub.realtimevoicetranslate_kmp.domain.usecase

import com.mahbub.realtimevoicetranslate_kmp.domain.model.Result
import com.mahbub.realtimevoicetranslate_kmp.domain.repository.SpeechToTextRepository

class StartSpeechRecognitionUseCase(
    private val speechToTextRepository: SpeechToTextRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return speechToTextRepository.startListening()
    }
}