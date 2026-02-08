import SwiftUI

struct CalendarView: View {

    @State private var viewModel: ViewModel

    init(viewModel: ViewModel) {
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
            headerSection
            if showMonthView {
                MonthAgendaView(
                    monthOffset: monthOffset,
                    meetings: currentUserMeetings,
                    use24HourTime: viewModel.use24HourTime,
                    userById: { viewModel.userById($0) },
                    onMeetingTap: { selectedMeeting = $0 }
                )
            } else {
                daySelectorRow
                Divider()
                agendaSection
            }
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

    // MARK: - Header

    private var headerSection: some View {
        HStack {
            if showMonthView {
                HStack(spacing: 12) {
                    Button { monthOffset -= 1 } label: {
                        Image(systemName: "chevron.left")
                    }
                    Text(monthLabel(for: monthOffset))
                        .font(.title3.weight(.semibold))
                        .frame(minWidth: 140)
                    Button { monthOffset += 1 } label: {
                        Image(systemName: "chevron.right")
                    }
                }
            } else {
                VStack(alignment: .leading, spacing: 2) {
                    Text(weekRangeLabel)
                        .font(.title3.weight(.semibold))
                    Text("\(selectedDayMeetings.count) meeting\(selectedDayMeetings.count == 1 ? "" : "s") today")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
            }
            Spacer()
            Button {
                withAnimation(.easeInOut(duration: 0.2)) {
                    showMonthView.toggle()
                    monthOffset = 0
                }
            } label: {
                Image(systemName: showMonthView ? "calendar.day.timeline.left" : "calendar")
                    .font(.title3)
                    .foregroundStyle(.blue)
            }
        }
        .padding(.horizontal)
        .padding(.top, 8)
        .padding(.bottom, 4)
    }

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

    // MARK: - Day Selector

    private var daySelectorRow: some View {
        ScrollViewReader { proxy in
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(Array(weekDays.enumerated()), id: \.element) { index, date in
                        let isSelected = index == selectedDayIndex
                        let today = isToday(date)
                        let dateStr = toIsoString(date)
                        let meetingCount = currentUserMeetings.filter { $0.date == dateStr }.count

                        DayPill(
                            date: date,
                            isSelected: isSelected,
                            isToday: today,
                            meetingCount: meetingCount
                        )
                        .id(index)
                        .onTapGesture {
                            withAnimation(.easeInOut(duration: 0.2)) {
                                selectedDayIndex = index
                            }
                        }
                    }
                }
                .padding(.horizontal, 16)
            }
            .padding(.vertical, 8)
        }
    }

    // MARK: - Agenda

    private var agendaSection: some View {
        Group {
            if selectedDayMeetings.isEmpty {
                ContentUnavailableView(
                    "No Meetings",
                    systemImage: "calendar",
                    description: Text("No meetings scheduled for \(formatDateRelative(selectedDay))")
                )
            } else {
                List {
                    ForEach(selectedDayMeetings) { meeting in
                        MeetingRow(
                            meeting: meeting,
                            use24HourTime: viewModel.use24HourTime,
                            userById: { viewModel.userById($0) },
                            onTap: { selectedMeeting = meeting }
                        )
                    }
                }
                .listStyle(.insetGrouped)
            }
        }
    }
}
