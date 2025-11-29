package com.mahbub.realtimevoicetranslate_kmp.domain.usecase

import com.mahbub.realtimevoicetranslate_kmp.domain.model.Result
import com.mahbub.realtimevoicetranslate_kmp.domain.repository.TextRecognitionRepository

class StartTextRecognitionUseCase(
    private val repository: TextRecognitionRepository
) {
    operator fun invoke(): Result<Unit> {
        return repository.pickImageAndRecognize()
    }
}
