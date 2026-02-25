import SwiftUI

struct AvailabilityView: View {

    @Environment(RouteManager.self) private var routeManager

    @State private var viewModel: AvailabilityViewModel

    init(viewModel: AvailabilityViewModel) {
        _viewModel = State(initialValue: viewModel)
    }

    @State private var localSlots: [TimeSlot] = []
    @State private var selectedDayIndex: Int = 0

    private let next14Days = getNextDays(14)

    var body: some View {
        VStack(spacing: 0) {
            DaySelectorRow(
                days: next14Days,
                selectedDayIndex: selectedDayIndex,
                localSlots: localSlots,
                onSelectDay: { index in
                    withAnimation(.easeInOut(duration: 0.2)) {
                        selectedDayIndex = index
                    }
                }
            )
            DayNavigationHeader(
                currentDay: next14Days[selectedDayIndex],
                currentDaySlots: localSlots.filter { $0.date == toIsoString(next14Days[selectedDayIndex]) },
                canGoBack: selectedDayIndex > 0,
                canGoForward: selectedDayIndex < next14Days.count - 1,
                onPrevious: {
                    if selectedDayIndex > 0 {
                        withAnimation { selectedDayIndex -= 1 }
                    }
                },
                onNext: {
                    if selectedDayIndex < next14Days.count - 1 {
                        withAnimation { selectedDayIndex += 1 }
                    }
                }
            )
            VerticalTimeBlocks(
                dateStr: toIsoString(next14Days[selectedDayIndex]),
                localSlots: localSlots,
                use24HourTime: viewModel.use24HourTime,
                onToggleHour: { hour, isAvailable in
                    let dateStr = toIsoString(next14Days[selectedDayIndex])
                    let currentDaySlots = localSlots.filter { $0.date == dateStr }
                    let newSlots: [TimeSlot]
                    if isAvailable {
                        newSlots = removeTimeBlock(currentDaySlots, date: dateStr, hour: hour)
                    } else {
                        newSlots = addTimeBlock(currentDaySlots, date: dateStr, hour: hour)
                    }
                    let merged = mergeTimeSlots(newSlots)
                    localSlots = localSlots.filter { $0.date != dateStr } + merged
                    viewModel.setAvailability(userId: viewModel.currentUser?.id ?? "", slots: mergeTimeSlots(localSlots))
                }
            )
            Legend()
        }
        .onAppear { localSlots = viewModel.availabilitySlots }
        .onChange(of: viewModel.availabilitySlots) { _, newValue in localSlots = newValue }
        .onChange(of: routeManager.pendingDeepLink) { _, newValue in
            guard case .availabilityDate(let date) = newValue else { return }
            if let index = next14Days.firstIndex(where: { toIsoString($0) == date }) {
                withAnimation { selectedDayIndex = index }
            }
            routeManager.pendingDeepLink = nil
        }
    }
}
