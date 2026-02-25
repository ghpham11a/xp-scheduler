import SwiftUI
import Observation

enum Tab: String, CaseIterable {
    case calendar = "Calendar"
    case availability = "Availability"
    case schedule = "Schedule"
    case settings = "Settings"

    var icon: String {
        switch self {
        case .calendar: "calendar"
        case .availability: "calendar.badge.clock"
        case .schedule: "plus.circle"
        case .settings: "gearshape"
        }
    }
}

@Observable
final class RouteManager {
    var selectedTab: Tab = .calendar

    var calendarPath = NavigationPath()
    var availabilityPath = NavigationPath()
    var schedulePath = NavigationPath()
    var settingsPath = NavigationPath()

    var pendingDeepLink: DeepLink?

    func path(for tab: Tab) -> Binding<NavigationPath> {
        switch tab {
        case .calendar:
            return Binding(get: { self.calendarPath }, set: { self.calendarPath = $0 })
        case .availability:
            return Binding(get: { self.availabilityPath }, set: { self.availabilityPath = $0 })
        case .schedule:
            return Binding(get: { self.schedulePath }, set: { self.schedulePath = $0 })
        case .settings:
            return Binding(get: { self.settingsPath }, set: { self.settingsPath = $0 })
        }
    }

    func handleDeepLink(_ url: URL) {
        guard url.scheme == "xpscheduler" else { return }
        let pathComponents = url.pathComponents.filter { $0 != "/" }
        let host = url.host(percentEncoded: false) ?? ""

        switch host {
        case "calendar":
            selectedTab = .calendar
            if pathComponents.count >= 2, pathComponents[0] == "meeting" {
                pendingDeepLink = .calendarMeeting(meetingId: pathComponents[1])
            } else {
                pendingDeepLink = .calendar
            }

        case "availability":
            selectedTab = .availability
            if let dateStr = pathComponents.first, !dateStr.isEmpty {
                pendingDeepLink = .availabilityDate(date: dateStr)
            } else {
                pendingDeepLink = .availability
            }

        case "schedule":
            selectedTab = .schedule
            if pathComponents.count >= 2, pathComponents[0] == "with" {
                pendingDeepLink = .scheduleWith(userId: pathComponents[1])
            } else {
                pendingDeepLink = .schedule
            }

        case "settings":
            selectedTab = .settings
            pendingDeepLink = .settings

        default:
            break
        }
    }
}

enum DeepLink: Equatable {
    case calendar
    case calendarMeeting(meetingId: String)
    case availability
    case availabilityDate(date: String)
    case schedule
    case scheduleWith(userId: String)
    case settings
}
