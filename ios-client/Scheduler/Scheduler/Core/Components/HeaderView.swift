import SwiftUI

struct HeaderView: View {
    var body: some View {
        HStack {
            HStack(spacing: 8) {
                RoundedRectangle(cornerRadius: 8)
                    .fill(.blue)
                    .frame(width: 32, height: 32)
                    .overlay {
                        Text("S")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundStyle(.white)
                    }
                Text("XP Scheduler")
                    .font(.headline)
            }

            Spacer()
        }
        .padding(.horizontal)
        .padding(.vertical, 8)
    }
}
