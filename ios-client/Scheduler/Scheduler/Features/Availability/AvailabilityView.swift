import SwiftUI

struct AvailabilityView: View {

    @State private var viewModel: ViewModel

    init(viewModel: ViewModel) {
        _viewModel = State(initialValue: viewModel)
    }

    @State private var localSlots: [TimeSlot] = []
    @State private var selectedDayIndex: Int = 0

    private let next14Days = getNextDays(14)

    var body: some View {
        VStack(spacing: 0) {
            daySelectorRow
            dayNavigationHeader
            verticalTimeBlocks
            legend
        }
        .onAppear { localSlots = viewModel.availabilitySlots }
        .onChange(of: viewModel.availabilitySlots) { _, newValue in localSlots = newValue }
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
                        isHourMark: isHourMark,
                        use24HourTime: viewModel.use24HourTime
                    ) {
                        let newSlots: [TimeSlot]
                        if isAvailable {
                            newSlots = removeTimeBlock(currentDaySlots, date: dateStr, hour: hour)
                        } else {
                            newSlots = addTimeBlock(currentDaySlots, date: dateStr, hour: hour)
                        }
                        let merged = mergeTimeSlots(newSlots)
                        localSlots = localSlots.filter { $0.date != dateStr } + merged
                        viewModel.setAvailability(userId: viewModel.currentUser?.id ?? "", slots: mergeTimeSlots(localSlots))
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
    let use24HourTime: Bool
    let onToggle: () -> Void

    var body: some View {
        Button(action: onToggle) {
            HStack {
                Text(formatHour(hour, use24Hour: use24HourTime))
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
