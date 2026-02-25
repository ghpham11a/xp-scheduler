import Foundation

final class UsersRepository: UsersRepo, @unchecked Sendable {
    private let networking: Networking

    init(networking: Networking) {
        self.networking = networking
    }

    func getUsers() async throws -> [User] {
        try await networking.makeRequest(endpoint: UsersEndpoints.GetUsers())
    }
}
