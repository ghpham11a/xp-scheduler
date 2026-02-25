import Foundation

enum UsersEndpoints {
    struct GetUsers: Endpoint {
        let path = "/users"
        let method = HTTPMethod.get
    }
}
