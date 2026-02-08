import Foundation

protocol SchedulerRepositoryProtocol {
    func getUsers() async throws -> [User]
    func getAvailabilities() async throws -> [Availability]
    func updateAvailability(userId: String, slots: [TimeSlot]) async throws -> Availability
    func getMeetings() async throws -> [Meeting]
    func createMeeting(_ request: CreateMeetingRequest) async throws -> Meeting
    func deleteMeeting(id: String) async throws -> DeleteMeetingResponse
}

class SchedulerRepository: SchedulerRepositoryProtocol {
    func getUsers() async throws -> [User] {
        try await APIService.getUsers()
    }

    func getAvailabilities() async throws -> [Availability] {
        try await APIService.getAvailabilities()
    }

    func updateAvailability(userId: String, slots: [TimeSlot]) async throws -> Availability {
        try await APIService.updateAvailability(userId: userId, slots: slots)
    }

    func getMeetings() async throws -> [Meeting] {
        try await APIService.getMeetings()
    }

    func createMeeting(_ request: CreateMeetingRequest) async throws -> Meeting {
        try await APIService.createMeeting(request)
    }

    func deleteMeeting(id: String) async throws -> DeleteMeetingResponse {
        try await APIService.deleteMeeting(id: id)
    }
}
