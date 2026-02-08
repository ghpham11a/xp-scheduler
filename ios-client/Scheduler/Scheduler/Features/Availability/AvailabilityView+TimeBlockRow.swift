import SwiftUI

extension AvailabilityView {
    struct TimeBlockRow: View {
        let hour: Double
        let isAvailable: Bool
        let isHourMark: Bool
        let use24HourTime: Bool
        let onToggle: () -> Void

        var body: some View {
            Button(action: onToggle) {
                HStack {
                    Text(formatHour(hour, use24Hour: use24HourTime))
                        .font(.subheadline)
                        .fontWeight(isHourMark ? .medium : .regular)
                        .foregroundStyle(
                            isAvailable ? .white :
                            isHourMark ? .primary :
                            .secondary.opacity(0.6)
                        )
                        .frame(width: 72, alignment: .leading)

                    Spacer()

                    ZStack {
                        Circle()
                            .stroke(isAvailable ? .white : Color(.systemGray3), lineWidth: 2)
                            .frame(width: 24, height: 24)
                        if isAvailable {
                            Circle()
                                .fill(.white.opacity(0.2))
                                .frame(width: 24, height: 24)
                            Image(systemName: "checkmark")
                                .font(.caption.bold())
                                .foregroundStyle(.white)
                        }
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
                .background(isAvailable ? Color.blue : Color.clear)
            }
            .buttonStyle(.plain)
        }
    }
}
