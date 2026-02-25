import SwiftUI

extension ScheduleView {
    struct ParticipantSelection: View {
        let users: [User]
        let availabilities: [Availability]
        let onSelect: (User) -> Void

        var body: some View {
            ScrollView {
                VStack(alignment: .leading, spacing: 12) {
                    Text("Schedule a Meeting")
                        .font(.title2.bold())
                    Text("Select who you want to meet with")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)

                    ForEach(users) { user in
                        let userAvail = availabilities.first { $0.userId == user.id }
                        let hasAvail = (userAvail?.slots.isEmpty == false)

                        Button {
                            if hasAvail {
                                onSelect(user)
                            }
                        } label: {
                            HStack(spacing: 14) {
                                UserAvatar(user: user, size: 44)

                                VStack(alignment: .leading, spacing: 2) {
                                    Text(user.name)
                                        .font(.body.weight(.medium))
                                    Text(hasAvail
                                         ? "\(Int(getTotalAvailableHours(userAvail?.slots ?? [])))h available"
                                         : "No availability set")
                                        .font(.caption)
                                        .foregroundStyle(hasAvail ? .blue : .red)
                                }

                                Spacer()

                                if hasAvail {
                                    Image(systemName: "chevron.right")
                                        .foregroundStyle(.secondary)
                                }
                            }
                            .padding()
                            .background(.regularMaterial, in: RoundedRectangle(cornerRadius: 12))
                            .opacity(hasAvail ? 1 : 0.5)
                        }
                        .buttonStyle(.plain)
                        .disabled(!hasAvail)
                        .accessibilityLabel(user.name)
                        .accessibilityValue(hasAvail ? "\(Int(getTotalAvailableHours(userAvail?.slots ?? []))) hours available" : "No availability set")
                    }
                }
                .padding()
            }
        }
    }
}
