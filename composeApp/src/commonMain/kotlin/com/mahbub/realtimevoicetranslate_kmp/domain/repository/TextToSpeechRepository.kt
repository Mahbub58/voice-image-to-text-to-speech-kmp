package com.mahbub.realtimevoicetranslate_kmp.domain.repository

import com.mahbub.realtimevoicetranslate_kmp.domain.model.Result
import com.mahbub.realtimevoicetranslate_kmp.domain.model.TTSText
import kotlinx.coroutines.flow.Flow

interface TextToSpeechRepository {
    fun getTTSState(): Flow<TTSText>
    fun speak(text: String): Result<Unit>
    fun pause(): Result<Unit>
    fun resume(): Result<Unit>
    fun stop(): Result<Unit>
    fun initialize(): Result<Unit>
    fun release(): Result<Unit>
}