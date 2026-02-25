import Foundation

final class MeetingsRepository: MeetingsRepo, @unchecked Sendable {
    private let networking: Networking

    init(networking: Networking) {
        self.networking = networking
    }

    func getMeetings() async throws -> [Meeting] {
        try await networking.makeRequest(endpoint: MeetingsEndpoints.GetMeetings())
    }

    func createMeeting(_ request: CreateMeetingRequest) async throws -> Meeting {
        let endpoint = try MeetingsEndpoints.CreateMeeting(request: request)
        return try await networking.makeRequest(endpoint: endpoint)
    }

    func deleteMeeting(id: String) async throws -> DeleteMeetingResponse {
        try await networking.makeRequest(endpoint: MeetingsEndpoints.DeleteMeeting(id: id))
    }
}
