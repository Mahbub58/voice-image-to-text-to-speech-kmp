package com.mahbub.realtimevoicetranslate_kmp.data.repository

import com.mahbub.realtimevoicetranslate_kmp.core.platform.getTTSProvider
import com.mahbub.realtimevoicetranslate_kmp.domain.model.Result
import com.mahbub.realtimevoicetranslate_kmp.domain.model.TTSText
import com.mahbub.realtimevoicetranslate_kmp.domain.repository.TextToSpeechRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TextToSpeechRepositoryImpl : TextToSpeechRepository {
    
    private val ttsManager = getTTSProvider()
    private val _ttsState = MutableStateFlow(
        TTSText(
            text = "",
            highlightStart = 0,
            highlightEnd = 0,
            isPlaying = false,
            isPaused = false
        )
    )
    
    override fun getTTSState(): Flow<TTSText> = _ttsState.asStateFlow()
    
    override fun speak(text: String): Result<Unit> {
        return try {
            ttsManager.speak(
                text = text,
                onWordBoundary = { start, end ->
                    _ttsState.value = _ttsState.value.copy(
                        text = text,
                        highlightStart = start,
                        highlightEnd = end + 1,
                        isPlaying = true,
                        isPaused = false
                    )
                },
                onStart = {
                    _ttsState.value = _ttsState.value.copy(
                        text = text,
                        isPlaying = true,
                        isPaused = false
                    )
                },
                onComplete = {
                    _ttsState.value = _ttsState.value.copy(
                        highlightStart = 0,
                        highlightEnd = 0,
                        isPlaying = false,
                        isPaused = false
                    )
                }
            )
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override fun pause(): Result<Unit> {
        return try {
            ttsManager.pause()
            _ttsState.value = _ttsState.value.copy(
                isPlaying = false,
                isPaused = true
            )
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override fun resume(): Result<Unit> {
        return try {
            ttsManager.resume()
            _ttsState.value = _ttsState.value.copy(
                isPlaying = true,
                isPaused = false
            )
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override fun stop(): Result<Unit> {
        return try {
            ttsManager.stop()
            _ttsState.value = _ttsState.value.copy(
                highlightStart = 0,
                highlightEnd = 0,
                isPlaying = false,
                isPaused = false
            )
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override fun initialize(): Result<Unit> {
        return try {
            ttsManager.initialize { }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override fun release(): Result<Unit> {
        return try {
            ttsManager.release()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}