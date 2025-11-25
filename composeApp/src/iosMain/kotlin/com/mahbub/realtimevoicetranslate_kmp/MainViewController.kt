package com.mahbub.realtimevoicetranslate_kmp

import androidx.compose.ui.window.ComposeUIViewController
import com.mahbub.realtimevoicetranslate_kmp.di.initKoin

fun MainViewController() = ComposeUIViewController(configure = {
    initKoin()
}) {
    App()
}