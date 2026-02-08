import SwiftUI

extension CalendarView {
    struct MonthAgendaView: View {
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
}
