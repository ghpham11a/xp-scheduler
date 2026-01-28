import SwiftUI

struct AvailabilityView: View {
    let currentUser: User?
    let availabilitySlots: [TimeSlot]
    let onUpdateAvailability: ([TimeSlot]) -> Void

    @State private var localSlots: [TimeSlot] = []

    var body: some View {
        VStack(spacing: 0) {
            availabilityHeader
            legendRow
            availabilityGrid
        }
        .onAppear { localSlots = availabilitySlots }
        .onChange(of: availabilitySlots) { _, newValue in localSlots = newValue }
    }

    // MARK: - Header

    private var availabilityHeader: some View {
        HStack(spacing: 16) {
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
                Text("\(Int(getTotalAvailableHours(localSlots)))h")
                    .font(.title2.bold())
                    .foregroundStyle(.blue)
                Text("\(getDaysWithAvailability(localSlots)) days")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
        }
        .padding()
        .background(.regularMaterial, in: RoundedRectangle(cornerRadius: 12))
        .padding(.horizontal)
        .padding(.top, 8)
    }

    // MARK: - Legend

    private var legendRow: some View {
        HStack(spacing: 24) {
            HStack(spacing: 6) {
                RoundedRectangle(cornerRadius: 3)
                    .fill(.blue)
                    .frame(width: 14, height: 14)
                Text("Available")
                    .font(.caption)
            }
            HStack(spacing: 6) {
                RoundedRectangle(cornerRadius: 3)
                    .fill(Color(.systemGray5))
                    .frame(width: 14, height: 14)
                Text("Unavailable")
                    .font(.caption)
            }
        }
        .padding(.vertical, 8)
    }

    // MARK: - Grid

    private var availabilityGrid: some View {
        let days = getNextDays(14)

        return ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 4) {
                ForEach(days, id: \.self) { date in
                    DayColumn(
                        date: date,
                        slots: localSlots.filter { $0.date == toIsoString(date) },
                        onSlotsChanged: { newSlots in
                            let dateStr = toIsoString(date)
                            localSlots = localSlots.filter { $0.date != dateStr } + newSlots
                            let merged = mergeTimeSlots(localSlots)
                            localSlots = merged
                            onUpdateAvailability(merged)
                        }
                    )
                }
            }
            .padding(.horizontal, 8)
        }
    }
}

// MARK: - Day Column

private struct DayColumn: View {
    let date: Date
    let slots: [TimeSlot]
    let onSlotsChanged: ([TimeSlot]) -> Void

    private let timeBlocks = generateHours(start: 6, end: 22)

    var body: some View {
        let dateStr = toIsoString(date)
        let dayHours = slots.reduce(0.0) { $0 + ($1.endHour - $1.startHour) }
        let today = isToday(date)

        VStack(spacing: 0) {
            // Day header
            VStack(spacing: 2) {
                Text(shortDayName(date))
                    .font(.caption2)
                    .fontWeight(today ? .bold : .regular)
                Text("\(dayOfMonth(date))")
                    .font(.subheadline.bold())
                if dayHours > 0 {
                    Text("\(Int(dayHours))h")
                        .font(.caption2)
                        .foregroundStyle(.blue)
                }
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 6)
            .background(today ? Color.blue.opacity(0.15) : Color(.systemGray6))

            // Preset buttons
            VStack(spacing: 2) {
                HStack(spacing: 2) {
                    PresetButton("9-5") {
                        onSlotsChanged([TimeSlot(date: dateStr, startHour: 9, endHour: 17)])
                    }
                    PresetButton("AM") {
                        onSlotsChanged([TimeSlot(date: dateStr, startHour: 6, endHour: 12)])
                    }
                }
                HStack(spacing: 2) {
                    PresetButton("PM") {
                        onSlotsChanged([TimeSlot(date: dateStr, startHour: 12, endHour: 18)])
                    }
                    PresetButton("Eve") {
                        onSlotsChanged([TimeSlot(date: dateStr, startHour: 18, endHour: 22)])
                    }
                }
                Button {
                    onSlotsChanged([])
                } label: {
                    Image(systemName: "xmark")
                        .font(.caption2)
                        .foregroundStyle(.red)
                }
                .frame(height: 20)
            }
            .padding(.vertical, 4)

            Divider()

            // Time blocks
            ScrollView(.vertical, showsIndicators: false) {
                VStack(spacing: 1) {
                    ForEach(timeBlocks, id: \.self) { hour in
                        let isAvailable = isHourInSlots(date: dateStr, hour: hour, slots: slots)
                        let isHalf = hour.truncatingRemainder(dividingBy: 1) != 0

                        Button {
                            let newSlots: [TimeSlot]
                            if isAvailable {
                                newSlots = removeTimeBlock(slots, date: dateStr, hour: hour)
                            } else {
                                newSlots = addTimeBlock(slots, date: dateStr, hour: hour)
                            }
                            onSlotsChanged(mergeTimeSlots(newSlots))
                        } label: {
                            ZStack {
                                RoundedRectangle(cornerRadius: 3)
                                    .fill(isAvailable ? .blue : Color(.systemGray5))
                                if !isHalf {
                                    Text(formatHour(hour))
                                        .font(.system(size: 7))
                                        .foregroundStyle(isAvailable ? .white : .secondary)
                                        .lineLimit(1)
                                        .minimumScaleFactor(0.5)
                                }
                            }
                            .frame(height: isHalf ? 16 : 20)
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.horizontal, 3)
            }
        }
        .frame(width: 72)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(
            RoundedRectangle(cornerRadius: 8)
                .stroke(today ? .blue : Color(.systemGray4), lineWidth: today ? 2 : 1)
        )
    }
}

private struct PresetButton: View {
    let label: String
    let action: () -> Void

    init(_ label: String, action: @escaping () -> Void) {
        self.label = label
        self.action = action
    }

    var body: some View {
        Button(action: action) {
            Text(label)
                .font(.system(size: 9))
                .padding(.horizontal, 6)
                .padding(.vertical, 2)
        }
        .buttonStyle(.bordered)
        .controlSize(.mini)
    }
}
