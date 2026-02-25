import SwiftUI

struct ContentView: View {
    @Environment(RouteManager.self) private var routeManager
    @Environment(ConnectivityMonitor.self) private var connectivity
    @Environment(SharedState.self) private var sharedState
    @ScaledMetric(relativeTo: .title) private var errorIconSize: CGFloat = 48

    var body: some View {
        @Bindable var routeManager = routeManager

        VStack(spacing: 0) {

            if !connectivity.isConnected {
                connectivityBanner
            }

            if sharedState.isLoading {
                loadingView
            } else if let error = sharedState.error {
                errorView(error)
            } else {
                TabView(selection: $routeManager.selectedTab) {
                    SwiftUI.Tab(Tab.calendar.rawValue, systemImage: Tab.calendar.icon, value: .calendar) {
                        CalendarView(viewModel: DependencyContainer.shared.resolve(CalendarViewModel.self))
                    }

                    SwiftUI.Tab(Tab.availability.rawValue, systemImage: Tab.availability.icon, value: .availability) {
                        AvailabilityView(viewModel: DependencyContainer.shared.resolve(AvailabilityViewModel.self))
                    }

                    SwiftUI.Tab(Tab.schedule.rawValue, systemImage: Tab.schedule.icon, value: .schedule) {
                        ScheduleView(viewModel: DependencyContainer.shared.resolve(ScheduleViewModel.self))
                    }

                    SwiftUI.Tab(Tab.settings.rawValue, systemImage: Tab.settings.icon, value: .settings) {
                        SettingsView(viewModel: DependencyContainer.shared.resolve(SettingsViewModel.self))
                    }
                }
            }
        }
        .overlay(alignment: .bottom) {
            if let mutationError = sharedState.mutationError {
                mutationErrorToast(mutationError)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                    .onAppear {
                        Task {
                            try? await Task.sleep(for: .seconds(4))
                            withAnimation { sharedState.clearMutationError() }
                        }
                    }
            }
        }
        .animation(.easeInOut(duration: 0.3), value: sharedState.mutationError)
    }

    private func mutationErrorToast(_ message: String) -> some View {
        HStack(spacing: 10) {
            Image(systemName: "exclamationmark.triangle.fill")
                .foregroundStyle(.white)
            Text(message)
                .font(.subheadline.weight(.medium))
                .foregroundStyle(.white)
                .lineLimit(2)
            Spacer()
            Button {
                withAnimation { sharedState.clearMutationError() }
            } label: {
                Image(systemName: "xmark")
                    .font(.caption.bold())
                    .foregroundStyle(.white.opacity(0.8))
            }
        }
        .padding()
        .background(.red.gradient, in: RoundedRectangle(cornerRadius: 12))
        .padding(.horizontal)
        .padding(.bottom, 100)
        .accessibilityElement(children: .combine)
        .accessibilityLabel("Error: \(message)")
        .accessibilityAddTraits(.isStaticText)
    }

    private var connectivityBanner: some View {
        HStack(spacing: 8) {
            Image(systemName: "wifi.slash")
            Text("No Internet Connection")
                .font(.subheadline.weight(.medium))
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 8)
        .background(.red)
        .foregroundStyle(.white)
        .accessibilityElement(children: .combine)
        .accessibilityLabel("No internet connection")
    }

    private var loadingView: some View {
        VStack(spacing: 16) {
            Spacer()
            ProgressView()
            Text("Loading...")
                .foregroundStyle(.secondary)
            Spacer()
        }
        .accessibilityElement(children: .combine)
        .accessibilityLabel("Loading content")
    }

    private func errorView(_ error: String) -> some View {
        VStack(spacing: 16) {
            Spacer()
            Image(systemName: "exclamationmark.triangle")
                .font(.system(size: errorIconSize))
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
