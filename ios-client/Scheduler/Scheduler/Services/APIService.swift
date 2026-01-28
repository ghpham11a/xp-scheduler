import Foundation

struct APIService: Sendable {
    static let baseURL = "http://localhost:6969"

    private static let decoder: JSONDecoder = {
        let d = JSONDecoder()
        d.keyDecodingStrategy = .useDefaultKeys
        return d
    }()

    private static let encoder: JSONEncoder = {
        let e = JSONEncoder()
        e.keyEncodingStrategy = .useDefaultKeys
        return e
    }()

    // MARK: - Users

    static func getUsers() async throws -> [User] {
        let data = try await fetch("\(baseURL)/users")
        return try decoder.decode([User].self, from: data)
    }

    // MARK: - Availabilities

    static func getAvailabilities() async throws -> [Availability] {
        let data = try await fetch("\(baseURL)/availabilities")
        return try decoder.decode([Availability].self, from: data)
    }

    static func updateAvailability(userId: String, slots: [TimeSlot]) async throws -> Availability {
        let body = try encoder.encode(slots)
        let data = try await fetch("\(baseURL)/availabilities/\(userId)", method: "PUT", body: body)
        return try decoder.decode(Availability.self, from: data)
    }

    // MARK: - Meetings

    static func getMeetings() async throws -> [Meeting] {
        let data = try await fetch("\(baseURL)/meetings")
        return try decoder.decode([Meeting].self, from: data)
    }

    static func createMeeting(_ request: CreateMeetingRequest) async throws -> Meeting {
        let body = try encoder.encode(request)
        let data = try await fetch("\(baseURL)/meetings", method: "POST", body: body)
        return try decoder.decode(Meeting.self, from: data)
    }

    static func deleteMeeting(id: String) async throws -> DeleteMeetingResponse {
        let data = try await fetch("\(baseURL)/meetings/\(id)", method: "DELETE")
        return try decoder.decode(DeleteMeetingResponse.self, from: data)
    }

    // MARK: - Private

    private static func fetch(_ urlString: String, method: String = "GET", body: Data? = nil) async throws -> Data {
        guard let url = URL(string: urlString) else {
            throw URLError(.badURL)
        }
        var request = URLRequest(url: url)
        request.httpMethod = method
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.timeoutInterval = 30
        if let body {
            request.httpBody = body
        }

        let (data, response) = try await URLSession.shared.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse,
              (200...299).contains(httpResponse.statusCode) else {
            let statusCode = (response as? HTTPURLResponse)?.statusCode ?? 0
            throw URLError(.badServerResponse, userInfo: [
                NSLocalizedDescriptionKey: "API error: \(statusCode)"
            ])
        }

        return data
    }
}
