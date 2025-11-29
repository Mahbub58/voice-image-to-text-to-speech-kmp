package com.mahbub.realtimevoicetranslate_kmp.core.platform

import com.mahbub.realtimevoicetranslate_kmp.domain.repository.TextRecognitionProvider

private var providerGetter: () -> TextRecognitionProvider? = { null }

fun setTextRecognitionProvider(provider: () -> TextRecognitionProvider) {
    providerGetter = provider
}

actual fun getTextRecognitionProvider(): TextRecognitionProvider {
    return providerGetter.invoke() ?: throw IllegalStateException("TextRecognition provider not set")
}
