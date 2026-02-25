import SwiftUI

extension CalendarView {
    struct AgendaSection: View {
        let meetings: [Meeting]
        let selectedDay: Date
        let use24HourTime: Bool
        let userById: (String) -> User?
        let onMeetingTap: (Meeting) -> Void

        var body: some View {
            if meetings.isEmpty {
                ContentUnavailableView(
                    "No Meetings",
                    systemImage: "calendar",
                    description: Text("No meetings scheduled for \(formatDateRelative(selectedDay))")
                )
            } else {
                List {
                    ForEach(meetings) { meeting in
                        MeetingRow(
                            meeting: meeting,
                            use24HourTime: use24HourTime,
                            userById: userById,
                            onTap: { onMeetingTap(meeting) }
                        )
                    }
                }
                .listStyle(.insetGrouped)
            }
        }
    }
}
