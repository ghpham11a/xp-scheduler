import SwiftUI

struct UserAvatar: View {
    let user: User
    var size: CGFloat = 32

    var body: some View {
        Circle()
            .fill(colorFromHex(user.avatarColor))
            .frame(width: size, height: size)
            .overlay {
                Text(getUserInitials(user.name))
                    .font(.system(size: size * 0.4, weight: .medium))
                    .foregroundStyle(.white)
            }
    }
}

func colorFromHex(_ hex: String) -> Color {
    var hexStr = hex.trimmingCharacters(in: .whitespacesAndNewlines)
    if hexStr.hasPrefix("#") { hexStr.removeFirst() }

    guard hexStr.count == 6, let rgb = UInt64(hexStr, radix: 16) else {
        return .blue
    }

    return Color(
        red: Double((rgb >> 16) & 0xFF) / 255,
        green: Double((rgb >> 8) & 0xFF) / 255,
        blue: Double(rgb & 0xFF) / 255
    )
}
