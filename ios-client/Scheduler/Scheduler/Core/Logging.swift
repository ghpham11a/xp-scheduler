import OSLog

enum Log {
    static let networking = Logger(subsystem: Bundle.main.bundleIdentifier ?? "com.scheduler", category: "Networking")
    static let general = Logger(subsystem: Bundle.main.bundleIdentifier ?? "com.scheduler", category: "General")
}
