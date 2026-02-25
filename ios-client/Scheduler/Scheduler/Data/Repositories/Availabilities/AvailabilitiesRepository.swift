import Foundation

final class AvailabilitiesRepository: AvailabilitiesRepo, @unchecked Sendable {
    private let networking: Networking

    init(networking: Networking) {
        self.networking = networking
    }

    func getAvailabilities() async throws -> [Availability] {
        try await networking.makeRequest(endpoint: AvailabilitiesEndpoints.GetAvailabilities())
    }

    func updateAvailability(userId: String, slots: [TimeSlot]) async throws -> Availability {
        let endpoint = try AvailabilitiesEndpoints.UpdateAvailability(userId: userId, slots: slots)
        return try await networking.makeRequest(endpoint: endpoint)
    }
}
