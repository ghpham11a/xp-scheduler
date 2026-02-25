import SwiftUI

extension SettingsView {
    struct AccountSection: View {
        let users: [User]
        let currentUser: User?
        let onSelectUser: (String) -> Void

        var body: some View {
            Section("Account") {
                Menu {
                    ForEach(users) { user in
                        Button {
                            onSelectUser(user.id)
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
                .accessibilityLabel("Current user: \(currentUser?.name ?? "None")")
                .accessibilityHint("Double tap to switch user")
            }
        }
    }
}
