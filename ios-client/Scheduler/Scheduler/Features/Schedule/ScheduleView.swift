import SwiftUI

enum ScheduleStep: Int, CaseIterable {
    case selectParticipant = 0
    case selectTime
    case confirm
}

struct ScheduleView: View {

    @Environment(RouteManager.self) private var routeManager

    @State private var viewModel: ScheduleViewModel

    init(viewModel: ScheduleViewModel) {
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
                WizardProgress(
                    onBack: { goBack() },
                    onCancel: { resetWizard() }
                )
            }

            switch currentStep {
            case .selectParticipant:
                ParticipantSelection(
                    users: otherUsers,
                    availabilities: viewModel.availabilities,
                    onSelect: { user in
                        selectedParticipant = user
                        currentStep = .selectTime
                    }
                )
            case .selectTime:
                TimeSlotSelection(
                    participant: selectedParticipant,
                    availabilities: viewModel.availabilities,
                    selectedDuration: selectedDuration,
                    meetings: viewModel.meetings,
                    currentUserId: viewModel.currentUserId,
                    use24HourTime: viewModel.use24HourTime,
                    onSelectDuration: { selectedDuration = $0 },
                    onSelectSlot: { date, startHour in
                        selectedDate = date
                        selectedStartHour = startHour
                        currentStep = .confirm
                    }
                )
            case .confirm:
                ConfirmationView(
                    participant: selectedParticipant,
                    selectedDate: selectedDate,
                    selectedStartHour: selectedStartHour,
                    selectedDuration: selectedDuration,
                    use24HourTime: viewModel.use24HourTime,
                    meetingTitle: $meetingTitle,
                    onConfirm: {
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
                    }
                )
            }

            if currentStep == .selectParticipant && !currentUserMeetings.isEmpty {
                Divider().padding(.vertical, 8)
                MeetingListSection(
                    meetings: currentUserMeetings,
                    currentUserId: viewModel.currentUserId,
                    use24HourTime: viewModel.use24HourTime,
                    userById: { viewModel.userById($0) },
                    onCancel: { viewModel.cancelMeeting($0) }
                )
            }
        }
        .onChange(of: routeManager.pendingDeepLink) { _, newValue in
            guard case .scheduleWith(let userId) = newValue else { return }
            if let user = otherUsers.first(where: { $0.id == userId }) {
                let userAvail = viewModel.availabilities.first { $0.userId == user.id }
                if userAvail?.slots.isEmpty == false {
                    selectedParticipant = user
                    currentStep = .selectTime
                }
            }
            routeManager.pendingDeepLink = nil
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

    private func goBack() {
        switch currentStep {
        case .selectTime: currentStep = .selectParticipant
        case .confirm: currentStep = .selectTime
        default: break
        }
    }
}
