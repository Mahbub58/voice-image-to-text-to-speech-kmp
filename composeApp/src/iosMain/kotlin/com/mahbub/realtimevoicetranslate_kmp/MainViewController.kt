package com.mahbub.realtimevoicetranslate_kmp

import androidx.compose.ui.window.ComposeUIViewController
import com.mahbub.realtimevoicetranslate_kmp.core.platform.setTTSProvider
import com.mahbub.realtimevoicetranslate_kmp.core.platform.setTextRecognitionProvider
import com.mahbub.realtimevoicetranslate_kmp.di.initKoin
import com.mahbub.realtimevoicetranslate_kmp.domain.repository.TTSProvider
import com.mahbub.realtimevoicetranslate_kmp.domain.repository.TextRecognitionProvider

fun MainViewController(ttsProvider: TTSProvider, textRecognitionProvider: TextRecognitionProvider) = ComposeUIViewController(
    configure = {
        initKoin()
        setTTSProvider { ttsProvider }
        setTextRecognitionProvider { textRecognitionProvider }
    }
) {
    App()
}
