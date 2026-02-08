import Foundation

// MARK: - Time Formatting

func formatHour(_ hour: Double, use24Hour: Bool = false) -> String {
    let hourInt = Int(hour)
    let minutes = Int((hour - Double(hourInt)) * 60)

    if use24Hour {
        if minutes == 0 {
            return String(format: "%02d:00", hourInt)
        }
        return String(format: "%02d:%02d", hourInt, minutes)
    }

    let displayHour: Int
    switch hourInt {
    case 0, 24: displayHour = 12
    case 13...23: displayHour = hourInt - 12
    default: displayHour = hourInt == 0 ? 12 : hourInt
    }
    let amPm = hourInt < 12 ? "AM" : "PM"
    if minutes == 0 {
        return "\(displayHour) \(amPm)"
    }
    return "\(displayHour):\(String(format: "%02d", minutes)) \(amPm)"
}

func formatTimeRange(_ startHour: Double, _ endHour: Double, use24Hour: Bool = false) -> String {
    "\(formatHour(startHour, use24Hour: use24Hour)) - \(formatHour(endHour, use24Hour: use24Hour))"
}

func formatHourCompact(_ hour: Double, use24Hour: Bool = false) -> String {
    let hourInt = Int(hour)

    if use24Hour {
        return "\(hourInt)"
    }

    let displayHour: Int
    switch hourInt {
    case 0, 12, 24: displayHour = 12
    case 13...23: displayHour = hourInt - 12
    default: displayHour = hourInt
    }
    let suffix = hourInt < 12 ? "a" : "p"
    return "\(displayHour)\(suffix)"
}

// MARK: - Date Helpers

let isoFormatter: DateFormatter = {
    let f = DateFormatter()
    f.dateFormat = "yyyy-MM-dd"
    f.locale = Locale(identifier: "en_US_POSIX")
    return f
}()

func toIsoString(_ date: Date) -> String {
    isoFormatter.string(from: date)
}

func fromIsoString(_ string: String) -> Date? {
    isoFormatter.date(from: string)
}

func getNextDays(_ count: Int) -> [Date] {
    let today = Calendar.current.startOfDay(for: Date())
    return (0..<count).compactMap { Calendar.current.date(byAdding: .day, value: $0, to: today) }
}

func shortDayName(_ date: Date) -> String {
    let f = DateFormatter()
    f.dateFormat = "EEE"
    return f.string(from: date)
}

func dayOfMonth(_ date: Date) -> Int {
    Calendar.current.component(.day, from: date)
}

func isToday(_ date: Date) -> Bool {
    Calendar.current.isDateInToday(date)
}

func isTomorrow(_ date: Date) -> Bool {
    Calendar.current.isDateInTomorrow(date)
}

func formatDateRelative(_ date: Date) -> String {
    if isToday(date) { return "Today" }
    if isTomorrow(date) { return "Tomorrow" }
    return formatDateShort(date)
}

func formatDateShort(_ date: Date) -> String {
    "\(shortDayName(date)) \(dayOfMonth(date))"
}

func formatDateFull(_ date: Date) -> String {
    let f = DateFormatter()
    f.dateFormat = "EEEE, MMMM d"
    return f.string(from: date)
}

func getWeekStart(_ date: Date) -> Date {
    let cal = Calendar.current
    let weekday = cal.component(.weekday, from: date) // 1=Sun
    return cal.date(byAdding: .day, value: -(weekday - 1), to: cal.startOfDay(for: date))!
}

// MARK: - Slot Helpers

func generateHours(start: Int = 6, end: Int = 22) -> [Double] {
    var hours: [Double] = []
    var h = Double(start)
    while h < Double(end) {
        hours.append(h)
        h += 0.5
    }
    return hours
}

func mergeTimeSlots(_ slots: [TimeSlot]) -> [TimeSlot] {
    guard !slots.isEmpty else { return [] }
    let byDate = Dictionary(grouping: slots) { $0.date }
    return byDate.flatMap { (date, dateSlots) -> [TimeSlot] in
        let sorted = dateSlots.sorted { $0.startHour < $1.startHour }
        var merged: [TimeSlot] = []
        for slot in sorted {
            if merged.isEmpty {
                merged.append(slot)
            } else {
                let last = merged[merged.count - 1]
                if last.endHour >= slot.startHour {
                    merged[merged.count - 1] = TimeSlot(
                        date: date,
                        startHour: last.startHour,
                        endHour: max(last.endHour, slot.endHour)
                    )
                } else {
                    merged.append(slot)
                }
            }
        }
        return merged
    }
}

func isHourInSlots(date: String, hour: Double, slots: [TimeSlot]) -> Bool {
    slots.contains { $0.date == date && hour >= $0.startHour && hour < $0.endHour }
}

func addTimeBlock(_ slots: [TimeSlot], date: String, hour: Double) -> [TimeSlot] {
    slots + [TimeSlot(date: date, startHour: hour, endHour: hour + 0.5)]
}

func removeTimeBlock(_ slots: [TimeSlot], date: String, hour: Double) -> [TimeSlot] {
    var result: [TimeSlot] = []
    for slot in slots {
        if slot.date != date {
            result.append(slot)
            continue
        }
        if hour >= slot.startHour && hour < slot.endHour {
            if hour > slot.startHour {
                result.append(TimeSlot(date: date, startHour: slot.startHour, endHour: hour))
            }
            if hour + 0.5 < slot.endHour {
                result.append(TimeSlot(date: date, startHour: hour + 0.5, endHour: slot.endHour))
            }
        } else {
            result.append(slot)
        }
    }
    return result
}

func getTotalAvailableHours(_ slots: [TimeSlot]) -> Double {
    slots.reduce(0) { $0 + ($1.endHour - $1.startHour) }
}

func getDaysWithAvailability(_ slots: [TimeSlot]) -> Int {
    Set(slots.map(\.date)).count
}

func hasConflict(date: String, startHour: Double, endHour: Double, meetings: [Meeting], userId: String) -> Bool {
    meetings.contains { meeting in
        meeting.date == date &&
        (meeting.organizerId == userId || meeting.participantId == userId) &&
        startHour < meeting.endHour &&
        endHour > meeting.startHour
    }
}

func getMeetingsForUser(_ meetings: [Meeting], userId: String) -> [Meeting] {
    meetings.filter { $0.organizerId == userId || $0.participantId == userId }
}

func getUserInitials(_ name: String) -> String {
    let parts = name.split(separator: " ")
    let initials = parts.prefix(2).compactMap(\.first).map { String($0).uppercased() }
    return initials.joined()
}

func findAvailableSlots(
    dateStr: String,
    participantSlots: [TimeSlot],
    currentUserSlots: [TimeSlot],
    duration: Double,
    meetings: [Meeting],
    currentUserId: String,
    participantId: String
) -> [Double] {
    var available: [Double] = []
    var hour = 6.0
    while hour + duration <= 22.0 {
        let endHour = hour + duration
        let participantAvailable = participantSlots.contains { hour >= $0.startHour && endHour <= $0.endHour }
        let currentUserAvailable = currentUserSlots.contains { hour >= $0.startHour && endHour <= $0.endHour }
        let currentConflict = hasConflict(date: dateStr, startHour: hour, endHour: endHour, meetings: meetings, userId: currentUserId)
        let participantConflict = hasConflict(date: dateStr, startHour: hour, endHour: endHour, meetings: meetings, userId: participantId)
        if participantAvailable && currentUserAvailable && !currentConflict && !participantConflict {
            available.append(hour)
        }
        hour += 0.5
    }
    return available
}

// MARK: - Enums

enum AvailabilityPreset: CaseIterable {
    case fullDay, business, morning, afternoon, evening

    var displayName: String {
        switch self {
        case .fullDay: "All Day"
        case .business: "9-5"
        case .morning: "Morning"
        case .afternoon: "Afternoon"
        case .evening: "Evening"
        }
    }

    var startHour: Double {
        switch self {
        case .fullDay: 6.0
        case .business: 9.0
        case .morning: 6.0
        case .afternoon: 12.0
        case .evening: 18.0
        }
    }

    var endHour: Double {
        switch self {
        case .fullDay: 22.0
        case .business: 17.0
        case .morning: 12.0
        case .afternoon: 18.0
        case .evening: 22.0
        }
    }
}

enum MeetingDuration: CaseIterable {
    case fifteenMin, thirtyMin, fortyFiveMin, oneHour, ninetyMin, twoHours

    var displayName: String {
        switch self {
        case .fifteenMin: "15 min"
        case .thirtyMin: "30 min"
        case .fortyFiveMin: "45 min"
        case .oneHour: "1 hour"
        case .ninetyMin: "90 min"
        case .twoHours: "2 hours"
        }
    }

    var hours: Double {
        switch self {
        case .fifteenMin: 0.25
        case .thirtyMin: 0.5
        case .fortyFiveMin: 0.75
        case .oneHour: 1.0
        case .ninetyMin: 1.5
        case .twoHours: 2.0
        }
    }
}
