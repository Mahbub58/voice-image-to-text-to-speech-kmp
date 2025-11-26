package com.mahbub.realtimevoicetranslate_kmp.core.platform

import com.mahbub.realtimevoicetranslate_kmp.data.TtsState
import com.mahbub.realtimevoicetranslate_kmp.tts.TTSProviderHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

actual class TextToSpeech {
    private val _state = MutableStateFlow(TtsState())
    actual val ttsState: MutableStateFlow<TtsState> get() = _state

    actual fun initialize(onInitialized: () -> Unit) {
        println("🎤 iOS TTS initialize() called")
        
        val provider = TTSProviderHolder.provider
        if (provider != null) {
            println("🎤 TTS Provider found, calling initialize...")
            provider.initialize {
                println("🎤 TTS Provider initialization complete")
                _state.update { it.copy(isInitialized = true) }
                onInitialized()
            }
        } else {
            println("🎤 ERROR: TTS Provider is null during initialization!")
            // Still call the callback to prevent hanging
            _state.update { it.copy(isInitialized = false) }
            onInitialized()
        }
    }

    actual fun speak(text: String) {
        println("🎤 iOS TTS speak() called with text: '${text.take(50)}...'")
        println("🎤 TTS Provider available: ${TTSProviderHolder.provider != null}")
        println("🎤 Current TTS state - isInitialized: ${_state.value.isInitialized}, isPlaying: ${_state.value.isPlaying}")
        
        // Ensure TTS is initialized before speaking
        if (!_state.value.isInitialized) {
            println("🎤 TTS not initialized, calling initialize...")
            initialize {
                println("🎤 TTS initialization complete, now speaking...")
                // After initialization, speak the text
                performSpeak(text)
            }
        } else {
            println("🎤 TTS already initialized, speaking directly...")
            performSpeak(text)
        }
    }
    
    private fun performSpeak(text: String) {
        println("🎤 performSpeak() called with text: '${text.take(50)}...'")
        _state.update { it.copy(text = text) }
        
        val provider = TTSProviderHolder.provider
        if (provider != null) {
            println("🎤 Calling TTSProvider.speak()...")
            provider.speak(
                text = text,
                onWordBoundary = { start, end ->
                    println("🎤 Word boundary callback: $start-$end")
                    _state.update { it.copy(highlightStart = start, highlightEnd = end) }
                },
                onStart = {
                    println("🎤 TTS started callback")
                    _state.update { it.copy(isPlaying = true, isPaused = false) }
                },
                onComplete = {
                    println("🎤 TTS completed callback")
                    _state.update { it.copy(isPlaying = false, isPaused = false, highlightStart = -1, highlightEnd = -1) }
                }
            )
        } else {
            println("🎤 ERROR: TTSProvider is null!")
        }
    }

    actual fun stop() {
        TTSProviderHolder.provider?.stop()
        _state.update { it.copy(isPlaying = false, isPaused = false, highlightStart = -1, highlightEnd = -1) }
    }

    actual fun pause() {
        TTSProviderHolder.provider?.pause()
        _state.update { it.copy(isPaused = true, isPlaying = false) }
    }

    actual fun resume() {
        TTSProviderHolder.provider?.resume()
        _state.update { it.copy(isPaused = false) }
    }

    actual fun release() {
        TTSProviderHolder.provider?.release()
        _state.update { TtsState() }
    }
    
    fun testTTS() {
        println("🎤 Running iOS TTS test...")
        println("🎤 Current state: isInitialized=${_state.value.isInitialized}")
        
        if (_state.value.isInitialized) {
            speak("This is a test of the text to speech system on iOS.")
        } else {
            println("🎤 TTS not initialized, initializing first...")
            initialize {
                println("🎤 Initialization complete, running test...")
                speak("This is a test of the text to speech system on iOS.")
            }
        }
    }
    
    fun verifyTTSChain() {
        println("🎤 Verifying complete TTS chain...")
        
        val provider = TTSProviderHolder.provider
        if (provider != null) {
            println("🎤 TTS Provider available, calling verifyTTSChain()...")
            // This would need to be added to the TTSProvider interface
            // For now, we'll run a comprehensive test
            testTTS()
        } else {
            println("🎤 ERROR: TTS Provider is null!")
        }
    }
}
