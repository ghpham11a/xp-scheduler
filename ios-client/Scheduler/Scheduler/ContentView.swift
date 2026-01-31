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
    @State private var viewModel = SchedulerViewModel()
    @State private var selectedTab: AppTab = .calendar

    var body: some View {
        VStack(spacing: 0) {
            if viewModel.isLoading {
                loadingView
            } else if let error = viewModel.error {
                errorView(error)
            } else {
                TabView(selection: $selectedTab) {
                    Tab(AppTab.calendar.rawValue, systemImage: AppTab.calendar.icon, value: .calendar) {
                        CalendarView(
                            currentUserId: viewModel.currentUserId,
                            users: viewModel.users,
                            availabilities: viewModel.availabilities,
                            meetings: viewModel.meetings,
                            use24HourTime: viewModel.use24HourTime,
                            onCancelMeeting: { viewModel.cancelMeeting($0) },
                            userById: { viewModel.userById($0) }
                        )
                    }

                    Tab(AppTab.availability.rawValue, systemImage: AppTab.availability.icon, value: .availability) {
                        AvailabilityView(
                            currentUser: viewModel.currentUser,
                            availabilitySlots: viewModel.currentUserAvailability?.slots ?? [],
                            use24HourTime: viewModel.use24HourTime,
                            onUpdateAvailability: { slots in
                                viewModel.setAvailability(userId: viewModel.currentUserId, slots: slots)
                            }
                        )
                    }

                    Tab(AppTab.schedule.rawValue, systemImage: AppTab.schedule.icon, value: .schedule) {
                        ScheduleView(
                            currentUserId: viewModel.currentUserId,
                            users: viewModel.users,
                            availabilities: viewModel.availabilities,
                            meetings: viewModel.meetings,
                            use24HourTime: viewModel.use24HourTime,
                            onScheduleMeeting: { org, part, date, start, end, title in
                                viewModel.addMeeting(
                                    organizerId: org,
                                    participantId: part,
                                    date: date,
                                    startHour: start,
                                    endHour: end,
                                    title: title
                                )
                            },
                            onCancelMeeting: { viewModel.cancelMeeting($0) },
                            userById: { viewModel.userById($0) }
                        )
                    }

                    Tab(AppTab.settings.rawValue, systemImage: AppTab.settings.icon, value: .settings) {
                        SettingsView(
                            currentUser: viewModel.currentUser,
                            users: viewModel.users,
                            use24HourTime: Binding(
                                get: { viewModel.use24HourTime },
                                set: { viewModel.use24HourTime = $0 }
                            ),
                            onUserSelected: { viewModel.setCurrentUser($0) }
                        )
                    }
                }
            }
        }
        .alert("Error", isPresented: .init(
            get: { viewModel.error != nil },
            set: { if !$0 { viewModel.clearError() } }
        )) {
            Button("OK") { viewModel.clearError() }
        } message: {
            Text(viewModel.error ?? "")
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
                Task { await viewModel.fetchData() }
            }
            .buttonStyle(.borderedProminent)
            Spacer()
        }
        .padding()
    }
}
