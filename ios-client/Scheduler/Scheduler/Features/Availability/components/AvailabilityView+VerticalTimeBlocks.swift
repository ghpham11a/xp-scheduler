import SwiftUI

extension AvailabilityView {
    struct VerticalTimeBlocks: View {
        let dateStr: String
        let localSlots: [TimeSlot]
        let use24HourTime: Bool
        let onToggleHour: (Double, Bool) -> Void

        private var currentDaySlots: [TimeSlot] {
            localSlots.filter { $0.date == dateStr }
        }

        var body: some View {
            let timeBlocks = generateHours(start: 0, end: 24)

            ScrollView {
                VStack(spacing: 0) {
                    ForEach(timeBlocks, id: \.self) { hour in
                        let isAvailable = isHourInSlots(date: dateStr, hour: hour, slots: currentDaySlots)
                        let isHourMark = hour.truncatingRemainder(dividingBy: 1) == 0

                        TimeBlockRow(
                            hour: hour,
                            isAvailable: isAvailable,
                            isHourMark: isHourMark,
                            use24HourTime: use24HourTime
                        ) {
                            onToggleHour(hour, isAvailable)
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
    }
}
