package com.mahbub.realtimevoicetranslate_kmp.domain.usecase

import com.mahbub.realtimevoicetranslate_kmp.domain.model.Result
import com.mahbub.realtimevoicetranslate_kmp.domain.repository.SpeechToTextRepository

class CopyTranscriptUseCase(
    private val speechToTextRepository: SpeechToTextRepository
) {
    suspend operator fun invoke(text: String): Result<Unit> {
        return speechToTextRepository.copyText(text)
    }
}