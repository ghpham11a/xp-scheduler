import SwiftUI

extension AvailabilityView {
    struct Legend: View {
        var body: some View {
            HStack(spacing: 24) {
                HStack(spacing: 6) {
                    RoundedRectangle(cornerRadius: 4)
                        .fill(.blue)
                        .frame(width: 16, height: 16)
                    Text("Available")
                        .font(.caption)
                }
                HStack(spacing: 6) {
                    RoundedRectangle(cornerRadius: 4)
                        .fill(Color(.systemGray5))
                        .frame(width: 16, height: 16)
                    Text("Unavailable")
                        .font(.caption)
                }
            }
            .padding(.vertical, 12)
            .accessibilityElement(children: .ignore)
            .accessibilityLabel("Legend: blue means available, gray means unavailable")
        }
    }
}
