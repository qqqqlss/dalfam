import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        // Koin은 앱이 켜질 때 한 번만 시작한다.
        KoinIosKt.startKoinForIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
