package com.mahbub.realtimevoicetranslate_kmp.domain.repository

import com.mahbub.realtimevoicetranslate_kmp.domain.model.Result
import com.mahbub.realtimevoicetranslate_kmp.domain.model.TextRecognitionState
import kotlinx.coroutines.flow.Flow

interface TextRecognitionRepository {
    fun getState(): Flow<TextRecognitionState>
    fun requestPermission(): Result<Boolean>
    fun pickImageAndRecognize(): Result<Unit>
    fun showNeedPermission()
    fun dismissPermissionDialog()
    fun openAppSettings()
}
