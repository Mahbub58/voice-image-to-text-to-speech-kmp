import SwiftUI

struct ContentView: View {
    @State private var textToSpeak = "Hello, this is a TTS test"
    @State private var isSpeaking = false
    
    var body: some View {
        VStack(spacing: 20) {
            Text("TTS Test App")
                .font(.largeTitle)
                .padding()
            
            TextField("Enter text to speak", text: $textToSpeak)
                .textFieldStyle(RoundedBorderTextFieldStyle())
                .padding()
            
            HStack(spacing: 20) {
                Button(action: {
                    TTSManagerIOS.shared.speak(text: textToSpeak)
                    isSpeaking = true
                }) {
                    Text("Speak")
                        .padding()
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(10)
                }
                
                Button(action: {
                    TTSManagerIOS.shared.stop()
                    isSpeaking = false
                }) {
                    Text("Stop")
                        .padding()
                        .background(Color.red)
                        .foregroundColor(.white)
                        .cornerRadius(10)
                }
            }
            
            Button(action: {
                TTSManagerIOS.shared.verifyTTSChain()
            }) {
                Text("Verify TTS Chain")
                    .padding()
                    .background(Color.green)
                    .foregroundColor(.white)
                    .cornerRadius(10)
            }
            
            if isSpeaking {
                Text("Speaking...")
                    .foregroundColor(.green)
            }
            
            Spacer()
        }
        .padding()
        .onAppear {
            TTSManagerIOS.shared.initialize()
        }
    }
}



