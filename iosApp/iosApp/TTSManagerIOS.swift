import ComposeApp
import AVFoundation
import Foundation

class TTSManagerIOS: ComposeApp.TTSProvider {
    static let shared = TTSManagerIOS()

    private let synthesizer = AVSpeechSynthesizer()
    private var delegateHandler: TTSSynthesizerDelegate?

    private var isPausedState = false
    private var originalText = ""
    private var pausedPosition = 0
    private var resumeOffset = 0

    // Callback blocks
    private var onWordBoundaryCallback: ((KotlinInt, KotlinInt) -> Void)?
    private var onStartCallback: (() -> Void)?
    private var onCompleteCallback: (() -> Void)?

    private func setupDelegate() {
        delegateHandler = TTSSynthesizerDelegate(
            onStart: { [weak self] in
                print("🎤 TTS Started")
                self?.onStartCallback?()
            },
            onWordBoundary: { [weak self] start, end in
                print("📍 Word boundary: \(start)-\(end)")
                self?.handleWordBoundary(start: start, end: end)
            },
            onFinish: { [weak self] in
                print("✅ TTS Finished")
                self?.handleFinish()
            }
        )
        synthesizer.delegate = delegateHandler
    }

    func initialize(onInitialized: @escaping () -> Void) {
        print("🚀 TTS Initialized")
        setupDelegate()
        configureAudioSessionForPlayback()
        onInitialized()
    }

    func speak(
        text: String,
        onWordBoundary: @escaping (KotlinInt, KotlinInt) -> Void,
        onStart: @escaping () -> Void,
        onComplete: @escaping () -> Void
    ) {
        print("🗣️ Speak called with text: '\(text.prefix(50))...'")
        print("📊 Current state - isPaused: \(isPausedState), resumeOffset: \(resumeOffset)")

        // Configure audio session for playback before speaking
        configureAudioSessionForPlayback()

        // Store callbacks
        onWordBoundaryCallback = onWordBoundary
        onStartCallback = onStart
        onCompleteCallback = onComplete

        // Check if originalText is empty to determine if this is first time or resume
        let isFirstTimeSpeak = originalText.isEmpty

        if isFirstTimeSpeak {
            print("🆕 First time speaking - resetting state")
            originalText = text
            pausedPosition = 0
            resumeOffset = 0
        } else {
            print("🔄 Resume speaking - keeping resumeOffset: \(resumeOffset)")
            // This is a resume call - don't reset anything
        }

        // Set paused state to false after checking
        isPausedState = false

        let utterance = AVSpeechUtterance(string: text)
        utterance.rate = 0.5

        synthesizer.speak(utterance)
    }

    func stop_() {
        print("🛑 Stop called")
        synthesizer.stopSpeaking(at: .immediate)
        resetAudioSession()
        isPausedState = false
        pausedPosition = 0
        resumeOffset = 0
        originalText = ""
        onWordBoundaryCallback?(-1, -1)
    }

    func pause_() {
        print("⏸️ Pause called")
        print("📍 Pausing at position: \(pausedPosition)")
        synthesizer.stopSpeaking(at: .immediate)  // Changed from pauseSpeaking to stopSpeaking
        isPausedState = true
    }

    func resume_() {
        print("▶️ Resume called")
        print("📊 Resume state - isPaused: \(isPausedState), pausedPos: \(pausedPosition), originalText.count: \(originalText.count)")

        if isPausedState && !originalText.isEmpty {
            let remainingText = getRemainingText()
            print("📝 Remaining text: '\(remainingText.prefix(50))...'")

            if !remainingText.isEmpty {
                let wordStartPos = findWordStart(text: originalText, position: pausedPosition)
                resumeOffset = wordStartPos
                print("📍 Resume offset set to: \(resumeOffset)")

                if let wordBoundary = onWordBoundaryCallback,
                   let start = onStartCallback,
                   let complete = onCompleteCallback {

                    // Set paused state to false BEFORE calling speak
                    isPausedState = false
                    print("🔄 Calling speak with remaining text, resumeOffset should stay: \(resumeOffset)")
                    speak(text: remainingText, onWordBoundary: wordBoundary, onStart: start, onComplete: complete)
                    print("📍 After speak call, resumeOffset is: \(resumeOffset)")
                }
            }
        }
    }

    func isPlaying() -> Bool {
        let playing = synthesizer.isSpeaking && !isPausedState
        print("❓ isPlaying: \(playing) (speaking: \(synthesizer.isSpeaking), paused: \(isPausedState))")
        return playing
    }

    func isPaused() -> Bool {
        print("❓ isPaused: \(isPausedState)")
        return isPausedState
    }

    func release_() {
        print("🗑️ Release called")
        synthesizer.stopSpeaking(at: .immediate)
        resetAudioSession()
        synthesizer.delegate = nil
        delegateHandler = nil
        isPausedState = false
        pausedPosition = 0
        resumeOffset = 0
        originalText = ""
    }

    private func handleWordBoundary(start: Int, end: Int) {
        if !isPausedState {
            // Calculate position in original text (same logic as Android)
            let actualStart = resumeOffset + start
            let actualEnd = resumeOffset + end - 1  // Note: end-1 like Android

            print("🎯 Handling word boundary: local(\(start)-\(end)) -> actual(\(actualStart)-\(actualEnd))")
            print("📏 Original text length: \(originalText.count), resumeOffset: \(resumeOffset)")

            guard actualStart >= 0 && actualStart < originalText.count else {
                print("⚠️ Word boundary actualStart(\(actualStart)) out of bounds!")
                return
            }

            // Find word boundaries in original text (same as Android)
            let wordStart = findWordStart(text: originalText, position: actualStart)
            let wordEnd = findWordEnd(text: originalText, position: min(actualEnd, originalText.count - 1))

            // Update paused position for future resume (same as Android)
            pausedPosition = wordStart

            print("✨ Highlighting: \(wordStart)-\(wordEnd), updated pausedPosition: \(pausedPosition)")
            print("📝 Highlighted text: '\(String(Array(originalText)[wordStart...wordEnd]))'")

            onWordBoundaryCallback?(KotlinInt(integerLiteral: wordStart), KotlinInt(integerLiteral: wordEnd))
        }
    }

    private func handleFinish() {
        print("🏁 Speech finished - isPaused: \(isPausedState)")
        if !isPausedState {
            print("🏁 Speech finished normally")
            onWordBoundaryCallback?(-1, -1)
            onCompleteCallback?()
            originalText = ""
            pausedPosition = 0
            resumeOffset = 0
        } else {
            print("⏸️ Speech finished due to pause - keeping state")
            // Don't reset state when paused, keep everything for resume
        }
    }

    private func configureAudioSessionForPlayback() {
        let audioSession = AVAudioSession.sharedInstance()
        
        do {
            // Deactivate current session first to ensure clean state
            try audioSession.setActive(false, options: .notifyOthersOnDeactivation)
            print("🔇 Audio session deactivated for TTS setup")
            
            // Configure for playback with speaker output
            try audioSession.setCategory(
                .playback,
                mode: .default,
                options: [.defaultToSpeaker, .duckOthers]
            )
            print("🔊 Audio session configured for playback")
            
            // Activate the session
            try audioSession.setActive(true, options: .notifyOthersOnDeactivation)
            print("🔊 Audio session activated for TTS")
            
        } catch {
            print("❌ Failed to configure audio session for TTS: \(error)")
        }
    }

    private func resetAudioSession() {
        let audioSession = AVAudioSession.sharedInstance()
        
        do {
            // Deactivate the session to allow other audio
            try audioSession.setActive(false, options: .notifyOthersOnDeactivation)
            print("🔇 Audio session deactivated after TTS stop")
        } catch {
            print("❌ Failed to reset audio session: \(error)")
        }
    }

    private func getRemainingText() -> String {
        if pausedPosition < originalText.count {
            let wordStartPos = findWordStart(text: originalText, position: pausedPosition)
            let startIndex = originalText.index(originalText.startIndex, offsetBy: wordStartPos)
            let remaining = String(originalText[startIndex...])
            print("📝 getRemainingText: pausedPos=\(pausedPosition), wordStart=\(wordStartPos), remaining='\(remaining.prefix(30))...'")
            return remaining
        }
        print("📝 getRemainingText: No remaining text")
        return ""
    }

    private func findWordStart(text: String, position: Int) -> Int {
        let safePosition = max(0, min(position, text.count - 1))
        var start = safePosition

        let textArray = Array(text)
        while start > 0 && !textArray[start - 1].isWhitespace {
            start -= 1
        }
        return start
    }

    private func findWordEnd(text: String, position: Int) -> Int {
        let safePosition = max(0, min(position, text.count - 1))
        var end = safePosition

        let textArray = Array(text)
        while end < textArray.count - 1 && !textArray[end + 1].isWhitespace {
            end += 1
        }
        return end
    }
}

private class TTSSynthesizerDelegate: NSObject, AVSpeechSynthesizerDelegate {
    let onStart: () -> Void
    let onWordBoundary: (Int, Int) -> Void
    let onFinish: () -> Void

    init(
        onStart: @escaping () -> Void,
        onWordBoundary: @escaping (Int, Int) -> Void,
        onFinish: @escaping () -> Void
    ) {
        self.onStart = onStart
        self.onWordBoundary = onWordBoundary
        self.onFinish = onFinish
        super.init()
    }

    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didStart utterance: AVSpeechUtterance) {
        onStart()
    }

    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, willSpeakRangeOfSpeechString characterRange: NSRange, utterance: AVSpeechUtterance) {
        let start = characterRange.location
        let end = characterRange.location + characterRange.length - 1
        onWordBoundary(start, end)
    }

    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didFinish utterance: AVSpeechUtterance) {
        onFinish()
    }
}