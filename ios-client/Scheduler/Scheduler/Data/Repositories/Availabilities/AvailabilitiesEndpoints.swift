import Foundation

enum AvailabilitiesEndpoints {
    struct GetAvailabilities: Endpoint {
        let path = "/availabilities"
        let method = HTTPMethod.get
    }

    struct UpdateAvailability: Endpoint {
        let path: String
        let method = HTTPMethod.put
        let body: Data?

        init(userId: String, slots: [TimeSlot]) throws {
            self.path = "/availabilities/\(userId)"
            self.body = try JSONEncoder().encode(slots)
        }
    }
}
