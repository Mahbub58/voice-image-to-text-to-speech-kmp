package com.mahbub.realtimevoicetranslate_kmp.domain.usecase

import com.mahbub.realtimevoicetranslate_kmp.domain.model.TextRecognitionState
import com.mahbub.realtimevoicetranslate_kmp.domain.repository.TextRecognitionRepository
import kotlinx.coroutines.flow.Flow

class ObserveTextRecognitionStateUseCase(
    private val repository: TextRecognitionRepository
) {
    operator fun invoke(): Flow<TextRecognitionState> = repository.getState()
}
