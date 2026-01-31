import SwiftUI

struct CalendarView: View {
    let currentUserId: String
    let users: [User]
    let availabilities: [Availability]
    let meetings: [Meeting]
    let use24HourTime: Bool
    let onCancelMeeting: (String) -> Void
    let userById: (String) -> User?

    @State private var selectedDayIndex: Int = 0
    @State private var selectedMeeting: Meeting?
    @State private var showMonthView: Bool = false
    @State private var monthOffset: Int = 0

    private let weekDays = getNextDays(7)

    private var currentUserMeetings: [Meeting] {
        getMeetingsForUser(meetings, userId: currentUserId)
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
                    use24HourTime: use24HourTime,
                    userById: userById,
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
                currentUserId: currentUserId,
                use24HourTime: use24HourTime,
                userById: userById,
                onCancel: {
                    onCancelMeeting(meeting.id)
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
                            use24HourTime: use24HourTime,
                            userById: userById,
                            onTap: { selectedMeeting = meeting }
                        )
                    }
                }
                .listStyle(.insetGrouped)
            }
        }
    }
}

// MARK: - Day Pill

private struct DayPill: View {
    let date: Date
    let isSelected: Bool
    let isToday: Bool
    let meetingCount: Int

    var body: some View {
        VStack(spacing: 2) {
            Text(shortDayName(date))
                .font(.caption2)
                .fontWeight(.medium)
            Text("\(dayOfMonth(date))")
                .font(.callout.bold())
            if meetingCount > 0 {
                Circle()
                    .fill(isSelected ? .white : .blue)
                    .frame(width: 6, height: 6)
            } else {
                Spacer().frame(height: 6)
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 8)
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(
                    isSelected ? Color.blue :
                    isToday ? Color.blue.opacity(0.15) :
                    Color(.systemGray6)
                )
        )
        .foregroundStyle(
            isSelected ? .white :
            isToday ? .blue :
            .primary
        )
    }
}

// MARK: - Meeting Row

private struct MeetingRow: View {
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

// MARK: - Month Agenda View

private struct MonthAgendaView: View {
    let monthOffset: Int
    let meetings: [Meeting]
    let use24HourTime: Bool
    let userById: (String) -> User?
    let onMeetingTap: (Meeting) -> Void

    private var monthDays: [Date] {
        let calendar = Calendar.current
        guard let targetMonth = calendar.date(byAdding: .month, value: monthOffset, to: Date()),
              let monthInterval = calendar.dateInterval(of: .month, for: targetMonth) else { return [] }
        var days: [Date] = []
        var current = monthInterval.start
        while current < monthInterval.end {
            days.append(current)
            current = calendar.date(byAdding: .day, value: 1, to: current) ?? current
        }
        return days
    }

    private var daysWithMeetings: [(Date, [Meeting])] {
        monthDays.compactMap { date in
            let dateStr = toIsoString(date)
            let dayMeetings = meetings
                .filter { $0.date == dateStr }
                .sorted { $0.startHour < $1.startHour }
            if dayMeetings.isEmpty && !isToday(date) {
                return nil
            }
            return (date, dayMeetings)
        }
    }

    var body: some View {
        if daysWithMeetings.isEmpty {
            ContentUnavailableView(
                "No Meetings",
                systemImage: "calendar",
                description: Text("No meetings scheduled this month")
            )
        } else {
            List {
                ForEach(daysWithMeetings, id: \.0) { date, dayMeetings in
                    Section {
                        if dayMeetings.isEmpty {
                            Text("No meetings")
                                .font(.subheadline)
                                .foregroundStyle(.secondary)
                        } else {
                            ForEach(dayMeetings) { meeting in
                                MeetingRow(
                                    meeting: meeting,
                                    use24HourTime: use24HourTime,
                                    userById: userById,
                                    onTap: { onMeetingTap(meeting) }
                                )
                            }
                        }
                    } header: {
                        HStack {
                            Text(formatDayHeader(date))
                                .font(.subheadline.weight(.semibold))
                            if isToday(date) {
                                Text("Today")
                                    .font(.caption)
                                    .foregroundStyle(.white)
                                    .padding(.horizontal, 6)
                                    .padding(.vertical, 2)
                                    .background(.blue, in: Capsule())
                            }
                        }
                    }
                }
            }
            .listStyle(.insetGrouped)
        }
    }

    private func formatDayHeader(_ date: Date) -> String {
        let f = DateFormatter()
        f.dateFormat = "EEEE, MMM d"
        return f.string(from: date)
    }
}

// MARK: - Meeting Detail Sheet

private struct MeetingDetailSheet: View {
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
