import UIKit
import PhotosUI
import Vision
import UniformTypeIdentifiers
import ComposeApp

class TextRecognitionManagerIOS: ComposeApp.TextRecognitionProvider {
    static let shared = TextRecognitionManagerIOS()
    private var currentPickerDelegate: PickerDelegate?

    func requestPermission(onPermissionResult: @escaping (ComposeApp.PermissionRequestStatus) -> Void) {
        PHPhotoLibrary.requestAuthorization { status in
            switch status {
            case .authorized, .limited:
                onPermissionResult(.allowed)
            case .denied, .restricted:
                onPermissionResult(.notAllowed)
            case .notDetermined:
                onPermissionResult(.notAllowed)
            @unknown default:
                onPermissionResult(.notAllowed)
            }
        }
    }

    @MainActor func pickImage(onResult: @escaping (String) -> Void, onError: @escaping (ComposeApp.KotlinThrowable) -> Void) {
        func topViewController() -> UIViewController? {
            if let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
               let window = scene.windows.first(where: { $0.isKeyWindow }),
               var root = window.rootViewController {
                while let presented = root.presentedViewController { root = presented }
                return root
            }
            return nil
        }
        guard let root = topViewController() else {
            onError(ComposeApp.KotlinThrowable(message: "No rootViewController"))
            return
        }
        var configuration = PHPickerConfiguration()
        configuration.filter = .images
        configuration.selectionLimit = 1
        let picker = PHPickerViewController(configuration: configuration)
        let delegate = PickerDelegate(onResult: onResult, onError: onError)
        currentPickerDelegate = delegate
        picker.delegate = delegate
        root.present(picker, animated: true)
    }

    func showNeedPermission() {}
    func dismissPermissionDialog() {}
    func openAppSettings() {
        if let url = URL(string: UIApplication.openSettingsURLString) {
            UIApplication.shared.open(url)
        }
    }
}

@MainActor private class PickerDelegate: NSObject, PHPickerViewControllerDelegate {
    let onResult: (String) -> Void
    let onError: (ComposeApp.KotlinThrowable) -> Void

    init(onResult: @escaping (String) -> Void, onError: @escaping (ComposeApp.KotlinThrowable) -> Void) {
        self.onResult = onResult
        self.onError = onError
    }

    func picker(_ picker: PHPickerViewController, didFinishPicking results: [PHPickerResult]) {
        picker.dismiss(animated: true)
        guard let itemProvider = results.first?.itemProvider else { return }
        if itemProvider.canLoadObject(ofClass: UIImage.self) {
            itemProvider.loadObject(ofClass: UIImage.self) { image, error in
                if let error = error {
                    self.onError(ComposeApp.KotlinThrowable(message: error.localizedDescription))
                    return
                }
                guard let uiImage = image as? UIImage, let cgImage = uiImage.cgImage else {
                    self.onError(ComposeApp.KotlinThrowable(message: "Invalid image"))
                    return
                }
                let handler = VNImageRequestHandler(cgImage: cgImage, options: [:])
                let request = VNRecognizeTextRequest { request, error in
                    if let error = error {
                        self.onError(ComposeApp.KotlinThrowable(message: error.localizedDescription))
                        return
                    }
                    let texts = (request.results as? [VNRecognizedTextObservation])?.compactMap { $0.topCandidates(1).first?.string }
                    let joined = texts?.joined(separator: "\n") ?? ""
                    self.onResult(joined)
                }
                request.recognitionLevel = .accurate
                do {
                    try handler.perform([request])
                } catch {
                    self.onError(ComposeApp.KotlinThrowable(message: error.localizedDescription))
                }
            }
        } else if itemProvider.hasItemConformingToTypeIdentifier(UTType.image.identifier) {
            itemProvider.loadDataRepresentation(forTypeIdentifier: UTType.image.identifier) { data, error in
                if let error = error {
                    self.onError(ComposeApp.KotlinThrowable(message: error.localizedDescription))
                    return
                }
                guard let data = data, let uiImage = UIImage(data: data), let cgImage = uiImage.cgImage else {
                    self.onError(ComposeApp.KotlinThrowable(message: "Invalid image"))
                    return
                }
                let handler = VNImageRequestHandler(cgImage: cgImage, options: [:])
                let request = VNRecognizeTextRequest { request, error in
                    if let error = error {
                        self.onError(ComposeApp.KotlinThrowable(message: error.localizedDescription))
                        return
                    }
                    let texts = (request.results as? [VNRecognizedTextObservation])?.compactMap { $0.topCandidates(1).first?.string }
                    let joined = texts?.joined(separator: "\n") ?? ""
                    self.onResult(joined)
                }
                request.recognitionLevel = .accurate
                do {
                    try handler.perform([request])
                } catch {
                    self.onError(ComposeApp.KotlinThrowable(message: error.localizedDescription))
                }
            }
        }
    }
}
