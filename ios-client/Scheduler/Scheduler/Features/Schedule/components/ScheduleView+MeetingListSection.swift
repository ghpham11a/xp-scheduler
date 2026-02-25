import SwiftUI

extension ScheduleView {
    struct MeetingListSection: View {
        let meetings: [Meeting]
        let currentUserId: String
        let use24HourTime: Bool
        let userById: (String) -> User?
        let onCancel: (String) -> Void

        var body: some View {
            VStack(alignment: .leading, spacing: 10) {
                Text("Your Meetings")
                    .font(.headline)
                    .padding(.horizontal)

                let sorted = meetings.sorted { ($0.date, $0.startHour) < ($1.date, $1.startHour) }

                ScrollView {
                    LazyVStack(spacing: 8) {
                        ForEach(sorted) { meeting in
                            let otherUserId = meeting.organizerId == currentUserId
                                ? meeting.participantId : meeting.organizerId
                            let otherUser = userById(otherUserId)
                            let isOrganizer = meeting.organizerId == currentUserId

                            HStack(spacing: 12) {
                                if let otherUser {
                                    UserAvatar(user: otherUser, size: 36)
                                }

                                VStack(alignment: .leading, spacing: 2) {
                                    Text(meeting.title)
                                        .font(.subheadline.weight(.medium))
                                    Text("with \(otherUser?.name ?? "Unknown")")
                                        .font(.caption)
                                        .foregroundStyle(.secondary)
                                    if let date = fromIsoString(meeting.date) {
                                        Text("\(formatDateRelative(date)) - \(formatTimeRange(meeting.startHour, meeting.endHour, use24Hour: use24HourTime))")
                                            .font(.caption)
                                            .foregroundStyle(.secondary)
                                    }
                                }

                                Spacer()

                                if isOrganizer {
                                    Button {
                                        onCancel(meeting.id)
                                    } label: {
                                        Image(systemName: "xmark.circle.fill")
                                            .foregroundStyle(.red)
                                    }
                                    .accessibilityLabel("Cancel meeting")
                                    .accessibilityHint("Double tap to cancel \(meeting.title)")
                                }
                            }
                            .padding()
                            .background(.regularMaterial, in: RoundedRectangle(cornerRadius: 10))
                            .accessibilityElement(children: .combine)
                        }
                    }
                    .padding(.horizontal)
                }
            }
        }
    }
}
