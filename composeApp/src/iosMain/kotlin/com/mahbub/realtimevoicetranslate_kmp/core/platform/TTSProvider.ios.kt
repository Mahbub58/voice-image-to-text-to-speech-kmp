package com.mahbub.realtimevoicetranslate_kmp.core.platform

import com.mahbub.realtimevoicetranslate_kmp.domain.repository.TTSProvider

private var ttsProvider: () -> TTSProvider? = { null }

fun setTTSProvider(provider: () -> TTSProvider) {
    ttsProvider = provider
}

actual fun getTTSProvider(): TTSProvider {
    return ttsProvider.invoke() ?: throw IllegalStateException("TTS provider not set")
}