import SwiftUI

struct SettingsView: View {

    @State private var viewModel: SettingsViewModel

    init(viewModel: SettingsViewModel) {
        _viewModel = State(initialValue: viewModel)
    }

    var body: some View {
        NavigationStack {
            List {
                AccountSection(
                    users: viewModel.users,
                    currentUser: viewModel.currentUser,
                    onSelectUser: { viewModel.setCurrentUser($0) }
                )

                CalendarSection(
                    use24HourTime: Binding(
                        get: { viewModel.use24HourTime },
                        set: { viewModel.use24HourTime = $0 }
                    )
                )
            }
            .navigationTitle("Settings")
        }
    }
}
