package com.mahbub.realtimevoicetranslate_kmp.core.platform.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import com.mahbub.realtimevoicetranslate_kmp.data.repository.SpeechToTextRepositoryImpl
import com.mahbub.realtimevoicetranslate_kmp.data.repository.TextToSpeechRepositoryImpl
import com.mahbub.realtimevoicetranslate_kmp.domain.repository.SpeechToTextRepository
import com.mahbub.realtimevoicetranslate_kmp.domain.repository.TextToSpeechRepository
import com.mahbub.realtimevoicetranslate_kmp.domain.usecase.CopyTranscriptUseCase
import com.mahbub.realtimevoicetranslate_kmp.domain.usecase.InitializeTTSUseCase
import com.mahbub.realtimevoicetranslate_kmp.domain.usecase.PauseTTSUseCase
import com.mahbub.realtimevoicetranslate_kmp.domain.usecase.RequestPermissionUseCase
import com.mahbub.realtimevoicetranslate_kmp.domain.usecase.ResumeTTSUseCase
import com.mahbub.realtimevoicetranslate_kmp.domain.usecase.SpeakTextUseCase
import com.mahbub.realtimevoicetranslate_kmp.domain.usecase.StartSpeechRecognitionUseCase
import com.mahbub.realtimevoicetranslate_kmp.domain.usecase.StopSpeechRecognitionUseCase
import com.mahbub.realtimevoicetranslate_kmp.domain.usecase.StopTTSUseCase
import com.mahbub.realtimevoicetranslate_kmp.presentation.screen.SpeechToTextViewModel
import com.mahbub.realtimevoicetranslate_kmp.presentation.screen.textToSpeach.TextToSpeechViewModel

expect val platformModule: Module

val sharedModule = module {
    // Repository bindings
    single<SpeechToTextRepository> { SpeechToTextRepositoryImpl(get()) }
    single<TextToSpeechRepository> { TextToSpeechRepositoryImpl() }
    
    // Use case bindings
    single { StartSpeechRecognitionUseCase(get()) }
    single { StopSpeechRecognitionUseCase(get()) }
    single { RequestPermissionUseCase(get()) }
    single { CopyTranscriptUseCase(get()) }
    single { InitializeTTSUseCase(get()) }
    single { SpeakTextUseCase(get()) }
    single { PauseTTSUseCase(get()) }
    single { ResumeTTSUseCase(get()) }
    single { StopTTSUseCase(get()) }
    
    // ViewModel bindings
    viewModel { SpeechToTextViewModel(get(), get(), get(), get(), get()) }
    viewModel { TextToSpeechViewModel(get(), get(), get(), get(), get()) }
}
