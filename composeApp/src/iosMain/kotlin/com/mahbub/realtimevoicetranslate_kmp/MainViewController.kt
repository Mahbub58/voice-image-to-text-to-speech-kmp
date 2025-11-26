package com.mahbub.realtimevoicetranslate_kmp

import androidx.compose.ui.window.ComposeUIViewController
import com.mahbub.realtimevoicetranslate_kmp.di.initKoin
import com.mahbub.realtimevoicetranslate_kmp.tts.TTSProvider
import com.mahbub.realtimevoicetranslate_kmp.tts.TTSProviderHolder

fun MainViewController(ttsProvider: TTSProvider) = ComposeUIViewController(configure = {
    TTSProviderHolder.provider = ttsProvider
    initKoin()
}) {
    App()
}
