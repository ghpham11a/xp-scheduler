import Foundation

enum MeetingsEndpoints {
    struct GetMeetings: Endpoint {
        let path = "/meetings"
        let method = HTTPMethod.get
    }

    struct CreateMeeting: Endpoint {
        let path = "/meetings"
        let method = HTTPMethod.post
        let body: Data?

        init(request: CreateMeetingRequest) throws {
            self.body = try JSONEncoder().encode(request)
        }
    }

    struct DeleteMeeting: Endpoint {
        let path: String
        let method = HTTPMethod.delete

        init(id: String) {
            self.path = "/meetings/\(id)"
        }
    }
}
