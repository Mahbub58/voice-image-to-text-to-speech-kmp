package com.mahbub.realtimevoicetranslate_kmp.domain.usecase

import com.mahbub.realtimevoicetranslate_kmp.domain.model.Result
import com.mahbub.realtimevoicetranslate_kmp.domain.repository.TextToSpeechRepository

class ReleaseTTSUseCase(
    private val repository: TextToSpeechRepository
) {
    operator fun invoke(): Result<Unit> = repository.release()
}
