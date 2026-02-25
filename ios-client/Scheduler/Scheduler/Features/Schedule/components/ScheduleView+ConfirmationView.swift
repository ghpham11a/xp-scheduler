import SwiftUI

extension ScheduleView {
    struct ConfirmationView: View {
        let participant: User?
        let selectedDate: String?
        let selectedStartHour: Double?
        let selectedDuration: MeetingDuration
        let use24HourTime: Bool
        @Binding var meetingTitle: String
        let onConfirm: () -> Void

        private var endHour: Double {
            (selectedStartHour ?? 0) + selectedDuration.hours
        }

        var body: some View {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    Text("Confirm Meeting")
                        .font(.title2.bold())

                    // Summary card
                    VStack(alignment: .leading, spacing: 16) {
                        if let participant {
                            Label {
                                HStack(spacing: 8) {
                                    UserAvatar(user: participant, size: 24)
                                    Text(participant.name)
                                        .fontWeight(.medium)
                                }
                            } icon: {
                                Image(systemName: "person")
                            }
                        }

                        if let dateStr = selectedDate, let date = fromIsoString(dateStr) {
                            Label {
                                Text(formatDateFull(date))
                                    .fontWeight(.medium)
                            } icon: {
                                Image(systemName: "calendar")
                            }
                        }

                        if let startHour = selectedStartHour {
                            Label {
                                Text("\(formatTimeRange(startHour, endHour, use24Hour: use24HourTime)) (\(selectedDuration.displayName))")
                                    .fontWeight(.medium)
                            } icon: {
                                Image(systemName: "clock")
                            }
                        }
                    }
                    .padding()
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(.regularMaterial, in: RoundedRectangle(cornerRadius: 12))

                    // Title input
                    TextField("Meeting Title", text: $meetingTitle)
                        .textFieldStyle(.roundedBorder)
                        .accessibilityLabel("Meeting title")

                    // Confirm button
                    Button(action: onConfirm) {
                        Label("Schedule Meeting", systemImage: "checkmark")
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 6)
                    }
                    .buttonStyle(.borderedProminent)
                    .disabled(meetingTitle.trimmingCharacters(in: .whitespaces).isEmpty)
                    .accessibilityHint("Double tap to confirm and schedule this meeting")
                }
                .padding()
            }
        }
    }
}
