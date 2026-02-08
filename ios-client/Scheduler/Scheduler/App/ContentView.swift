import SwiftUI

enum AppTab: String, CaseIterable {
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

struct ContentView: View {
    let container: DependencyContainer

    @State private var sharedState: SharedState
    @State private var calendarViewModel: CalendarView.ViewModel
    @State private var availabilityViewModel: AvailabilityView.ViewModel
    @State private var scheduleViewModel: ScheduleView.ViewModel
    @State private var settingsViewModel: SettingsView.ViewModel
    @State private var selectedTab: AppTab = .calendar

    init(container: DependencyContainer) {
        self.container = container
        _sharedState = State(initialValue: container.resolve(SharedState.self))
        _calendarViewModel = State(initialValue: container.resolve(CalendarView.ViewModel.self))
        _availabilityViewModel = State(initialValue: container.resolve(AvailabilityView.ViewModel.self))
        _scheduleViewModel = State(initialValue: container.resolve(ScheduleView.ViewModel.self))
        _settingsViewModel = State(initialValue: container.resolve(SettingsView.ViewModel.self))
    }

    var body: some View {
        VStack(spacing: 0) {
            if sharedState.isLoading {
                loadingView
            } else if let error = sharedState.error {
                errorView(error)
            } else {
                TabView(selection: $selectedTab) {
                    Tab(AppTab.calendar.rawValue, systemImage: AppTab.calendar.icon, value: .calendar) {
                        CalendarView(viewModel: calendarViewModel)
                    }

                    Tab(AppTab.availability.rawValue, systemImage: AppTab.availability.icon, value: .availability) {
                        AvailabilityView(viewModel: availabilityViewModel)
                    }

                    Tab(AppTab.schedule.rawValue, systemImage: AppTab.schedule.icon, value: .schedule) {
                        ScheduleView(viewModel: scheduleViewModel)
                    }

                    Tab(AppTab.settings.rawValue, systemImage: AppTab.settings.icon, value: .settings) {
                        SettingsView(viewModel: settingsViewModel)
                    }
                }
            }
        }
        .alert("Error", isPresented: .init(
            get: { sharedState.error != nil },
            set: { if !$0 { sharedState.clearError() } }
        )) {
            Button("OK") { sharedState.clearError() }
        } message: {
            Text(sharedState.error ?? "")
        }
    }

    private var loadingView: some View {
        VStack(spacing: 16) {
            Spacer()
            ProgressView()
            Text("Loading...")
                .foregroundStyle(.secondary)
            Spacer()
        }
    }

    private func errorView(_ error: String) -> some View {
        VStack(spacing: 16) {
            Spacer()
            Image(systemName: "exclamationmark.triangle")
                .font(.system(size: 48))
                .foregroundStyle(.red)
            Text("Something went wrong")
                .font(.title3.weight(.medium))
            Text(error)
                .font(.caption)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
            Button("Retry") {
                Task { await sharedState.fetchData() }
            }
            .buttonStyle(.borderedProminent)
            Spacer()
        }
        .padding()
    }
}
