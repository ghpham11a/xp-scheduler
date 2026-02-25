import SwiftUI

extension CalendarView {
    struct HeaderSection: View {
        let showMonthView: Bool
        let monthOffset: Int
        let weekRangeLabel: String
        let meetingCount: Int
        let onPreviousMonth: () -> Void
        let onNextMonth: () -> Void
        let onToggleView: () -> Void
        let monthLabel: String

        var body: some View {
            HStack {
                if showMonthView {
                    HStack(spacing: 12) {
                        Button(action: onPreviousMonth) {
                            Image(systemName: "chevron.left")
                        }
                        .accessibilityLabel("Previous month")
                        Text(monthLabel)
                            .font(.title3.weight(.semibold))
                            .frame(minWidth: 140)
                        Button(action: onNextMonth) {
                            Image(systemName: "chevron.right")
                        }
                        .accessibilityLabel("Next month")
                    }
                } else {
                    VStack(alignment: .leading, spacing: 2) {
                        Text(weekRangeLabel)
                            .font(.title3.weight(.semibold))
                        Text("\(meetingCount) meeting\(meetingCount == 1 ? "" : "s") today")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }
                Spacer()
                Button(action: onToggleView) {
                    Image(systemName: showMonthView ? "calendar.day.timeline.left" : "calendar")
                        .font(.title3)
                        .foregroundStyle(.blue)
                }
                .accessibilityLabel(showMonthView ? "Switch to week view" : "Switch to month view")
            }
            .padding(.horizontal)
            .padding(.top, 8)
            .padding(.bottom, 4)
        }
    }
}
