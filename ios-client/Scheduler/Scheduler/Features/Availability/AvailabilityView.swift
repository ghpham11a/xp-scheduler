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

// MARK: - Helper

private func fullDayName(_ date: Date) -> String {
    let f = DateFormatter()
    f.dateFormat = "EEEE"
    return f.string(from: date)
}
