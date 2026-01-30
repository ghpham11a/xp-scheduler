import Foundation
import Observation

@Observable
final class SchedulerViewModel {
    var currentUserId: String = "" {
        didSet { UserDefaults.standard.set(currentUserId, forKey: "currentUserId") }
    }
    var users: [User] = []
    var availabilities: [Availability] = []
    var meetings: [Meeting] = []
    var isLoading = true
    var error: String?

    init() {
        if let saved = UserDefaults.standard.string(forKey: "currentUserId") {
            currentUserId = saved
        }
        Task { await fetchData() }
    }

    func fetchData() async {
        isLoading = true
        error = nil
        do {
            async let usersResult = APIService.getUsers()
            async let availResult = APIService.getAvailabilities()
            async let meetingsResult = APIService.getMeetings()

            let (u, a, m) = try await (usersResult, availResult, meetingsResult)
            users = u
            availabilities = a
            meetings = m

            if currentUserId.isEmpty, let first = users.first {
                currentUserId = first.id
            }
            isLoading = false
        } catch {
            self.error = error.localizedDescription
            isLoading = false
        }
    }

    func setCurrentUser(_ userId: String) {
        currentUserId = userId
    }

    func setAvailability(userId: String, slots: [TimeSlot]) {
        let newAvailability = Availability(userId: userId, slots: slots)
        if let index = availabilities.firstIndex(where: { $0.userId == userId }) {
            availabilities[index] = newAvailability
        } else {
            availabilities.append(newAvailability)
        }

        Task {
            do {
                _ = try await APIService.updateAvailability(userId: userId, slots: slots)
            } catch {
                await fetchData()
            }
        }
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
                let meeting = try await APIService.createMeeting(request)
                meetings.append(meeting)
            } catch {
                self.error = error.localizedDescription
            }
        }
    }

    func cancelMeeting(_ meetingId: String) {
        meetings.removeAll { $0.id == meetingId }

        Task {
            do {
                _ = try await APIService.deleteMeeting(id: meetingId)
            } catch {
                await fetchData()
            }
        }
    }

    func clearError() {
        error = nil
    }

    // MARK: - Computed Helpers

    var currentUser: User? {
        users.first { $0.id == currentUserId }
    }

    var currentUserAvailability: Availability? {
        availabilities.first { $0.userId == currentUserId }
    }

    var currentUserMeetings: [Meeting] {
        getMeetingsForUser(meetings, userId: currentUserId)
    }

    func userById(_ id: String) -> User? {
        users.first { $0.id == id }
    }

    func availabilityForUser(_ userId: String) -> Availability? {
        availabilities.first { $0.userId == userId }
    }
}
