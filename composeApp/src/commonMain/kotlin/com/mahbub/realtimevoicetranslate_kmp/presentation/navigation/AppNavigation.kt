package com.mahbub.realtimevoicetranslate_kmp.presentation.navigation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.mahbub.realtimevoicetranslate_kmp.presentation.screen.LandingScreen
import com.mahbub.realtimevoicetranslate_kmp.presentation.screen.SpeechToTextScreen
import com.mahbub.realtimevoicetranslate_kmp.presentation.screen.textToSpeach.TextToSpeechScreen
import com.mahbub.realtimevoicetranslate_kmp.presentation.screen.textrecognition.TextRecognitionScreen

@Composable
fun AppNavigation() {
    MaterialTheme {
        val backStack = remember { mutableStateListOf<ScreenRoute>() }
        var current by remember { mutableStateOf<ScreenRoute>(ScreenRoute.Landing) }

        fun navigate(route: ScreenRoute) {
            backStack.add(current)
            current = route
        }

        fun popBack() {
            if (backStack.isNotEmpty()) {
                current = backStack.removeAt(backStack.lastIndex)
            }
        }

        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            when (current) {
                ScreenRoute.Landing -> LandingScreen(
                    onClickSpeechToText = { navigate(ScreenRoute.SpeechToText) },
                    onClickTextToSpeech = { navigate(ScreenRoute.TextToSpeech) },
                    onClickTextRecognition = { navigate(ScreenRoute.TextRecognition) }
                )
                ScreenRoute.SpeechToText -> SpeechToTextScreen(onBack = { popBack() })
                ScreenRoute.TextToSpeech -> TextToSpeechScreen(onBack = { popBack() })
                ScreenRoute.TextRecognition -> TextRecognitionScreen(onBack = { popBack() })
            }
        }
    }
}
