package com.mahbub.realtimevoicetranslate_kmp.presentation.navigation

sealed class ScreenRoute {
    data object Landing : ScreenRoute()
    data object SpeechToText : ScreenRoute()
    data object TextToSpeech : ScreenRoute()
    data object TextRecognition : ScreenRoute()
}
