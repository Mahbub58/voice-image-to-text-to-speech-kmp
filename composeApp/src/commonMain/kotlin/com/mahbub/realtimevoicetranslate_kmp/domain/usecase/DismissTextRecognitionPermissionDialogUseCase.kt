package com.mahbub.realtimevoicetranslate_kmp.domain.usecase

import com.mahbub.realtimevoicetranslate_kmp.domain.repository.TextRecognitionRepository

class DismissTextRecognitionPermissionDialogUseCase(
    private val repository: TextRecognitionRepository
) {
    operator fun invoke() {
        repository.dismissPermissionDialog()
    }
}
