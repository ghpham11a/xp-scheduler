import Foundation
import Observation

extension ScheduleView {
    @Observable
    class ViewModel {
        let sharedState: SharedState
        private let repository: SchedulerRepositoryProtocol

        init(sharedState: SharedState, repository: SchedulerRepositoryProtocol) {
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

        func addMeeting(organizerId: String, participantId: String, date: String, startHour: Double, endHour: Double, title: String) {
            Task {
                do {
                    let request = CreateMeetingRequest(
                        organizerId: organizerId,
                        participantId: participantId,
                        date: date,
                        startHour: startHour,
                        endHour: endHour,
                        title: title
                    )
                    let meeting = try await repository.createMeeting(request)
                    sharedState.meetings.append(meeting)
                } catch {
                    sharedState.error = error.localizedDescription
                }
            }
        }

        func cancelMeeting(_ meetingId: String) {
            sharedState.meetings.removeAll { $0.id == meetingId }

            Task {
                do {
                    _ = try await repository.deleteMeeting(id: meetingId)
                } catch {
                    await sharedState.fetchData()
                }
            }
        }
    }
}
