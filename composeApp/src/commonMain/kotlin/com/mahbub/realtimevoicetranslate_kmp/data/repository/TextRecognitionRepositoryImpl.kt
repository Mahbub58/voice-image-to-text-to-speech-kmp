package com.mahbub.realtimevoicetranslate_kmp.data.repository

import com.mahbub.realtimevoicetranslate_kmp.core.platform.getTextRecognitionProvider
import com.mahbub.realtimevoicetranslate_kmp.data.PermissionRequestStatus
import com.mahbub.realtimevoicetranslate_kmp.domain.model.Result
import com.mahbub.realtimevoicetranslate_kmp.domain.model.TextRecognitionState
import com.mahbub.realtimevoicetranslate_kmp.domain.repository.TextRecognitionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TextRecognitionRepositoryImpl : TextRecognitionRepository {
    private val provider = getTextRecognitionProvider()
    private val _state = MutableStateFlow(TextRecognitionState())

    override fun getState(): Flow<TextRecognitionState> = _state.asStateFlow()

    override fun requestPermission(): Result<Boolean> {
        return try {
            provider.requestPermission { status ->
                when (status) {
                    PermissionRequestStatus.ALLOWED -> {
                        _state.value = _state.value.copy(error = null)
                    }
                    PermissionRequestStatus.NEVER_ASK_AGAIN -> {
                        provider.showNeedPermission()
                        _state.value = _state.value.copy(showPermissionNeedDialog = true)
                    }
                    PermissionRequestStatus.NOT_ALLOWED -> {
                        _state.value = _state.value.copy(error = "Permission denied")
                    }
                }
            }
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override fun pickImageAndRecognize(): Result<Unit> {
        return try {
            _state.value = _state.value.copy(isProcessing = true, error = null)
            provider.pickImage(
                onResult = { text ->
                    _state.value = _state.value.copy(
                        recognizedText = text,
                        isProcessing = false,
                        error = null
                    )
                },
                onError = { e ->
                    _state.value = _state.value.copy(isProcessing = false, error = e.message)
                }
            )
            Result.Success(Unit)
        } catch (e: Exception) {
            _state.value = _state.value.copy(isProcessing = false, error = e.message)
            Result.Error(e)
        }
    }

    override fun showNeedPermission() {
        provider.showNeedPermission()
        _state.value = _state.value.copy(showPermissionNeedDialog = true)
    }

    override fun dismissPermissionDialog() {
        provider.dismissPermissionDialog()
        _state.value = _state.value.copy(showPermissionNeedDialog = false)
    }

    override fun openAppSettings() {
        provider.openAppSettings()
        _state.value = _state.value.copy(showPermissionNeedDialog = false)
    }
}
