import SwiftUI

extension ScheduleView {
    struct WizardProgress: View {
        let onBack: () -> Void
        let onCancel: () -> Void

        var body: some View {
            HStack {
                Button(action: onBack) {
                    Image(systemName: "chevron.left")
                        .font(.body.weight(.medium))
                }
                .accessibilityLabel("Go back")

                Spacer()

                Button("Cancel", action: onCancel)
                    .font(.subheadline)
            }
            .padding(.horizontal)
            .padding(.vertical, 10)
        }
    }
}
