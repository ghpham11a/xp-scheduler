import SwiftUI

struct CalendarView: View {

    @Environment(RouteManager.self) private var routeManager

    @State private var viewModel: CalendarViewModel

    init(viewModel: CalendarViewModel) {
        _viewModel = State(initialValue: viewModel)
    }

    @State private var selectedDayIndex: Int = 0
    @State private var selectedMeeting: Meeting?
    @State private var showMonthView: Bool = false
    @State private var monthOffset: Int = 0

    private let weekDays = getNextDays(7)

    private var currentUserMeetings: [Meeting] {
        getMeetingsForUser(viewModel.meetings, userId: viewModel.currentUserId)
    }

    private var selectedDay: Date {
        weekDays[selectedDayIndex]
    }

    private var selectedDayMeetings: [Meeting] {
        let dateStr = toIsoString(selectedDay)
        return currentUserMeetings
            .filter { $0.date == dateStr }
            .sorted { $0.startHour < $1.startHour }
    }

    var body: some View {
        VStack(spacing: 0) {
            HeaderSection(
                showMonthView: showMonthView,
                monthOffset: monthOffset,
                weekRangeLabel: weekRangeLabel,
                meetingCount: selectedDayMeetings.count,
                onPreviousMonth: { monthOffset -= 1 },
                onNextMonth: { monthOffset += 1 },
                onToggleView: {
                    withAnimation(.easeInOut(duration: 0.2)) {
                        showMonthView.toggle()
                        monthOffset = 0
                    }
                },
                monthLabel: monthLabel(for: monthOffset)
            )
            if showMonthView {
                MonthAgendaView(
                    monthOffset: monthOffset,
                    meetings: currentUserMeetings,
                    use24HourTime: viewModel.use24HourTime,
                    userById: { viewModel.userById($0) },
                    onMeetingTap: { selectedMeeting = $0 }
                )
            } else {
                DaySelectorRow(
                    weekDays: weekDays,
                    selectedDayIndex: selectedDayIndex,
                    currentUserMeetings: currentUserMeetings,
                    onSelectDay: { index in
                        withAnimation(.easeInOut(duration: 0.2)) {
                            selectedDayIndex = index
                        }
                    }
                )
                Divider()
                AgendaSection(
                    meetings: selectedDayMeetings,
                    selectedDay: selectedDay,
                    use24HourTime: viewModel.use24HourTime,
                    userById: { viewModel.userById($0) },
                    onMeetingTap: { selectedMeeting = $0 }
                )
            }
        }
        .onChange(of: routeManager.pendingDeepLink) { _, newValue in
            guard case .calendarMeeting(let meetingId) = newValue else { return }
            if let meeting = currentUserMeetings.first(where: { $0.id == meetingId }) {
                selectedMeeting = meeting
            }
            routeManager.pendingDeepLink = nil
        }
        .sheet(item: $selectedMeeting) { meeting in
            MeetingDetailSheet(
                meeting: meeting,
                currentUserId: viewModel.currentUserId,
                use24HourTime: viewModel.use24HourTime,
                userById: { viewModel.userById($0) },
                onCancel: {
                    viewModel.cancelMeeting(meeting.id)
                    selectedMeeting = nil
                },
                onDismiss: { selectedMeeting = nil }
            )
            .presentationDetents([.medium])
        }
    }

    // MARK: - Helpers

    private var weekRangeLabel: String {
        let f = DateFormatter()
        f.dateFormat = "MMM d"
        let start = weekDays.first ?? Date()
        let end = weekDays.last ?? Date()
        return "\(f.string(from: start)) - \(f.string(from: end))"
    }

    private func monthLabel(for offset: Int) -> String {
        let calendar = Calendar.current
        guard let date = calendar.date(byAdding: .month, value: offset, to: Date()) else {
            return ""
        }
        let f = DateFormatter()
        f.dateFormat = "MMMM yyyy"
        return f.string(from: date)
    }
}
