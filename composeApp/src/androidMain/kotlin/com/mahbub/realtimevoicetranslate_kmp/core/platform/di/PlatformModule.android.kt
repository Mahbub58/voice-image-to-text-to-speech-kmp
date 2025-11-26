package com.mahbub.realtimevoicetranslate_kmp.core.platform.di

import com.mahbub.realtimevoicetranslate_kmp.core.platform.SpeechToText

import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { SpeechToText(get(), get()) }
}
