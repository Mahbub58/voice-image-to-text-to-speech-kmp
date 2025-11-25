package com.mahbub.realtimevoicetranslate_kmp.core.platform.di

import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module
import com.mahbub.realtimevoicetranslate_kmp.presentation.screen.SpeechToTextViewModel

expect val platformModule: Module

val sharedModule = module {
    viewModel { SpeechToTextViewModel(get()) }
}
