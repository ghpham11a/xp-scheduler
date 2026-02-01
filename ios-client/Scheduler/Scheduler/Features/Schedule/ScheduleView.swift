import SwiftUI

enum ScheduleStep: Int, CaseIterable {
    case selectParticipant = 0
    case selectDuration
    case selectTime
    case confirm
}

struct ScheduleView: View {
    let currentUserId: String
    let users: [User]
    let availabilities: [Availability]
    let meetings: [Meeting]
    let use24HourTime: Bool
    let onScheduleMeeting: (String, String, String, Double, Double, String) -> Void
    let onCancelMeeting: (String) -> Void
    let userById: (String) -> User?

    @State private var currentStep: ScheduleStep = .selectParticipant
    @State private var selectedParticipant: User?
    @State private var selectedDuration: MeetingDuration?
    @State private var selectedDate: String?
    @State private var selectedStartHour: Double?
    @State private var meetingTitle = ""

    private var otherUsers: [User] {
        users.filter { $0.id != currentUserId }
    }

    private var currentUserMeetings: [Meeting] {
        getMeetingsForUser(meetings, userId: currentUserId)
    }

    var body: some View {
        VStack(spacing: 0) {
            if currentStep != .selectParticipant {
                wizardProgress
            }

            switch currentStep {
            case .selectParticipant:
                participantSelection
            case .selectDuration:
                durationSelection
            case .selectTime:
                timeSlotSelection
            case .confirm:
                confirmationView
            }

            if currentStep == .selectParticipant && !currentUserMeetings.isEmpty {
                Divider().padding(.vertical, 8)
                meetingListSection
            }
        }
    }

    private func resetWizard() {
        currentStep = .selectParticipant
        selectedParticipant = nil
        selectedDuration = nil
        selectedDate = nil
        selectedStartHour = nil
        meetingTitle = ""
    }

    // MARK: - Wizard Progress

    private var wizardProgress: some View {
        HStack {
            Button { goBack() } label: {
                Image(systemName: "chevron.left")
                    .font(.body.weight(.medium))
            }

            Spacer()

            HStack(spacing: 0) {
                ForEach(ScheduleStep.allCases, id: \.rawValue) { step in
                    if step.rawValue > 0 {
                        Rectangle()
                            .fill(currentStep.rawValue > step.rawValue - 1 ? Color.blue : Color(.systemGray4))
                            .frame(width: 20, height: 2)
                    }
                    Circle()
                        .fill(currentStep.rawValue >= step.rawValue ? Color.blue : Color(.systemGray4))
                        .frame(width: 22, height: 22)
                        .overlay {
                            Text("\(step.rawValue + 1)")
                                .font(.caption2.weight(.medium))
                                .foregroundStyle(currentStep.rawValue >= step.rawValue ? .white : .primary)
                        }
                }
            }

            Spacer()

            Button("Cancel") { resetWizard() }
                .font(.subheadline)
        }
        .padding(.horizontal)
        .padding(.vertical, 10)
    }

    private func goBack() {
        switch currentStep {
        case .selectDuration: currentStep = .selectParticipant
        case .selectTime: currentStep = .selectDuration
        case .confirm: currentStep = .selectTime
        default: break
        }
    }

    // MARK: - Step 1: Participant Selection

    private var participantSelection: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                Text("Schedule a Meeting")
                    .font(.title2.bold())
                Text("Select who you want to meet with")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)

                ForEach(otherUsers) { user in
                    let userAvail = availabilities.first { $0.userId == user.id }
                    let hasAvail = (userAvail?.slots.isEmpty == false)

                    Button {
                        if hasAvail {
                            selectedParticipant = user
                            currentStep = .selectDuration
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
                }
            }
            .padding()
        }
    }

    // MARK: - Step 2: Duration Selection

    private var durationSelection: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Meeting Duration")
                .font(.title2.bold())
            Text("How long should the meeting be?")
                .font(.subheadline)
                .foregroundStyle(.secondary)

            LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 12), count: 3), spacing: 12) {
                ForEach(MeetingDuration.allCases, id: \.self) { duration in
                    Button {
                        selectedDuration = duration
                        currentStep = .selectTime
                    } label: {
                        Text(duration.displayName)
                            .font(.subheadline.weight(.medium))
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 16)
                            .background(.regularMaterial, in: RoundedRectangle(cornerRadius: 12))
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(Color(.systemGray3), lineWidth: 1)
                            )
                    }
                    .buttonStyle(.plain)
                }
            }

            Spacer()
        }
        .padding()
    }

    // MARK: - Step 3: Time Slot Selection

    private var timeSlotSelection: some View {
        let next7Days = getNextDays(7)
        let participantSlots = availabilities.first { $0.userId == selectedParticipant?.id }?.slots ?? []
        let currentUserSlots = availabilities.first { $0.userId == currentUserId }?.slots ?? []
        let duration = selectedDuration?.hours ?? 0.5

        return ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                Text("Select Time")
                    .font(.title2.bold())

                if let participant = selectedParticipant, let dur = selectedDuration {
                    HStack(spacing: 6) {
                        Text("Meeting with")
                            .foregroundStyle(.secondary)
                        UserAvatar(user: participant, size: 22)
                        Text(participant.name)
                            .fontWeight(.medium)
                        Text("- \(dur.displayName)")
                            .foregroundStyle(.secondary)
                    }
                    .font(.subheadline)
                }

                ForEach(next7Days, id: \.self) { date in
                    let dateStr = toIsoString(date)
                    let dayParticipantSlots = participantSlots.filter { $0.date == dateStr }
                    let dayCurrentSlots = currentUserSlots.filter { $0.date == dateStr }

                    let available = findAvailableSlots(
                        dateStr: dateStr,
                        participantSlots: dayParticipantSlots,
                        currentUserSlots: dayCurrentSlots,
                        duration: duration,
                        meetings: meetings,
                        currentUserId: currentUserId,
                        participantId: selectedParticipant?.id ?? ""
                    )

                    if !available.isEmpty {
                        VStack(alignment: .leading, spacing: 8) {
                            Text(formatDateRelative(date))
                                .font(.subheadline.weight(.semibold))

                            ScrollView(.horizontal, showsIndicators: false) {
                                HStack(spacing: 8) {
                                    ForEach(available, id: \.self) { startHour in
                                        Button {
                                            selectedDate = dateStr
                                            selectedStartHour = startHour
                                            currentStep = .confirm
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
                                    }
                                }
                            }
                        }
                        .padding(.vertical, 4)
                    }
                }
            }
            .padding()
        }
    }

    // MARK: - Step 4: Confirmation

    private var confirmationView: some View {
        let endHour = (selectedStartHour ?? 0) + (selectedDuration?.hours ?? 0)

        return ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                Text("Confirm Meeting")
                    .font(.title2.bold())

                // Summary card
                VStack(alignment: .leading, spacing: 16) {
                    if let participant = selectedParticipant {
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

                    if let startHour = selectedStartHour, let dur = selectedDuration {
                        Label {
                            Text("\(formatTimeRange(startHour, endHour, use24Hour: use24HourTime)) (\(dur.displayName))")
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

                // Confirm button
                Button {
                    guard let participant = selectedParticipant,
                          let date = selectedDate,
                          let startHour = selectedStartHour,
                          let duration = selectedDuration else { return }
                    onScheduleMeeting(
                        currentUserId,
                        participant.id,
                        date,
                        startHour,
                        startHour + duration.hours,
                        meetingTitle
                    )
                    resetWizard()
                } label: {
                    Label("Schedule Meeting", systemImage: "checkmark")
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 6)
                }
                .buttonStyle(.borderedProminent)
                .disabled(meetingTitle.trimmingCharacters(in: .whitespaces).isEmpty)
            }
            .padding()
        }
    }

    // MARK: - Meeting List

    private var meetingListSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("Your Meetings")
                .font(.headline)
                .padding(.horizontal)

            let sorted = currentUserMeetings.sorted { ($0.date, $0.startHour) < ($1.date, $1.startHour) }

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
                                    onCancelMeeting(meeting.id)
                                } label: {
                                    Image(systemName: "xmark.circle.fill")
                                        .foregroundStyle(.red)
                                }
                            }
                        }
                        .padding()
                        .background(.regularMaterial, in: RoundedRectangle(cornerRadius: 10))
                    }
                }
                .padding(.horizontal)
            }
        }
    }
}
