# voice-image-to-text-to-speach-kmp

A cross‑platform (Android + iOS) app built with Compose Multiplatform that delivers:

- Speech‑to‑Text (STT) voice transcription
- Text‑to‑Speech (TTS) playback with word highlighting
- Image Text Recognition (OCR) from the photo library (Android ML Kit, iOS Vision)

The project is modular and follows a clean architecture with `domain` use cases, repository abstractions, and platform providers bridged via expect/actual implementations.

## Features
- Speech to Text: microphone input, permission dialog, start/stop, language selection
- Text to Speech: play/pause/resume/stop; highlights current word boundary in UI
- Text Recognition: pick image from gallery and extract text
- Common UI in `commonMain` with platform providers in `androidMain` and `iosMain`
- Dependency Injection via Koin

## Architecture Overview
- `domain/`
  - Business rules and models
  - Use cases: `StartSpeechRecognitionUseCase`, `StopSpeechRecognitionUseCase`, `RequestPermissionUseCase`, `InitializeTTSUseCase`, `SpeakTextUseCase`, `PauseTTSUseCase`, `ResumeTTSUseCase`, `StopTTSUseCase`, `ObserveTTSStateUseCase`, `ReleaseTTSUseCase`, `StartTextRecognitionUseCase`, `RequestTextRecognitionPermissionUseCase`, `ObserveTextRecognitionStateUseCase`, `DismissTextRecognitionPermissionDialogUseCase`, `OpenTextRecognitionSettingsUseCase`
- `data/`
  - Repositories: `SpeechToTextRepositoryImpl`, `TextToSpeechRepositoryImpl`, `TextRecognitionRepositoryImpl`
  - Bridge platform providers to domain (e.g., ML Kit / Vision handling)
- `core/platform/`
  - expect/actual accessors to platform providers: `getTTSProvider()`, `getTextRecognitionProvider()`
- `presentation/`
  - ViewModels use only use cases (no direct repository calls)
  - Compose screens in `commonMain` drive the features and permissions
- DI: `core/platform/di/PlatformModule.kt` wires repositories, use cases, and view models via Koin

## Platform Implementations
- Android
  - STT: `SpeechRecognizer` with `ActivityResultContracts.RequestPermission` for `RECORD_AUDIO`
  - TTS: platform TTS provider (`AndroidTTSProvider`)
  - OCR: ML Kit `TextRecognition` via `ActivityResultContracts.GetContent` for gallery images
  - Android Manifest includes ML Kit OCR meta‑data and permissions
- iOS
  - STT: `AVAudioSession` + `SFSpeechRecognizer` permission handling
  - TTS: `AVSpeechSynthesizer` with word boundary and start/finish callbacks
  - OCR: `PHPickerViewController` + Vision `VNRecognizeTextRequest`
  - Info.plist includes usage descriptions for Speech Recognition, Microphone, Photo Library, and Camera

## Permissions
- Android Manifest
  - `android.permission.RECORD_AUDIO`
  - `android.permission.INTERNET`
  - ML Kit OCR meta‑data: `com.google.mlkit.vision.DEPENDENCIES = ocr`
- iOS Info.plist
  - `NSSpeechRecognitionUsageDescription`
  - `NSMicrophoneUsageDescription`
  - `NSPhotoLibraryUsageDescription`
  - `NSCameraUsageDescription`

## Getting Started

### Prerequisites
- Android: Android Studio (Giraffe or newer), JDK 17, Android SDK
- iOS: Xcode 14+, macOS, Swift toolchain

### Clone
```
git clone https://github.com/your-org/RealTimeVoiceTranslate_KMP.git
cd RealTimeVoiceTranslate_KMP
```

### Android Run
- Open the project in Android Studio
- Select an emulator or device
- Run `composeApp` configuration
- Or from CLI: `./gradlew :composeApp:assembleDebug`

### iOS Run
- Open `iosApp` in Xcode
- Build and run the app target on a simulator or device
- The Swift side injects platform providers:
  - `TTSManagerIOS.shared` (AVSpeechSynthesizer)
  - `TextRecognitionManagerIOS.shared` (PHPicker + Vision)

## Usage
- Landing Screen: choose Speech to Text, Text to Speech, or Text Recognition
- Speech to Text: tap the mic to request permission and start/stop transcription; select language
- Text to Speech: enter text and tap Play; pause/resume/stop; watch word highlighting
- Text Recognition: tap ‘Pick Image’ to select an image and extract text

## Development Guidelines
- ViewModels should depend on use cases (not repositories/providers directly)
- Add platform functionality behind `expect/actual` or dedicated providers; expose access via `getXProvider()` in `core/platform`
- Wire new use cases and repositories in Koin (`PlatformModule.kt`)
- Keep UI logic in `presentation` and business logic in `domain`
- Follow existing naming and patterns; avoid introducing secrets in code

## Module Layout
- `composeApp/src/commonMain`: shared UI, view models, domain and data abstractions
- `composeApp/src/androidMain`: Android implementations (STT, TTS, ML Kit OCR)
- `composeApp/src/iosMain`: iOS implementations (TTS bridge, provider setters)
- `iosApp/iosApp`: Swift glue code for iOS providers and app bootstrapping

## Contribution
- Fork and create a feature branch
- Align with architecture (use cases → repositories → providers)
- Include platform integration changes in respective `androidMain`/`iosMain`
- Run on both platforms when possible
- Open a PR with a clear description and testing notes

## License
Licensed under the Apache License, Version 2.0. See `LICENSE` for details.

