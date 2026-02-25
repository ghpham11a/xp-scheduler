import SwiftUI

extension ScheduleView {
    struct TimeSlotSelection: View {
        let participant: User?
        let availabilities: [Availability]
        let selectedDuration: MeetingDuration
        let meetings: [Meeting]
        let currentUserId: String
        let use24HourTime: Bool
        let onSelectDuration: (MeetingDuration) -> Void
        let onSelectSlot: (String, Double) -> Void

        var body: some View {
            let next7Days = getNextDays(7)
            let participantSlots = availabilities.first { $0.userId == participant?.id }?.slots ?? []
            let duration = selectedDuration.hours

            ScrollView {
                VStack(alignment: .leading, spacing: 12) {
                    Text("Select Time")
                        .font(.title2.bold())

                    if let participant {
                        HStack(spacing: 6) {
                            Text("Meeting with")
                                .foregroundStyle(.secondary)
                            UserAvatar(user: participant, size: 22)
                            Text(participant.name)
                                .fontWeight(.medium)
                        }
                        .font(.subheadline)
                    }

                    // Duration picker
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Duration")
                            .font(.subheadline.weight(.medium))
                            .foregroundStyle(.secondary)

                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 8) {
                                ForEach(MeetingDuration.allCases, id: \.self) { dur in
                                    Button {
                                        onSelectDuration(dur)
                                    } label: {
                                        Text(dur.displayName)
                                            .font(.subheadline.weight(.medium))
                                            .padding(.horizontal, 14)
                                            .padding(.vertical, 8)
                                            .background(
                                                selectedDuration == dur ? Color.blue : Color(.secondarySystemBackground),
                                                in: RoundedRectangle(cornerRadius: 8)
                                            )
                                            .foregroundStyle(selectedDuration == dur ? .white : .primary)
                                    }
                                    .buttonStyle(.plain)
                                    .accessibilityAddTraits(selectedDuration == dur ? .isSelected : [])
                                }
                            }
                        }
                    }

                    Divider()
                        .padding(.vertical, 4)

                    // Available time slots
                    let allAvailable = next7Days.flatMap { date -> [Double] in
                        let dateStr = toIsoString(date)
                        return findAvailableSlots(
                            dateStr: dateStr,
                            participantSlots: participantSlots.filter { $0.date == dateStr },
                            duration: duration,
                            meetings: meetings,
                            currentUserId: currentUserId,
                            participantId: participant?.id ?? ""
                        )
                    }

                    if allAvailable.isEmpty {
                        ContentUnavailableView(
                            "No Available Times",
                            systemImage: "calendar.badge.exclamationmark",
                            description: Text("No available times for the selected duration. Try a shorter duration or ask the participant to update their availability.")
                        )
                    } else {
                        ForEach(next7Days, id: \.self) { date in
                            let dateStr = toIsoString(date)
                            let dayParticipantSlots = participantSlots.filter { $0.date == dateStr }

                            let available = findAvailableSlots(
                                dateStr: dateStr,
                                participantSlots: dayParticipantSlots,
                                duration: duration,
                                meetings: meetings,
                                currentUserId: currentUserId,
                                participantId: participant?.id ?? ""
                            )

                            if !available.isEmpty {
                                VStack(alignment: .leading, spacing: 8) {
                                    Text(formatDateRelative(date))
                                        .font(.subheadline.weight(.semibold))

                                    ScrollView(.horizontal, showsIndicators: false) {
                                        HStack(spacing: 8) {
                                            ForEach(available, id: \.self) { startHour in
                                                Button {
                                                    onSelectSlot(dateStr, startHour)
                                                } label: {
                                                    Text(formatHour(startHour, use24Hour: use24HourTime))
                                                        .font(.subheadline)
                                                        .padding(.horizontal, 12)
                                                        .padding(.vertical, 8)
                                                        .background(.regularMaterial, in: RoundedRectangle(cornerRadius: 8))
                                                        .overlay(
                                                            RoundedRectangle(cornerRadius: 8)
                                                                .stroke(Color(.systemGray3), lineWidth: 1)
                                                        )
                                                }
                                                .buttonStyle(.plain)
                                                .accessibilityHint("Double tap to select this time")
                                            }
                                        }
                                    }
                                }
                                .padding(.vertical, 4)
                            }
                        }
                    }
                }
                .padding()
            }
        }
    }
}
