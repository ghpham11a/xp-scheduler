import SwiftUI

extension CalendarView {
    struct MeetingDetailSheet: View {
        let meeting: Meeting
        let currentUserId: String
        let use24HourTime: Bool
        let userById: (String) -> User?
        let onCancel: () -> Void
        let onDismiss: () -> Void

        private var isOrganizer: Bool { meeting.organizerId == currentUserId }

        var body: some View {
            NavigationStack {
                List {
                    Section {
                        Label {
                            Text(formatTimeRange(meeting.startHour, meeting.endHour, use24Hour: use24HourTime))
                        } icon: {
                            Image(systemName: "clock")
                        }

                        if let date = fromIsoString(meeting.date) {
                            Label {
                                Text(formatDateFull(date))
                            } icon: {
                                Image(systemName: "calendar")
                            }
                        }
                    }

                    Section("Organizer") {
                        if let organizer = userById(meeting.organizerId) {
                            HStack(spacing: 10) {
                                UserAvatar(user: organizer, size: 30)
                                Text(organizer.name)
                            }
                        }
                    }

                    Section("Participant") {
                        if let participant = userById(meeting.participantId) {
                            HStack(spacing: 10) {
                                UserAvatar(user: participant, size: 30)
                                Text(participant.name)
                            }
                        }
                    }

                    if isOrganizer {
                        Section {
                            Button("Cancel Meeting", role: .destructive, action: onCancel)
                        }
                    }
                }
                .navigationTitle(meeting.title)
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .confirmationAction) {
                        Button("Done", action: onDismiss)
                    }
                }
            }
        }
    }
}
