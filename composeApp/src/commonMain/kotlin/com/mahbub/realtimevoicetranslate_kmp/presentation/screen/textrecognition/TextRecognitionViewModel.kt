package com.mahbub.realtimevoicetranslate_kmp.presentation.screen.textrecognition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mahbub.realtimevoicetranslate_kmp.domain.model.Result
import com.mahbub.realtimevoicetranslate_kmp.domain.model.TextRecognitionState
import com.mahbub.realtimevoicetranslate_kmp.domain.usecase.RequestTextRecognitionPermissionUseCase
import com.mahbub.realtimevoicetranslate_kmp.domain.usecase.StartTextRecognitionUseCase
import com.mahbub.realtimevoicetranslate_kmp.domain.usecase.ObserveTextRecognitionStateUseCase
import com.mahbub.realtimevoicetranslate_kmp.domain.usecase.DismissTextRecognitionPermissionDialogUseCase
import com.mahbub.realtimevoicetranslate_kmp.domain.usecase.OpenTextRecognitionSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TextRecognitionViewModel(
    private val requestPermissionUseCase: RequestTextRecognitionPermissionUseCase,
    private val startTextRecognitionUseCase: StartTextRecognitionUseCase,
    private val observeStateUseCase: ObserveTextRecognitionStateUseCase,
    private val dismissPermissionDialogUseCase: DismissTextRecognitionPermissionDialogUseCase,
    private val openSettingsUseCase: OpenTextRecognitionSettingsUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(TextRecognitionState())
    val state: StateFlow<TextRecognitionState> = _state

    init {
        observeState()
    }

    private fun observeState() {
        viewModelScope.launch {
            observeStateUseCase().onEach { s ->
                _state.update { s }
            }.collect {}
        }
    }

    fun requestPermission() {
        viewModelScope.launch {
            when (val result = requestPermissionUseCase()) {
                is Result.Success -> {}
                is Result.Error -> {
                    _state.update { it.copy(error = result.exception.message) }
                }
                Result.Loading -> {}
            }
        }
    }

    fun pickImageAndRecognize() {
        viewModelScope.launch {
            when (val result = startTextRecognitionUseCase()) {
                is Result.Success -> {}
                is Result.Error -> {
                    _state.update { it.copy(error = result.exception.message, isProcessing = false) }
                }
                Result.Loading -> {}
            }
        }
    }

    fun dismissPermissionDialog() {
        dismissPermissionDialogUseCase()
    }

    fun openAppSettings() {
        openSettingsUseCase()
    }
}
