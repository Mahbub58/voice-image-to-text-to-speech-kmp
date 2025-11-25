package com.mahbub.realtimevoicetranslate_kmp

import androidx.compose.ui.window.ComposeUIViewController
import com.mahbub.realtimevoicetranslate_kmp.di.initKoin
import com.mahbub.realtimevoicetranslate_kmp.tts.TTSProvider

//fun MainViewController() = ComposeUIViewController(configure = {
//    initKoin()
//}) {
//    App()
//}

fun MainViewController(ttsProvider: TTSProvider) = ComposeUIViewController(configure = {
    initKoin()
}) {
    App()
}
