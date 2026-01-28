import Foundation

struct User: Codable, Identifiable, Hashable {
    let id: String
    let name: String
    let email: String
    let avatarColor: String
}

struct TimeSlot: Codable, Hashable {
    let date: String      // ISO date (YYYY-MM-DD)
    let startHour: Double  // 0-24, supports 0.5 increments
    let endHour: Double
}

struct Availability: Codable {
    let userId: String
    let slots: [TimeSlot]
}

struct Meeting: Codable, Identifiable, Hashable {
    let id: String
    let organizerId: String
    let participantId: String
    let date: String
    let startHour: Double
    let endHour: Double
    let title: String
}

struct CreateMeetingRequest: Codable {
    let organizerId: String
    let participantId: String
    let date: String
    let startHour: Double
    let endHour: Double
    let title: String
}

struct DeleteMeetingResponse: Codable {
    let status: String
    let id: String
}
