package com.mahbub.realtimevoicetranslate_kmp

import androidx.compose.ui.window.ComposeUIViewController
import com.mahbub.realtimevoicetranslate_kmp.core.platform.setTTSProvider
import com.mahbub.realtimevoicetranslate_kmp.di.initKoin
import com.mahbub.realtimevoicetranslate_kmp.domain.repository.TTSProvider

fun MainViewController(ttsProvider: TTSProvider) = ComposeUIViewController(
    configure = {
        initKoin()
        setTTSProvider { ttsProvider }
    }
) {
    App()
}