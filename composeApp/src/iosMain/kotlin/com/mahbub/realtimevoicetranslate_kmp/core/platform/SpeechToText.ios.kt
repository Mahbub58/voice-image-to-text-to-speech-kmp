package com.mahbub.realtimevoicetranslate_kmp.core.platform

import com.mahbub.realtimevoicetranslate_kmp.data.ListeningStatus
import com.mahbub.realtimevoicetranslate_kmp.data.PermissionRequestStatus
import com.mahbub.realtimevoicetranslate_kmp.data.RecognizerError
import com.mahbub.realtimevoicetranslate_kmp.data.TranscriptState
import com.mahbub.realtimevoicetranslate_kmp.data.Error
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import platform.AVFAudio.*
import platform.Foundation.NSLocale
import platform.Foundation.NSURL
import platform.Foundation.localeIdentifier
import platform.Speech.*
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UIKit.UIPasteboard
import kotlin.coroutines.resume

actual class SpeechToText {

    // State management
    private val _transcriptState = MutableStateFlow(
        TranscriptState(
            listeningStatus = ListeningStatus.INACTIVE,
            error = Error(isError = false),
            transcript = null,
        )
    )

    actual val transcriptState: MutableStateFlow<TranscriptState>
        get() = _transcriptState

    // Audio components with proper lifecycle management
    private var audioEngine: AVAudioEngine? = null
    private var recognitionRequest: SFSpeechAudioBufferRecognitionRequest? = null
    private var recognitionTask: SFSpeechRecognitionTask? = null
    private var speechRecognizer: SFSpeechRecognizer? = null

    // State flags
    private var isTapInstalled = false
    private var isCurrentlyTranscribing = false
    private var hasRetriedNoSpeech = false

    // Thread-safe operations
    private val transcriptionMutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        initializeRecognizer()
        loadSupportedLanguages()
    }

    private fun initializeRecognizer() {
        speechRecognizer = SFSpeechRecognizer()
    }

    private fun loadSupportedLanguages() {
        getSupportedLanguages { supportedLanguages ->
            _transcriptState.update { it.copy(supportedLanguages = supportedLanguages) }
        }
    }

    actual fun startTranscribing() {
        scope.launch {
            transcriptionMutex.withLock {
                if (isCurrentlyTranscribing) {
                    println("SpeechToText.iOS: Already transcribing, ignoring start request")
                    return@launch
                }

                val recognizer = speechRecognizer
                if (recognizer == null || !recognizer.isAvailable()) {
                    handleRecognizerUnavailable()
                    return@launch
                }

                try {
                    startTranscriptionSession(recognizer)
                } catch (e: Exception) {
                    handleTranscriptionError(e.message ?: "Unknown error starting transcription")
                }
            }
        }
    }

    private fun startTranscriptionSession(recognizer: SFSpeechRecognizer) {
        println("SpeechToText.iOS: Starting transcription session")

        isCurrentlyTranscribing = true
        updateListeningStatus(ListeningStatus.LISTENING)

        val (engine, request) = prepareAudioEngine()
        audioEngine = engine
        recognitionRequest = request

        recognitionTask = recognizer.recognitionTaskWithRequest(request) { result, error ->
            handleRecognitionResult(result, error)
        }
    }

    private fun handleRecognitionResult(
        result: platform.Speech.SFSpeechRecognitionResult?,
        error: platform.Foundation.NSError?
    ) {
        when {
            result != null -> {
                val transcript = result.bestTranscription.formattedString
                updateTranscriptSuccess(transcript)
                hasRetriedNoSpeech = false
            }
            error != null -> {
                handleRecognitionError(error)
            }
        }
    }

    private fun handleRecognitionError(error: platform.Foundation.NSError) {
        val errorMessage = error.localizedDescription
        val isNoSpeechError = errorMessage?.contains("No speech", ignoreCase = true) == true

        if (isNoSpeechError && !hasRetriedNoSpeech) {
            println("SpeechToText.iOS: No speech detected, retrying...")
            hasRetriedNoSpeech = true
            scope.launch {
                cleanupResources()
                startTranscribing()
            }
        } else {
            println("SpeechToText.iOS: Recognition error: $errorMessage")
            handleTranscriptionError(errorMessage)
            hasRetriedNoSpeech = false
        }
    }

    actual fun stopTranscribing() {
        scope.launch {
            transcriptionMutex.withLock {
                println("SpeechToText.iOS: Stopping transcription")
                cleanupResources()
                updateListeningStatus(ListeningStatus.INACTIVE)
                Error(isError = false, message = null)
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun cleanupResources() {
        println("SpeechToText.iOS: Cleaning up resources")

        // Stop recognition request
        recognitionRequest?.endAudio()

        // Cancel and cleanup recognition task
        recognitionTask?.let { task ->
            runCatching { task.finish() }
            runCatching { task.cancel() }
        }

        // Remove audio tap if installed
        audioEngine?.let { engine ->
            if (isTapInstalled) {
                runCatching {
                    engine.inputNode.removeTapOnBus(0u)
                    println("SpeechToText.iOS: Audio tap removed")
                }
                isTapInstalled = false
            }

            // Stop audio engine
            if (engine.running) {
                engine.stop()
                println("SpeechToText.iOS: Audio engine stopped")
            }
        }

        // Deactivate audio session
        runCatching {
            AVAudioSession.sharedInstance().setActive(
                false,
                1uL, // AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation
                null
            )
            println("SpeechToText.iOS: Audio session deactivated")
        }

        // Clear references
        recognitionTask = null
        audioEngine = null
        recognitionRequest = null
        isCurrentlyTranscribing = false

        println("SpeechToText.iOS: Cleanup completed")
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun prepareAudioEngine(): Pair<AVAudioEngine, SFSpeechAudioBufferRecognitionRequest> {
        val engine = AVAudioEngine()
        val request = SFSpeechAudioBufferRecognitionRequest().apply {
            shouldReportPartialResults = true
            requiresOnDeviceRecognition = false
        }

        configureAudioSession()
        installAudioTap(engine, request)
        startAudioEngine(engine)

        return Pair(engine, request)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun configureAudioSession() {
        val audioSession = AVAudioSession.sharedInstance()

        val categorySuccess = audioSession.setCategory(
            AVAudioSessionCategoryRecord,
            AVAudioSessionModeSpokenAudio,
            0uL, // AVAudioSessionCategoryOptionDefaultToSpeaker
            null
        )

        if (!categorySuccess) {
            println("SpeechToText.iOS: Warning - Failed to set audio session category")
        }

        audioSession.setPreferredSampleRate(16000.0, null)
        audioSession.setPreferredIOBufferDuration(0.02, null)

        val activateSuccess = audioSession.setActive(
            true,
            1uL, // AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation
            null
        )

        if (!activateSuccess) {
            println("SpeechToText.iOS: Warning - Failed to activate audio session")
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun installAudioTap(
        engine: AVAudioEngine,
        request: SFSpeechAudioBufferRecognitionRequest
    ) {
        val inputNode = engine.inputNode
        val recordingFormat = inputNode.outputFormatForBus(0u)

        inputNode.installTapOnBus(0u, 2048u, recordingFormat) { buffer, _ ->
            buffer?.let { request.appendAudioPCMBuffer(it) }
        }

        isTapInstalled = true
        println("SpeechToText.iOS: Audio tap installed")
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun startAudioEngine(engine: AVAudioEngine) {
        engine.prepare()
        val started = engine.startAndReturnError(null)

        if (started) {
            println("SpeechToText.iOS: Audio engine started successfully")
        } else {
            throw IllegalStateException("Failed to start audio engine")
        }
    }

    actual fun requestPermission(onPermissionResult: (PermissionRequestStatus) -> Unit) {
        scope.launch {
            try {
                val recordPermission = checkRecordPermission()
                val speechPermission = checkSpeechPermission()

                val status = determinePermissionStatus(recordPermission, speechPermission)
                onPermissionResult(status)
            } catch (e: Exception) {
                println("SpeechToText.iOS: Error checking permissions: ${e.message}")
                onPermissionResult(PermissionRequestStatus.NOT_ALLOWED)
            }
        }
    }

    private suspend fun checkRecordPermission(): Boolean =
        suspendCancellableCoroutine { continuation ->
            AVAudioSession.sharedInstance().requestRecordPermission { granted ->
                if (continuation.isActive) {
                    continuation.resume(granted)
                }
            }
        }

    private suspend fun checkSpeechPermission(): Boolean =
        suspendCancellableCoroutine { continuation ->
            SFSpeechRecognizer.requestAuthorization { status ->
                if (continuation.isActive) {
                    val isAuthorized = status ==
                            SFSpeechRecognizerAuthorizationStatus.SFSpeechRecognizerAuthorizationStatusAuthorized
                    continuation.resume(isAuthorized)
                }
            }
        }

    private fun determinePermissionStatus(
        hasRecordPermission: Boolean,
        hasSpeechPermission: Boolean
    ): PermissionRequestStatus {
        return when {
            hasRecordPermission && hasSpeechPermission ->
                PermissionRequestStatus.ALLOWED

            isPermissionDenied() ->
                PermissionRequestStatus.NEVER_ASK_AGAIN

            else ->
                PermissionRequestStatus.NOT_ALLOWED
        }
    }

    private fun isPermissionDenied(): Boolean {
        val recordStatus = AVAudioSession.sharedInstance().recordPermission
        val speechStatus = SFSpeechRecognizer.authorizationStatus()

        return recordStatus == AVAudioSessionRecordPermissionDenied ||
                speechStatus == SFSpeechRecognizerAuthorizationStatus.SFSpeechRecognizerAuthorizationStatusDenied
    }

    actual fun setLanguage(languageCode: String) {
        scope.launch {
            transcriptionMutex.withLock {
                val wasTranscribing = isCurrentlyTranscribing

                if (wasTranscribing) {
                    cleanupResources()
                }

                val locale = NSLocale(languageCode)
                speechRecognizer = SFSpeechRecognizer(locale)
                println("SpeechToText.iOS: Language set to $languageCode")

                if (wasTranscribing) {
                    startTranscribing()
                }
            }
        }
    }

    actual fun getSupportedLanguages(onLanguagesResult: (List<String>) -> Unit) {
        val supportedLocales = SFSpeechRecognizer.supportedLocales()
        val languages = supportedLocales.mapNotNull {
            (it as? NSLocale)?.localeIdentifier()
        }
        onLanguagesResult(languages)
    }

    actual fun copyText(text: String) {
        UIPasteboard.generalPasteboard.string = text
    }

    actual fun showNeedPermission() {
        _transcriptState.update { it.copy(showPermissionNeedDialog = true) }
    }

    actual fun dismissPermissionDialog() {
        _transcriptState.update { it.copy(showPermissionNeedDialog = false) }
    }

    actual fun openAppSettings() {
        val recordStatus = AVAudioSession.sharedInstance().recordPermission
        val speechStatus = SFSpeechRecognizer.authorizationStatus()

        when {
            recordStatus == AVAudioSessionRecordPermissionDenied ->
                openMicrophoneSettings()

            speechStatus == SFSpeechRecognizerAuthorizationStatus.SFSpeechRecognizerAuthorizationStatusDenied ->
                openSpeechRecognitionSettings()

            else ->
                openGeneralSettings()
        }

        dismissPermissionDialog()
    }

    private fun openMicrophoneSettings() {
        val url = NSURL.URLWithString("prefs:root=Privacy&path=MICROPHONE")
        openURLOrFallback(url)
    }

    private fun openSpeechRecognitionSettings() {
        val url = NSURL.URLWithString("prefs:root=Privacy&path=SPEECH_RECOGNITION")
        openURLOrFallback(url)
    }

    private fun openURLOrFallback(url: NSURL?) {
        val app = UIApplication.sharedApplication

        if (url != null && app.canOpenURL(url)) {
            app.openURL(url, mapOf<Any?, Any>(), null)
        } else {
            openGeneralSettings()
        }
    }

    private fun openGeneralSettings() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
        url?.let { UIApplication.sharedApplication.openURL(it, mapOf<Any?, Any>(), null) }
    }

    // Helper methods for state updates
    private fun updateListeningStatus(status: ListeningStatus) {
        _transcriptState.update { it.copy(listeningStatus = status) }
    }

    private fun updateTranscriptSuccess(transcript: String) {
        _transcriptState.update {
            it.copy(
                transcript = transcript,
                error = Error(isError = false)
            )
        }
    }

    private fun handleTranscriptionError(message: String) {
        println("SpeechToText.iOS: Transcription error: $message")
        cleanupResources()

        _transcriptState.update {
            it.copy(
                listeningStatus = ListeningStatus.INACTIVE,
                error = Error(isError = false, message = message),
                transcript = null
            )
        }
    }

    private fun handleRecognizerUnavailable() {
        _transcriptState.update {
            it.copy(
                listeningStatus = ListeningStatus.INACTIVE,
                error = Error(
                    isError = true,
                    message = RecognizerError.RecognizerIsUnavailable.message
                )
            )
        }
    }

    // Cleanup on deinitialization
    fun cleanup() {
        scope.launch {
            transcriptionMutex.withLock {
                cleanupResources()
            }
            scope.cancel()
        }
    }
}