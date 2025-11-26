package com.mahbub.realtimevoicetranslate_kmp.data.repository

import com.mahbub.realtimevoicetranslate_kmp.core.platform.SpeechToText
import com.mahbub.realtimevoicetranslate_kmp.domain.model.Result
import com.mahbub.realtimevoicetranslate_kmp.domain.model.Transcript
import com.mahbub.realtimevoicetranslate_kmp.domain.repository.SpeechToTextRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SpeechToTextRepositoryImpl(
    private val speechToText: SpeechToText
) : SpeechToTextRepository {
    
    override fun getTranscript(): Flow<Transcript> {
        return speechToText.transcriptState.map { transcriptState ->
            Transcript(
                text = transcriptState.transcript ?: "",
                isFinal = transcriptState.listeningStatus.name == "FINAL",
                confidence = 0.9f, // Default confidence
                timestamp = 0L // Platform-specific timestamp will be set
            )
        }
    }
    
    override fun startListening(): Result<Unit> {
        return try {
            speechToText.startTranscribing()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override fun stopListening(): Result<Unit> {
        return try {
            speechToText.stopTranscribing()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override fun requestPermission(): Result<Boolean> {
        return try {
            var permissionResult = false
            speechToText.requestPermission { status ->
                permissionResult = when (status) {
                    com.mahbub.realtimevoicetranslate_kmp.data.PermissionRequestStatus.ALLOWED -> true
                    else -> false
                }
            }
            Result.Success(permissionResult)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override fun getSupportedLanguages(): Result<List<String>> {
        return try {
            // Return default languages for now
            Result.Success(listOf("en-US", "es-ES", "fr-FR", "de-DE", "it-IT"))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override fun setLanguage(languageCode: String): Result<Unit> {
        return try {
            speechToText.setLanguage(languageCode)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override fun copyText(text: String): Result<Unit> {
        return try {
            speechToText.copyText(text)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}