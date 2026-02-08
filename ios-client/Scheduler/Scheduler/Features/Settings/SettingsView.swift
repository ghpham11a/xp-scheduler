import SwiftUI

struct SettingsView: View {

    @State private var viewModel: ViewModel

    init(viewModel: ViewModel) {
        _viewModel = State(initialValue: viewModel)
    }

    var body: some View {
        NavigationStack {
            List {
                Section("Account") {
                    Menu {
                        ForEach(viewModel.users) { user in
                            Button {
                                viewModel.setCurrentUser(user.id)
                            } label: {
                                Label {
                                    VStack(alignment: .leading) {
                                        Text(user.name)
                                        Text(user.email)
                                            .font(.caption)
                                            .foregroundStyle(.secondary)
                                    }
                                } icon: {
                                    if user.id == viewModel.currentUser?.id {
                                        Image(systemName: "checkmark")
                                    }
                                }
                            }
                        }
                    } label: {
                        HStack {
                            if let currentUser = viewModel.currentUser {
                                UserAvatar(user: currentUser, size: 32)
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(currentUser.name)
                                    Text(currentUser.email)
                                        .font(.caption)
                                        .foregroundStyle(.secondary)
                                }
                            }
                            Spacer()
                            Image(systemName: "chevron.up.chevron.down")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                    }
                    .buttonStyle(.plain)
                }

                Section("Calendar") {
                    Toggle("24-Hour Time", isOn: Binding(
                        get: { viewModel.use24HourTime },
                        set: { viewModel.use24HourTime = $0 }
                    ))
                }
            }
            .navigationTitle("Settings")
        }
    }
}
