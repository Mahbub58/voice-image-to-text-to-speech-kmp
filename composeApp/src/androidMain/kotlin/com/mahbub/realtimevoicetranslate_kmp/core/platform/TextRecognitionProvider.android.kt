package com.mahbub.realtimevoicetranslate_kmp.core.platform

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.mahbub.realtimevoicetranslate_kmp.data.PermissionRequestStatus
import com.mahbub.realtimevoicetranslate_kmp.domain.repository.TextRecognitionProvider
import com.mahbub.realtimevoicetranslate_kmp.domain.usecase.activityProvider

actual fun getTextRecognitionProvider(): TextRecognitionProvider {
    return AndroidTextRecognitionProvider()
}

class AndroidTextRecognitionProvider : TextRecognitionProvider {
    private var pickImageLauncher = initPickImageLauncher()
    private var onResultCallback: ((String) -> Unit)? = null
    private var onErrorCallback: ((Throwable) -> Unit)? = null

    private fun initPickImageLauncher() =
        (activityProvider() as ComponentActivity).activityResultRegistry.register(
            "pick_image",
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri == null) {
                onErrorCallback?.invoke(IllegalStateException("No image selected"))
                clearCallbacks()
                return@register
            }
            try {
                val image = InputImage.fromFilePath((activityProvider() as ComponentActivity), uri)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        onResultCallback?.invoke(visionText.text)
                        clearCallbacks()
                    }
                    .addOnFailureListener { e ->
                        onErrorCallback?.invoke(e)
                        clearCallbacks()
                    }
            } catch (e: Exception) {
                onErrorCallback?.invoke(e)
                clearCallbacks()
            }
        }

    private fun clearCallbacks() {
        onResultCallback = null
        onErrorCallback = null
    }

    override fun requestPermission(onPermissionResult: (PermissionRequestStatus) -> Unit) {
        onPermissionResult(PermissionRequestStatus.ALLOWED)
    }

    override fun pickImage(onResult: (String) -> Unit, onError: (Throwable) -> Unit) {
        onResultCallback = onResult
        onErrorCallback = onError
        pickImageLauncher.launch("image/*")
    }

    override fun showNeedPermission() {}

    override fun dismissPermissionDialog() {}

    override fun openAppSettings() {}
}
