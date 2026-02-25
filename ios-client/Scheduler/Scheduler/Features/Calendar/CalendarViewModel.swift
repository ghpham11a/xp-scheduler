import Foundation
import Observation

@Observable
class CalendarViewModel {
    let sharedState: SharedState
    private let repository: MeetingsRepo

    init(sharedState: SharedState, repository: MeetingsRepo) {
        self.sharedState = sharedState
        self.repository = repository
    }

    var currentUserId: String { sharedState.currentUserId }
    var users: [User] { sharedState.users }
    var availabilities: [Availability] { sharedState.availabilities }
    var meetings: [Meeting] { sharedState.meetings }
    var use24HourTime: Bool { sharedState.use24HourTime }

    func userById(_ id: String) -> User? {
        sharedState.userById(id)
    }

    func cancelMeeting(_ meetingId: String) {
        sharedState.meetings.removeAll { $0.id == meetingId }

        Task {
            do {
                _ = try await repository.deleteMeeting(id: meetingId)
            } catch {
                sharedState.mutationError = error.localizedDescription
                await sharedState.fetchData()
            }
        }
    }
}
