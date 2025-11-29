package com.mahbub.realtimevoicetranslate_kmp.domain.usecase

import com.mahbub.realtimevoicetranslate_kmp.domain.model.TTSText
import com.mahbub.realtimevoicetranslate_kmp.domain.repository.TextToSpeechRepository
import kotlinx.coroutines.flow.Flow

class ObserveTTSStateUseCase(
    private val repository: TextToSpeechRepository
) {
    operator fun invoke(): Flow<TTSText> = repository.getTTSState()
}
