import Foundation
import Observation

extension SettingsView {
    @Observable
    class ViewModel {
        let sharedState: SharedState

        init(sharedState: SharedState) {
            self.sharedState = sharedState
        }

        var currentUser: User? { sharedState.currentUser }
        var users: [User] { sharedState.users }

        var use24HourTime: Bool {
            get { sharedState.use24HourTime }
            set { sharedState.use24HourTime = newValue }
        }

        func setCurrentUser(_ userId: String) {
            sharedState.currentUserId = userId
        }
    }
}
