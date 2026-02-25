import Foundation

enum HTTPMethod: String {
    case get = "GET"
    case post = "POST"
    case put = "PUT"
    case delete = "DELETE"
}

enum NetworkError: LocalizedError {
    case invalidURL
    case invalidResponse
    case httpError(statusCode: Int, data: Data)
    case encodingError(Error)
    case decodingError(Error)
    case serverError(String)

    var errorDescription: String? {
        switch self {
        case .invalidURL:
            return "The request could not be completed. Please try again."
        case .invalidResponse:
            return "The server returned an unexpected response. Please try again."
        case .httpError(let statusCode, _):
            switch statusCode {
            case 400:
                return "The request could not be completed. Please check your input."
            case 401, 403:
                return "You don't have permission to perform this action."
            case 404:
                return "The requested item was not found."
            case 409:
                return "There was a conflict. Please refresh and try again."
            case 500...599:
                return "The server is experiencing issues. Please try again later."
            default:
                return "Something went wrong (error \(statusCode)). Please try again."
            }
        case .encodingError:
            return "The request could not be prepared. Please try again."
        case .decodingError:
            return "The server response could not be read. Please try again."
        case .serverError:
            return "The server is experiencing issues. Please try again later."
        }
    }
}

protocol Networking: Sendable {
    func makeRequest<T: Decodable>(endpoint: Endpoint) async throws -> T
}
