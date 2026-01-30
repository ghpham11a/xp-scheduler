import SwiftUI

struct AvailabilityView: View {
    let currentUser: User?
    let availabilitySlots: [TimeSlot]
    let onUpdateAvailability: ([TimeSlot]) -> Void

    @State private var localSlots: [TimeSlot] = []
    @State private var selectedDayIndex: Int = 0

    private let next14Days = getNextDays(14)

    var body: some View {
        VStack(spacing: 0) {
            availabilityHeader
            daySelectorRow
            dayNavigationHeader
            quickActionButtons
            verticalTimeBlocks
            legend
        }
        .onAppear { localSlots = availabilitySlots }
        .onChange(of: availabilitySlots) { _, newValue in localSlots = newValue }
    }

    // MARK: - Header

    private var availabilityHeader: some View {
        let totalHours = getTotalAvailableHours(localSlots)
        let daysWithAvailability = getDaysWithAvailability(localSlots)

        return HStack(spacing: 16) {
            if let currentUser {
                UserAvatar(user: currentUser, size: 44)
            }

            VStack(alignment: .leading, spacing: 2) {
                Text(currentUser?.name ?? "Unknown User")
                    .font(.headline)
                Text("Set your availability for the next 2 weeks")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }

            Spacer()

            VStack(alignment: .trailing) {
                Text("\(Int(totalHours))h")
                    .font(.title2.bold())
                    .foregroundStyle(.blue)
                Text("\(daysWithAvailability) days")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
        }
        .padding()
        .background(.regularMaterial, in: RoundedRectangle(cornerRadius: 12))
        .padding(.horizontal)
        .padding(.top, 8)
    }

    // MARK: - Day Selector Row

    private var daySelectorRow: some View {
        ScrollViewReader { proxy in
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(Array(next14Days.enumerated()), id: \.element) { index, date in
                        let isSelected = index == selectedDayIndex
                        let today = isToday(date)
                        let dayHours = getTotalAvailableHours(localSlots.filter { $0.date == toIsoString(date) })

                        DayPill(
                            date: date,
                            isSelected: isSelected,
                            isToday: today,
                            hasAvailability: dayHours > 0
                        )
                        .id(index)
                        .onTapGesture {
                            withAnimation(.easeInOut(duration: 0.2)) {
                                selectedDayIndex = index
                            }
                            withAnimation {
                                proxy.scrollTo(max(0, index - 2), anchor: .leading)
                            }
                        }
                    }
                }
                .padding(.horizontal, 16)
            }
            .padding(.vertical, 8)
        }
    }

    // MARK: - Day Navigation Header

    private var dayNavigationHeader: some View {
        let currentDay = next14Days[selectedDayIndex]
        let currentDaySlots = localSlots.filter { $0.date == toIsoString(currentDay) }
        let currentDayHours = getTotalAvailableHours(currentDaySlots)

        return HStack {
            Button {
                if selectedDayIndex > 0 {
                    withAnimation { selectedDayIndex -= 1 }
                }
            } label: {
                Image(systemName: "chevron.left")
                    .font(.title2)
                    .foregroundStyle(selectedDayIndex > 0 ? .primary : .tertiary)
            }
            .disabled(selectedDayIndex == 0)

            Spacer()

            VStack(spacing: 2) {
                Text(fullDayName(currentDay))
                    .font(.headline)
                Text("\(formatDateRelative(currentDay)) • \(currentDayHours > 0 ? "\(Int(currentDayHours))h selected" : "No availability")")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }

            Spacer()

            Button {
                if selectedDayIndex < next14Days.count - 1 {
                    withAnimation { selectedDayIndex += 1 }
                }
            } label: {
                Image(systemName: "chevron.right")
                    .font(.title2)
                    .foregroundStyle(selectedDayIndex < next14Days.count - 1 ? .primary : .tertiary)
            }
            .disabled(selectedDayIndex == next14Days.count - 1)
        }
        .padding(.horizontal)
        .padding(.vertical, 8)
    }

    // MARK: - Quick Action Buttons

    private var quickActionButtons: some View {
        let currentDay = next14Days[selectedDayIndex]
        let dateStr = toIsoString(currentDay)

        return ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(AvailabilityPreset.allCases, id: \.self) { preset in
                    Button {
                        localSlots = localSlots.filter { $0.date != dateStr }
                        localSlots.append(TimeSlot(date: dateStr, startHour: preset.startHour, endHour: preset.endHour))
                        let merged = mergeTimeSlots(localSlots)
                        localSlots = merged
                        onUpdateAvailability(merged)
                    } label: {
                        Text(preset.displayName)
                            .font(.subheadline)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                    }
                    .buttonStyle(.bordered)
                    .tint(preset == .fullDay ? .blue : .secondary)
                }

                Button {
                    localSlots = localSlots.filter { $0.date != dateStr }
                    onUpdateAvailability(localSlots)
                } label: {
                    Text("Clear")
                        .font(.subheadline)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 6)
                }
                .buttonStyle(.bordered)
                .tint(.red)
            }
            .padding(.horizontal, 16)
        }
        .padding(.vertical, 8)
    }

    // MARK: - Vertical Time Blocks

    private var verticalTimeBlocks: some View {
        let currentDay = next14Days[selectedDayIndex]
        let dateStr = toIsoString(currentDay)
        let currentDaySlots = localSlots.filter { $0.date == dateStr }
        let timeBlocks = generateHours(start: 0, end: 24)

        return ScrollView {
            VStack(spacing: 0) {
                ForEach(timeBlocks, id: \.self) { hour in
                    let isAvailable = isHourInSlots(date: dateStr, hour: hour, slots: currentDaySlots)
                    let isHourMark = hour.truncatingRemainder(dividingBy: 1) == 0

                    TimeBlockRow(
                        hour: hour,
                        isAvailable: isAvailable,
                        isHourMark: isHourMark
                    ) {
                        let newSlots: [TimeSlot]
                        if isAvailable {
                            newSlots = removeTimeBlock(currentDaySlots, date: dateStr, hour: hour)
                        } else {
                            newSlots = addTimeBlock(currentDaySlots, date: dateStr, hour: hour)
                        }
                        let merged = mergeTimeSlots(newSlots)
                        localSlots = localSlots.filter { $0.date != dateStr } + merged
                        onUpdateAvailability(mergeTimeSlots(localSlots))
                    }

                    if hour < 23.5 {
                        Divider()
                            .opacity(0.5)
                    }
                }
            }
            .padding(.horizontal, 16)
        }
        .background(Color(.systemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(Color(.systemGray4), lineWidth: 1)
        )
        .padding(.horizontal, 16)
    }

    // MARK: - Legend

    private var legend: some View {
        HStack(spacing: 24) {
            HStack(spacing: 6) {
                RoundedRectangle(cornerRadius: 4)
                    .fill(.blue)
                    .frame(width: 16, height: 16)
                Text("Available")
                    .font(.caption)
            }
            HStack(spacing: 6) {
                RoundedRectangle(cornerRadius: 4)
                    .fill(Color(.systemGray5))
                    .frame(width: 16, height: 16)
                Text("Unavailable")
                    .font(.caption)
            }
        }
        .padding(.vertical, 12)
    }
}

// MARK: - Day Pill

private struct DayPill: View {
    let date: Date
    let isSelected: Bool
    let isToday: Bool
    let hasAvailability: Bool

    var body: some View {
        VStack(spacing: 2) {
            Text(shortDayName(date))
                .font(.caption2)
                .fontWeight(.medium)
            Text("\(dayOfMonth(date))")
                .font(.callout.bold())
            if hasAvailability {
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

// MARK: - Time Block Row

private struct TimeBlockRow: View {
    let hour: Double
    let isAvailable: Bool
    let isHourMark: Bool
    let onToggle: () -> Void

    var body: some View {
        Button(action: onToggle) {
            HStack {
                Text(formatHour(hour))
                    .font(.subheadline)
                    .fontWeight(isHourMark ? .medium : .regular)
                    .foregroundStyle(
                        isAvailable ? .white :
                        isHourMark ? .primary :
                        .secondary.opacity(0.6)
                    )
                    .frame(width: 72, alignment: .leading)

                Spacer()

                ZStack {
                    Circle()
                        .stroke(isAvailable ? .white : Color(.systemGray3), lineWidth: 2)
                        .frame(width: 24, height: 24)
                    if isAvailable {
                        Circle()
                            .fill(.white.opacity(0.2))
                            .frame(width: 24, height: 24)
                        Image(systemName: "checkmark")
                            .font(.caption.bold())
                            .foregroundStyle(.white)
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(isAvailable ? Color.blue : Color.clear)
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Helper

private func fullDayName(_ date: Date) -> String {
    let f = DateFormatter()
    f.dateFormat = "EEEE"
    return f.string(from: date)
}
