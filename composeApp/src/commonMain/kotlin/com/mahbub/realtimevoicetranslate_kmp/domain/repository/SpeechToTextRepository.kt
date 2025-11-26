package com.mahbub.realtimevoicetranslate_kmp.domain.repository

import com.mahbub.realtimevoicetranslate_kmp.domain.model.Result
import com.mahbub.realtimevoicetranslate_kmp.domain.model.Transcript
import kotlinx.coroutines.flow.Flow

interface SpeechToTextRepository {
    fun getTranscript(): Flow<Transcript>
    fun startListening(): Result<Unit>
    fun stopListening(): Result<Unit>
    fun requestPermission(): Result<Boolean>
    fun getSupportedLanguages(): Result<List<String>>
    fun setLanguage(languageCode: String): Result<Unit>
    fun copyText(text: String): Result<Unit>
}