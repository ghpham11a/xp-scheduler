import Foundation
import OSLog

final class NetworkService: Networking, @unchecked Sendable {
    private let session: URLSession
    private let decoder: JSONDecoder
    private let maxRetries = 3

    init(session: URLSession = .shared) {
        self.session = session
        self.decoder = JSONDecoder()
        self.decoder.keyDecodingStrategy = .useDefaultKeys
    }

    func makeRequest<T: Decodable>(endpoint: Endpoint) async throws -> T {
        guard let url = endpoint.url else {
            throw NetworkError.invalidURL
        }

        var request = URLRequest(url: url)
        request.httpMethod = endpoint.method.rawValue
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.timeoutInterval = 30

        if let headers = endpoint.headers {
            for (key, value) in headers {
                request.setValue(value, forHTTPHeaderField: key)
            }
        }

        if let body = endpoint.body {
            request.httpBody = body
        }

        let data: Data
        if endpoint.method == .get {
            data = try await performWithRetry(request: request)
        } else {
            data = try await perform(request: request)
        }

        do {
            return try decoder.decode(T.self, from: data)
        } catch {
            Log.networking.error("Decoding failed: \(error.localizedDescription)")
            throw NetworkError.decodingError(error)
        }
    }

    // MARK: - Private

    private func perform(request: URLRequest) async throws -> Data {
        Log.networking.debug("\(request.httpMethod ?? "?") \(request.url?.absoluteString ?? "")")

        let (data, response) = try await session.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse else {
            throw NetworkError.invalidResponse
        }

        guard (200...299).contains(httpResponse.statusCode) else {
            Log.networking.error("HTTP \(httpResponse.statusCode) for \(request.url?.absoluteString ?? "")")
            throw NetworkError.httpError(statusCode: httpResponse.statusCode, data: data)
        }

        return data
    }

    private func performWithRetry(request: URLRequest, attempt: Int = 1) async throws -> Data {
        do {
            return try await perform(request: request)
        } catch {
            if attempt < maxRetries, isRetryable(error) {
                let delay = pow(2.0, Double(attempt - 1))
                Log.networking.warning("Retry \(attempt)/\(self.maxRetries) after \(delay)s for \(request.url?.absoluteString ?? "")")
                try await Task.sleep(for: .seconds(delay))
                return try await performWithRetry(request: request, attempt: attempt + 1)
            }
            throw error
        }
    }

    private func isRetryable(_ error: Error) -> Bool {
        if let networkError = error as? NetworkError {
            if case .httpError(let code, _) = networkError {
                return code >= 500
            }
        }
        return (error as? URLError) != nil
    }
}
