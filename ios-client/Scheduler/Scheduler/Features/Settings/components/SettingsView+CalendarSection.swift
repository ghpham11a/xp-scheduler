import SwiftUI

extension SettingsView {
    struct CalendarSection: View {
        @Binding var use24HourTime: Bool

        var body: some View {
            Section("Calendar") {
                Toggle("24-Hour Time", isOn: $use24HourTime)
            }
        }
    }
}
