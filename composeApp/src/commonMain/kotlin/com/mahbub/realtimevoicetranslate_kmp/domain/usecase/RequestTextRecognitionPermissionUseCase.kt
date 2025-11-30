package com.mahbub.realtimevoicetranslate_kmp.domain.usecase

import com.mahbub.realtimevoicetranslate_kmp.domain.model.Result
import com.mahbub.realtimevoicetranslate_kmp.domain.repository.TextRecognitionRepository

class RequestTextRecognitionPermissionUseCase(
    private val repository: TextRecognitionRepository
) {
    operator fun invoke(): Result<Boolean> {
        return repository.requestPermission()
    }
}
