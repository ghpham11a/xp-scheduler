import Foundation

protocol UsersRepo: Sendable {
    func getUsers() async throws -> [User]
}
