import SwiftUI

extension AvailabilityView {
    struct DaySelectorRow: View {
        let days: [Date]
        let selectedDayIndex: Int
        let localSlots: [TimeSlot]
        let onSelectDay: (Int) -> Void

        var body: some View {
            ScrollViewReader { proxy in
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(Array(days.enumerated()), id: \.element) { index, date in
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
                                onSelectDay(index)
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
    }
}
