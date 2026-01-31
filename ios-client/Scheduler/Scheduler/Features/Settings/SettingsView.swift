import SwiftUI

struct SettingsView: View {
    let currentUser: User?
    let users: [User]
    @Binding var use24HourTime: Bool
    let onUserSelected: (String) -> Void

    var body: some View {
        NavigationStack {
            List {
                Section("Account") {
                    Menu {
                        ForEach(users) { user in
                            Button {
                                onUserSelected(user.id)
                            } label: {
                                Label {
                                    VStack(alignment: .leading) {
                                        Text(user.name)
                                        Text(user.email)
                                            .font(.caption)
                                            .foregroundStyle(.secondary)
                                    }
                                } icon: {
                                    if user.id == currentUser?.id {
                                        Image(systemName: "checkmark")
                                    }
                                }
                            }
                        }
                    } label: {
                        HStack {
                            if let currentUser {
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
                    Toggle("24-Hour Time", isOn: $use24HourTime)
                }
            }
            .navigationTitle("Settings")
        }
    }
}
