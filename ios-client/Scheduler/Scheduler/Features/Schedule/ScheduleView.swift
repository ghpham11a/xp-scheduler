import SwiftUI

enum ScheduleStep: Int, CaseIterable {
    case selectParticipant = 0
    case selectTime
    case confirm
}

struct ScheduleView: View {

    @State private var viewModel: ViewModel

    init(viewModel: ViewModel) {
        _viewModel = State(initialValue: viewModel)
    }

    @State private var currentStep: ScheduleStep = .selectParticipant
    @State private var selectedParticipant: User?
    @State private var selectedDuration: MeetingDuration = .thirtyMin
    @State private var selectedDate: String?
    @State private var selectedStartHour: Double?
    @State private var meetingTitle = ""

    private var otherUsers: [User] {
        viewModel.users.filter { $0.id != viewModel.currentUserId }
    }

    private var currentUserMeetings: [Meeting] {
        getMeetingsForUser(viewModel.meetings, userId: viewModel.currentUserId)
    }

    var body: some View {
        VStack(spacing: 0) {
            if currentStep != .selectParticipant {
                wizardProgress
            }

            switch currentStep {
            case .selectParticipant:
                participantSelection
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
        selectedDuration = .thirtyMin
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

            Button("Cancel") { resetWizard() }
                .font(.subheadline)
        }
        .padding(.horizontal)
        .padding(.vertical, 10)
    }

    private func goBack() {
        switch currentStep {
        case .selectTime: currentStep = .selectParticipant
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
                    let userAvail = viewModel.availabilities.first { $0.userId == user.id }
                    let hasAvail = (userAvail?.slots.isEmpty == false)

                    Button {
                        if hasAvail {
                            selectedParticipant = user
                            currentStep = .selectTime
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

    // MARK: - Step 2: Duration + Time Slot Selection

    private var timeSlotSelection: some View {
        let next7Days = getNextDays(7)
        let participantSlots = viewModel.availabilities.first { $0.userId == selectedParticipant?.id }?.slots ?? []
        let duration = selectedDuration.hours

        return ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                Text("Select Time")
                    .font(.title2.bold())

                if let participant = selectedParticipant {
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
                                    selectedDuration = dur
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
                        meetings: viewModel.meetings,
                        currentUserId: viewModel.currentUserId,
                        participantId: selectedParticipant?.id ?? ""
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
                            meetings: viewModel.meetings,
                            currentUserId: viewModel.currentUserId,
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
                                                Text(formatHour(startHour, use24Hour: viewModel.use24HourTime))
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
            }
            .padding()
        }
    }

    // MARK: - Step 4: Confirmation

    private var confirmationView: some View {
        let endHour = (selectedStartHour ?? 0) + selectedDuration.hours

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

                    if let startHour = selectedStartHour {
                        Label {
                            Text("\(formatTimeRange(startHour, endHour, use24Hour: viewModel.use24HourTime)) (\(selectedDuration.displayName))")
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
                          let startHour = selectedStartHour else { return }
                    viewModel.addMeeting(
                        organizerId: viewModel.currentUserId,
                        participantId: participant.id,
                        date: date,
                        startHour: startHour,
                        endHour: startHour + selectedDuration.hours,
                        title: meetingTitle
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
                        let otherUserId = meeting.organizerId == viewModel.currentUserId
                            ? meeting.participantId : meeting.organizerId
                        let otherUser = viewModel.userById(otherUserId)
                        let isOrganizer = meeting.organizerId == viewModel.currentUserId

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
                                    Text("\(formatDateRelative(date)) - \(formatTimeRange(meeting.startHour, meeting.endHour, use24Hour: viewModel.use24HourTime))")
                                        .font(.caption)
                                        .foregroundStyle(.secondary)
                                }
                            }

                            Spacer()

                            if isOrganizer {
                                Button {
                                    viewModel.cancelMeeting(meeting.id)
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
