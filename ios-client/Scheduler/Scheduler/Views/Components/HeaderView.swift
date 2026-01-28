import SwiftUI

struct HeaderView: View {
    let currentUser: User?
    let users: [User]
    let onUserSelected: (String) -> Void

    var body: some View {
        HStack {
            HStack(spacing: 8) {
                RoundedRectangle(cornerRadius: 8)
                    .fill(.blue)
                    .frame(width: 32, height: 32)
                    .overlay {
                        Text("S")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundStyle(.white)
                    }
                Text("XP Scheduler")
                    .font(.headline)
            }

            Spacer()

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
                HStack(spacing: 8) {
                    if let currentUser {
                        UserAvatar(user: currentUser, size: 28)
                        Text(currentUser.name)
                            .font(.subheadline)
                    }
                    Image(systemName: "chevron.down")
                        .font(.caption)
                }
                .padding(.horizontal, 10)
                .padding(.vertical, 6)
                .background(.quaternary, in: RoundedRectangle(cornerRadius: 8))
            }
            .buttonStyle(.plain)
        }
        .padding(.horizontal)
        .padding(.vertical, 8)
    }
}
