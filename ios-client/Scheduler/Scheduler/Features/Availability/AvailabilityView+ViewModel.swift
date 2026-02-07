import Foundation
import Observation

extension AvailabilityView {
    @Observable
    class ViewModel {
        let sharedState: SharedState
        private let repository: SchedulerRepositoryProtocol

        init(sharedState: SharedState, repository: SchedulerRepositoryProtocol) {
            self.sharedState = sharedState
            self.repository = repository
        }

        var currentUser: User? { sharedState.currentUser }
        var availabilitySlots: [TimeSlot] { sharedState.currentUserAvailability?.slots ?? [] }
        var use24HourTime: Bool { sharedState.use24HourTime }

        func setAvailability(userId: String, slots: [TimeSlot]) {
            let newAvailability = Availability(userId: userId, slots: slots)
            if let index = sharedState.availabilities.firstIndex(where: { $0.userId == userId }) {
                sharedState.availabilities[index] = newAvailability
            } else {
                sharedState.availabilities.append(newAvailability)
            }

            Task {
                do {
                    _ = try await repository.updateAvailability(userId: userId, slots: slots)
                } catch {
                    await sharedState.fetchData()
                }
            }
        }
    }
}
