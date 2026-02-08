import SwiftUI

extension CalendarView {
    struct MeetingRow: View {
        let meeting: Meeting
        let use24HourTime: Bool
        let userById: (String) -> User?
        let onTap: () -> Void

        var body: some View {
            Button(action: onTap) {
                HStack(spacing: 12) {
                    RoundedRectangle(cornerRadius: 3)
                        .fill(colorFromHex(
                            (userById(meeting.participantId) ?? userById(meeting.organizerId))?.avatarColor ?? "#3B82F6"
                        ))
                        .frame(width: 4, height: 50)

                    VStack(alignment: .leading, spacing: 4) {
                        Text(meeting.title)
                            .font(.body.weight(.medium))
                        Text(formatTimeRange(meeting.startHour, meeting.endHour, use24Hour: use24HourTime))
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                    }

                    Spacer()

                    if let other = userById(meeting.participantId) ?? userById(meeting.organizerId) {
                        VStack(spacing: 2) {
                            UserAvatar(user: other, size: 32)
                            Text(other.name.components(separatedBy: " ").first ?? "")
                                .font(.caption2)
                                .foregroundStyle(.secondary)
                        }
                    }
                }
                .padding(.vertical, 4)
            }
            .buttonStyle(.plain)
        }
    }
}
