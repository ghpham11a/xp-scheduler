import SwiftUI

extension CalendarView {
    struct DaySelectorRow: View {
        let weekDays: [Date]
        let selectedDayIndex: Int
        let currentUserMeetings: [Meeting]
        let onSelectDay: (Int) -> Void

        var body: some View {
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
                                onSelectDay(index)
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
