import Foundation

protocol MeetingsRepo: Sendable {
    func getMeetings() async throws -> [Meeting]
    func createMeeting(_ request: CreateMeetingRequest) async throws -> Meeting
    func deleteMeeting(id: String) async throws -> DeleteMeetingResponse
}
