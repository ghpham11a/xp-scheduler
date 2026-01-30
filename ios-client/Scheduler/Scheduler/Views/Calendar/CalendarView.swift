import SwiftUI

enum CalendarViewMode: String, CaseIterable {
    case week = "Week"
    case day = "Day"
    case agenda = "Agenda"

    var icon: String {
        switch self {
        case .week: "calendar"
        case .day: "calendar.day.timeline.left"
        case .agenda: "list.bullet"
        }
    }
}

struct CalendarView: View {
    let currentUserId: String
    let users: [User]
    let availabilities: [Availability]
    let meetings: [Meeting]
    let onCancelMeeting: (String) -> Void
    let userById: (String) -> User?

    @State private var viewMode: CalendarViewMode = .week
    @State private var weekOffset = 0
    @State private var selectedDay = Date()
    @State private var selectedMeeting: Meeting?
    @State private var showAllHours = false

    private var weekStart: Date {
        let ref = Calendar.current.date(byAdding: .weekOfYear, value: weekOffset, to: Date())!
        return getWeekStart(ref)
    }

    private var weekDays: [Date] {
        (0..<7).compactMap { Calendar.current.date(byAdding: .day, value: $0, to: weekStart) }
    }

    private var currentUserMeetings: [Meeting] {
        getMeetingsForUser(meetings, userId: currentUserId)
    }

    private var currentUserAvailability: [TimeSlot] {
        availabilities.first { $0.userId == currentUserId }?.slots ?? []
    }

    var body: some View {
        VStack(spacing: 0) {
            calendarHeader
            Divider()

            switch viewMode {
            case .week:
                WeekGridView(
                    weekDays: weekDays,
                    meetings: currentUserMeetings,
                    availability: currentUserAvailability,
                    showAllHours: showAllHours,
                    userById: userById,
                    onMeetingTap: { selectedMeeting = $0 }
                )
            case .day:
                DayDetailView(
                    selectedDay: $selectedDay,
                    weekDays: weekDays,
                    meetings: currentUserMeetings,
                    availability: currentUserAvailability,
                    showAllHours: showAllHours,
                    userById: userById,
                    onMeetingTap: { selectedMeeting = $0 }
                )
            case .agenda:
                AgendaListView(
                    weekDays: weekDays,
                    meetings: currentUserMeetings,
                    availability: currentUserAvailability,
                    userById: userById,
                    onMeetingTap: { selectedMeeting = $0 }
                )
            }
        }
        .sheet(item: $selectedMeeting) { meeting in
            MeetingDetailSheet(
                meeting: meeting,
                currentUserId: currentUserId,
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

    private var calendarHeader: some View {
        VStack(spacing: 8) {
            HStack {
                HStack(spacing: 4) {
                    Button { weekOffset -= 1 } label: {
                        Image(systemName: "chevron.left")
                    }
                    Button("Today") {
                        weekOffset = 0
                        selectedDay = Date()
                    }
                    .font(.subheadline)
                    Button { weekOffset += 1 } label: {
                        Image(systemName: "chevron.right")
                    }
                }

                Spacer()

                let start = weekDays.first ?? Date()
                let end = weekDays.last ?? Date()
                Text(weekRangeLabel(start: start, end: end))
                    .font(.subheadline.weight(.medium))
            }
            .padding(.horizontal)

            HStack {
                Picker("View", selection: $viewMode) {
                    ForEach(CalendarViewMode.allCases, id: \.self) { mode in
                        Label(mode.rawValue, systemImage: mode.icon)
                            .tag(mode)
                    }
                }
                .pickerStyle(.segmented)

                Spacer()

                Toggle("24h", isOn: $showAllHours)
                    .fixedSize()
                    .font(.caption)
            }
            .padding(.horizontal)
            .padding(.bottom, 4)
        }
        .padding(.top, 4)
    }

    private func weekRangeLabel(start: Date, end: Date) -> String {
        let f = DateFormatter()
        f.dateFormat = "MMM d"
        return "\(f.string(from: start)) - \(f.string(from: end))"
    }
}

// MARK: - Week Grid

private struct WeekGridView: View {
    let weekDays: [Date]
    let meetings: [Meeting]
    let availability: [TimeSlot]
    let showAllHours: Bool
    let userById: (String) -> User?
    let onMeetingTap: (Meeting) -> Void

    private var startHour: Int { showAllHours ? 0 : 6 }
    private var endHour: Int { showAllHours ? 24 : 22 }
    private var hours: [Int] { Array(startHour..<endHour) }
    private var hourHeight: CGFloat { 50 }

    var body: some View {
        ScrollView(.vertical) {
            HStack(alignment: .top, spacing: 0) {
                // Time labels
                VStack(spacing: 0) {
                    Color.clear.frame(height: 36) // header space
                    ForEach(hours, id: \.self) { hour in
                        Text(formatHour(Double(hour)))
                            .font(.system(size: 9))
                            .foregroundStyle(.secondary)
                            .frame(width: 44, height: hourHeight, alignment: .topTrailing)
                            .padding(.trailing, 4)
                    }
                }

                // Day columns
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 0) {
                        ForEach(weekDays, id: \.self) { day in
                            DayGridColumn(
                                day: day,
                                meetings: meetings.filter { $0.date == toIsoString(day) },
                                availability: availability.filter { $0.date == toIsoString(day) },
                                startHour: startHour,
                                hourHeight: hourHeight,
                                hours: hours,
                                userById: userById,
                                onMeetingTap: onMeetingTap
                            )
                            .frame(width: 90)
                        }
                    }
                }
            }
        }
    }
}

private struct DayGridColumn: View {
    let day: Date
    let meetings: [Meeting]
    let availability: [TimeSlot]
    let startHour: Int
    let hourHeight: CGFloat
    let hours: [Int]
    let userById: (String) -> User?
    let onMeetingTap: (Meeting) -> Void

    private var today: Bool { isToday(day) }

    var body: some View {
        VStack(spacing: 0) {
            // Day header
            VStack(spacing: 1) {
                Text(shortDayName(day))
                    .font(.caption2)
                Text("\(dayOfMonth(day))")
                    .font(.subheadline)
                    .fontWeight(today ? .bold : .regular)
            }
            .frame(height: 36)
            .frame(maxWidth: .infinity)
            .background(today ? Color.blue.opacity(0.12) : .clear)

            // Time grid
            ZStack(alignment: .top) {
                // Grid lines
                VStack(spacing: 0) {
                    ForEach(hours, id: \.self) { _ in
                        Rectangle()
                            .fill(Color(.systemGray5))
                            .frame(height: 1)
                            .frame(maxWidth: .infinity)
                            .padding(.bottom, hourHeight - 1)
                    }
                }

                // Availability blocks
                ForEach(Array(availability.enumerated()), id: \.offset) { _, slot in
                    let top = (slot.startHour - Double(startHour)) * hourHeight
                    let height = (slot.endHour - slot.startHour) * hourHeight
                    if slot.startHour >= Double(startHour) {
                        Rectangle()
                            .fill(Color.blue.opacity(0.1))
                            .frame(height: height)
                            .offset(y: top)
                    }
                }

                // Meetings
                ForEach(meetings) { meeting in
                    let top = (meeting.startHour - Double(startHour)) * hourHeight
                    let height = (meeting.endHour - meeting.startHour) * hourHeight
                    if meeting.startHour >= Double(startHour) {
                        let otherUser = userById(meeting.participantId) ?? userById(meeting.organizerId)
                        let bgColor = colorFromHex(otherUser?.avatarColor ?? "#3B82F6")

                        Button { onMeetingTap(meeting) } label: {
                            Text(meeting.title)
                                .font(.system(size: 9))
                                .foregroundStyle(.white)
                                .lineLimit(2)
                                .padding(3)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .frame(height: height)
                                .background(bgColor.opacity(0.85), in: RoundedRectangle(cornerRadius: 3))
                        }
                        .buttonStyle(.plain)
                        .offset(y: top)
                        .padding(.horizontal, 1)
                    }
                }

                // Current time indicator
                if today {
                    let now = Date()
                    let cal = Calendar.current
                    let currentHour = Double(cal.component(.hour, from: now)) + Double(cal.component(.minute, from: now)) / 60.0
                    if currentHour >= Double(startHour) && currentHour <= Double(startHour + hours.count) {
                        let top = (currentHour - Double(startHour)) * hourHeight
                        Rectangle()
                            .fill(.red)
                            .frame(height: 2)
                            .offset(y: top)
                    }
                }
            }
            .frame(height: CGFloat(hours.count) * hourHeight)
        }
    }
}

// MARK: - Day View

private struct DayDetailView: View {
    @Binding var selectedDay: Date
    let weekDays: [Date]
    let meetings: [Meeting]
    let availability: [TimeSlot]
    let showAllHours: Bool
    let userById: (String) -> User?
    let onMeetingTap: (Meeting) -> Void

    var body: some View {
        VStack(spacing: 0) {
            // Day selector chips
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(weekDays, id: \.self) { day in
                        Button {
                            selectedDay = day
                        } label: {
                            VStack(spacing: 2) {
                                Text(shortDayName(day))
                                    .font(.caption)
                                Text("\(dayOfMonth(day))")
                                    .font(.subheadline.bold())
                            }
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .background(
                                Calendar.current.isDate(day, inSameDayAs: selectedDay)
                                    ? (isToday(day) ? Color.blue : Color(.systemGray4))
                                    : .clear,
                                in: RoundedRectangle(cornerRadius: 8)
                            )
                            .foregroundStyle(
                                Calendar.current.isDate(day, inSameDayAs: selectedDay) && isToday(day)
                                    ? .white : .primary
                            )
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.horizontal)
                .padding(.vertical, 8)
            }

            // Single day grid
            WeekGridView(
                weekDays: [selectedDay],
                meetings: meetings,
                availability: availability,
                showAllHours: showAllHours,
                userById: userById,
                onMeetingTap: onMeetingTap
            )
        }
    }
}

// MARK: - Agenda View

private struct AgendaListView: View {
    let weekDays: [Date]
    let meetings: [Meeting]
    let availability: [TimeSlot]
    let userById: (String) -> User?
    let onMeetingTap: (Meeting) -> Void

    private var meetingsByDay: [(String, [Meeting])] {
        let weekDateStrings = Set(weekDays.map { toIsoString($0) })
        let filtered = meetings.filter { weekDateStrings.contains($0.date) }
        let grouped = Dictionary(grouping: filtered) { $0.date }
        return grouped.sorted { $0.key < $1.key }
    }

    var body: some View {
        if meetingsByDay.isEmpty {
            ContentUnavailableView(
                "No Meetings",
                systemImage: "calendar.badge.exclamationmark",
                description: Text("No meetings scheduled this week")
            )
        } else {
            List {
                ForEach(meetingsByDay, id: \.0) { dateStr, dayMeetings in
                    Section {
                        ForEach(dayMeetings.sorted(by: { $0.startHour < $1.startHour })) { meeting in
                            AgendaMeetingRow(
                                meeting: meeting,
                                userById: userById,
                                onTap: { onMeetingTap(meeting) }
                            )
                        }
                    } header: {
                        if let date = fromIsoString(dateStr) {
                            HStack {
                                Text(formatDateFull(date))
                                    .font(.subheadline.weight(.semibold))
                                Spacer()
                                let dayAvail = availability.filter { $0.date == dateStr }
                                let hours = dayAvail.reduce(0.0) { $0 + ($1.endHour - $1.startHour) }
                                if hours > 0 {
                                    Text("\(Int(hours))h available")
                                        .font(.caption)
                                        .foregroundStyle(.blue)
                                }
                            }
                        }
                    }
                }
            }
            .listStyle(.insetGrouped)
        }
    }
}

private struct AgendaMeetingRow: View {
    let meeting: Meeting
    let userById: (String) -> User?
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 12) {
                RoundedRectangle(cornerRadius: 2)
                    .fill(colorFromHex(
                        (userById(meeting.participantId) ?? userById(meeting.organizerId))?.avatarColor ?? "#3B82F6"
                    ))
                    .frame(width: 4, height: 44)

                VStack(alignment: .leading, spacing: 2) {
                    Text(meeting.title)
                        .font(.subheadline.weight(.medium))
                    Text(formatTimeRange(meeting.startHour, meeting.endHour))
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }

                Spacer()

                if let other = userById(meeting.participantId) ?? userById(meeting.organizerId) {
                    UserAvatar(user: other, size: 28)
                }
            }
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Meeting Detail Sheet

private struct MeetingDetailSheet: View {
    let meeting: Meeting
    let currentUserId: String
    let userById: (String) -> User?
    let onCancel: () -> Void
    let onDismiss: () -> Void

    private var isOrganizer: Bool { meeting.organizerId == currentUserId }

    var body: some View {
        NavigationStack {
            List {
                Section {
                    Label {
                        Text(formatTimeRange(meeting.startHour, meeting.endHour))
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
