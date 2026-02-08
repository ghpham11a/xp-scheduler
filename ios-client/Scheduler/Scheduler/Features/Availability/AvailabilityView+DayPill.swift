import SwiftUI

extension AvailabilityView {
    struct DayPill: View {
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
}
