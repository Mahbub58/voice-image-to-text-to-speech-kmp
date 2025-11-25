package com.mahbub.realtimevoicetranslate_kmp.core.platform.di

import com.mahbub.realtimevoicetranslate_kmp.core.platform.SpeechToText
import com.mahbub.realtimevoicetranslate_kmp.core.platform.TextToSpeech
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule = module {
    single { SpeechToText() }
    single { TextToSpeech() }
}
