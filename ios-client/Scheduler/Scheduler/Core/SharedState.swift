import Foundation
import Observation

@Observable
final class SharedState {
    var currentUserId: String = "" {
        didSet { UserDefaults.standard.set(currentUserId, forKey: "currentUserId") }
    }
    var use24HourTime: Bool = false {
        didSet { UserDefaults.standard.set(use24HourTime, forKey: "use24HourTime") }
    }
    var users: [User] = []
    var availabilities: [Availability] = []
    var meetings: [Meeting] = []
    var isLoading = true
    var error: String?
    var mutationError: String?

    private let usersRepo: UsersRepo
    private let availabilitiesRepo: AvailabilitiesRepo
    private let meetingsRepo: MeetingsRepo

    init(usersRepo: UsersRepo, availabilitiesRepo: AvailabilitiesRepo, meetingsRepo: MeetingsRepo) {
        self.usersRepo = usersRepo
        self.availabilitiesRepo = availabilitiesRepo
        self.meetingsRepo = meetingsRepo
        if let saved = UserDefaults.standard.string(forKey: "currentUserId") {
            currentUserId = saved
        }
        use24HourTime = UserDefaults.standard.bool(forKey: "use24HourTime")
        Task { await fetchData() }
    }

    func fetchData() async {
        isLoading = true
        error = nil
        do {
            async let usersResult = usersRepo.getUsers()
            async let availResult = availabilitiesRepo.getAvailabilities()
            async let meetingsResult = meetingsRepo.getMeetings()

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

    func clearError() {
        error = nil
    }

    func clearMutationError() {
        mutationError = nil
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
