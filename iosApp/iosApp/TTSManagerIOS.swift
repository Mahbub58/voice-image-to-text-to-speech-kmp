import AVFoundation
import Foundation

class TTSManagerIOS {
    static let shared = TTSManagerIOS()

    private let synthesizer = AVSpeechSynthesizer()
    private var delegateHandler: TTSSynthesizerDelegate?

    private var isPausedState = false
    private var originalText = ""
    private var pausedPosition = 0
    private var resumeOffset = 0

    private init() {
        delegateHandler = TTSSynthesizerDelegate()
        synthesizer.delegate = delegateHandler
        
        // Configure audio session
        configureAudioSession()
        
        print("🎤 TTSManagerIOS initialized")
    }
    
    private func configureAudioSession() {
        print("🔊 Configuring audio session...")
        let session = AVAudioSession.sharedInstance()
        
        do {
            try session.setCategory(.playback, mode: .spokenAudio, options: [.duckOthers])
            try session.setActive(true)
            print("✅ Audio session configured successfully")
        } catch {
            print("❌ Audio session configuration error: \(error)")
        }
    }
    
    func initialize() {
        print("🔄 TTS Manager initialize called")
        verifyTTSChain()
    }
    
    func speak(text: String) {
        print("🗣️ Swift TTS speak() called with text: '\(text.prefix(50))...'")
        
        // Reset state for new speech
        originalText = text
        pausedPosition = 0
        resumeOffset = 0
        isPausedState = false
        
        // Configure audio session
        configureAudioSession()
        
        // Create utterance
        let utterance = AVSpeechUtterance(string: text)
        utterance.rate = AVSpeechUtteranceDefaultSpeechRate
        utterance.volume = 1.0
        utterance.pitchMultiplier = 1.0
        
        // Configure voice with better error handling
        let preferred = Locale.preferredLanguages.first ?? "en-US"
        let code = preferred.replacingOccurrences(of: "_", with: "-")
        print("🎤 Setting voice for language: \(code)")
        
        // Try to find the best available voice
        var selectedVoice: AVSpeechSynthesisVoice?
        
        // First try the exact language code
        if let exactVoice = AVSpeechSynthesisVoice(language: code) {
            selectedVoice = exactVoice
            print("✅ Found exact voice: \(exactVoice.name) (\(exactVoice.language))")
        }
        // Then try the base language (e.g., "en" for "en-US")
        else if let baseCode = code.split(separator: "-").first,
                let baseVoice = AVSpeechSynthesisVoice(language: String(baseCode)) {
            selectedVoice = baseVoice
            print("✅ Found base voice: \(baseVoice.name) (\(baseVoice.language))")
        }
        // Finally fall back to English
        else if let fallbackVoice = AVSpeechSynthesisVoice(language: "en-US") {
            selectedVoice = fallbackVoice
            print("✅ Using fallback voice: \(fallbackVoice.name) (\(fallbackVoice.language))")
        }
        else {
            // Use system default voice
            print("⚠️ No specific voice found, using system default")
        }
        
        if let voice = selectedVoice {
            utterance.voice = voice
        }
        
        print("🗣️ Speaking text: '\(text.prefix(50))...'")
        print("📊 Rate: \(utterance.rate), Volume: \(utterance.volume), Voice: \(utterance.voice?.name ?? "default")")

        synthesizer.speak(utterance)
    }
    
    func stop() {
        print("⏹️ Swift TTS stop() called")
        
        if synthesizer.isSpeaking {
            synthesizer.stopSpeaking(at: .immediate)
            print("🛑 Stopped speaking immediately")
        } else {
            print("⚠️ Not currently speaking")
        }
        
        // Reset state
        isPausedState = false
        originalText = ""
        pausedPosition = 0
        resumeOffset = 0
    }
    
    func pause() {
        print("⏸️ Swift TTS pause() called")
        
        if synthesizer.isSpeaking {
            let success = synthesizer.pauseSpeaking(at: .word)
            if success {
                isPausedState = true
                print("✅ Paused speaking at word boundary")
            } else {
                print("❌ Failed to pause speaking")
            }
        } else {
            print("⚠️ Not currently speaking")
        }
    }
    
    func resume() {
        print("▶️ Swift TTS resume() called")
        
        if synthesizer.isPaused {
            let success = synthesizer.continueSpeaking()
            if success {
                isPausedState = false
                print("✅ Resumed speaking")
            } else {
                print("❌ Failed to resume speaking")
            }
        } else {
            print("⚠️ Not currently paused")
        }
    }
    
    func checkTTSStatus() {
        print("🔍 Checking TTS status...")
        print("   isSpeaking: \(synthesizer.isSpeaking)")
        print("   isPaused: \(synthesizer.isPaused)")
        print("   delegate set: \(synthesizer.delegate != nil)")
        
        // Test utterance creation
        let testUtterance = AVSpeechUtterance(string: "Status test")
        testUtterance.rate = AVSpeechUtteranceDefaultSpeechRate
        testUtterance.volume = 0.1 // Low volume for test
        
        if let voice = AVSpeechSynthesisVoice(language: "en-US") {
            testUtterance.voice = voice
            print("   ✅ Test utterance created successfully")
        } else {
            print("   ❌ Failed to create voice for test utterance")
        }
    }
    
    func verifyTTSChain() {
        print("🔗 Verifying complete TTS chain...")
        
        // Step 1: Check basic AVSpeechSynthesizer functionality
        print("1️⃣ Testing basic AVSpeechSynthesizer...")
        let testUtterance = AVSpeechUtterance(string: "Chain test")
        testUtterance.rate = AVSpeechUtteranceDefaultSpeechRate
        testUtterance.volume = 0.5 // Lower volume for test
        testUtterance.pitchMultiplier = 1.0
        
        if let voice = AVSpeechSynthesisVoice(language: "en-US") {
            testUtterance.voice = voice
            print("   ✅ Basic utterance creation successful")
        } else {
            print("   ❌ Failed to create voice")
            return
        }
        
        // Step 2: Test audio session
        print("2️⃣ Testing audio session...")
        let session = AVAudioSession.sharedInstance()
        do {
            try session.setCategory(.playback, mode: .spokenAudio, options: [.duckOthers])
            try session.setActive(true)
            print("   ✅ Audio session configured")
        } catch {
            print("   ❌ Audio session error: \(error)")
            return
        }
        
        // Step 3: Test delegate setup
        print("3️⃣ Testing delegate...")
        if synthesizer.delegate != nil {
            print("   ✅ Delegate is set")
        } else {
            print("   ❌ Delegate not set")
            return
        }
        
        // Step 4: Test actual speech with a very short utterance
        print("4️⃣ Testing actual speech...")
        let shortTest = AVSpeechUtterance(string: "Test")
        shortTest.rate = AVSpeechUtteranceDefaultSpeechRate
        shortTest.volume = 0.3 // Very low volume
        shortTest.voice = testUtterance.voice
        
        print("   🎙️ Speaking short test...")
        synthesizer.speak(shortTest)
        
        // Give it a moment to start
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            if self.synthesizer.isSpeaking {
                print("   ✅ TTS chain verified - speech is working!")
                self.synthesizer.stopSpeaking(at: .immediate)
            } else {
                print("   ❌ TTS chain broken - speech not starting")
            }
        }
    }
}

// MARK: - AVSpeechSynthesizerDelegate
class TTSSynthesizerDelegate: NSObject, AVSpeechSynthesizerDelegate {
    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didStart utterance: AVSpeechUtterance) {
        print("🎤 TTS started: '\(utterance.speechString.prefix(30))...'")
    }
    
    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didFinish utterance: AVSpeechUtterance) {
        print("✅ TTS finished: '\(utterance.speechString.prefix(30))...'")
    }
    
    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didPause utterance: AVSpeechUtterance) {
        print("⏸️ TTS paused: '\(utterance.speechString.prefix(30))...'")
    }
    
    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didContinue utterance: AVSpeechUtterance) {
        print("▶️ TTS continued: '\(utterance.speechString.prefix(30))...'")
    }
    
    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didCancel utterance: AVSpeechUtterance) {
        print("🛑 TTS cancelled: '\(utterance.speechString.prefix(30))...'")
    }
    
    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, willSpeakRangeOfSpeechString characterRange: NSRange, utterance: AVSpeechUtterance) {
        let word = (utterance.speechString as NSString).substring(with: characterRange)
        print("🔤 Will speak word: '\(word)' at range: \(characterRange)")
    }
}