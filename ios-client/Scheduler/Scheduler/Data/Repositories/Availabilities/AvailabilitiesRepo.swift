import Foundation

protocol AvailabilitiesRepo: Sendable {
    func getAvailabilities() async throws -> [Availability]
    func updateAvailability(userId: String, slots: [TimeSlot]) async throws -> Availability
}
