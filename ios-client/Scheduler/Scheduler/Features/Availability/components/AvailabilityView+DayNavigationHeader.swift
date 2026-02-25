import SwiftUI

extension AvailabilityView {
    struct DayNavigationHeader: View {
        let currentDay: Date
        let currentDaySlots: [TimeSlot]
        let canGoBack: Bool
        let canGoForward: Bool
        let onPrevious: () -> Void
        let onNext: () -> Void

        var body: some View {
            let currentDayHours = getTotalAvailableHours(currentDaySlots)

            HStack {
                Button(action: onPrevious) {
                    Image(systemName: "chevron.left")
                        .font(.title2)
                        .foregroundStyle(canGoBack ? .primary : .tertiary)
                }
                .disabled(!canGoBack)
                .accessibilityLabel("Previous day")

                Spacer()

                VStack(spacing: 2) {
                    Text(fullDayName(currentDay))
                        .font(.headline)
                    Text("\(formatDateRelative(currentDay)) • \(currentDayHours > 0 ? "\(Int(currentDayHours))h selected" : "No availability")")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }

                Spacer()

                Button(action: onNext) {
                    Image(systemName: "chevron.right")
                        .font(.title2)
                        .foregroundStyle(canGoForward ? .primary : .tertiary)
                }
                .disabled(!canGoForward)
                .accessibilityLabel("Next day")
            }
            .padding(.horizontal)
            .padding(.vertical, 8)
        }
    }
}

private func fullDayName(_ date: Date) -> String {
    let f = DateFormatter()
    f.dateFormat = "EEEE"
    return f.string(from: date)
}
