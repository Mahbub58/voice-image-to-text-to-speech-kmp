package com.mahbub.realtimevoicetranslate_kmp.domain.repository

import com.mahbub.realtimevoicetranslate_kmp.data.PermissionRequestStatus

interface TextRecognitionProvider {
    fun requestPermission(onPermissionResult: (PermissionRequestStatus) -> Unit)
    fun pickImage(onResult: (String) -> Unit, onError: (Throwable) -> Unit)
    fun showNeedPermission()
    fun dismissPermissionDialog()
    fun openAppSettings()
}
