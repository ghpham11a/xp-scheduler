# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Build (from ios-client/)
xcodebuild -project Scheduler/Scheduler.xcodeproj -scheme Scheduler -sdk iphonesimulator build

# Run tests
xcodebuild -project Scheduler/Scheduler.xcodeproj -scheme Scheduler test -sdk iphonesimulator -destination 'platform=iOS Simulator,name=iPhone 16'

# Resolve SPM packages (required after adding dependencies)
xcodebuild -project Scheduler/Scheduler.xcodeproj -scheme Scheduler -resolvePackageDependencies
```

## Architecture

### Dependency Injection (Swinject)

`DependencyContainer` (singleton) registers all dependencies. `SchedulerApp` passes it to `ContentView`, which resolves ViewModels and passes them to each tab view via constructor injection.

Registration scopes:
- `SchedulerRepositoryProtocol` → `.container` (singleton)
- `SharedState` → `.container` (singleton, all ViewModels share one instance)
- ViewModels → transient (new instance per resolution)

### Data Flow

```
View (@State var viewModel: ViewModel)
  → ViewModel (reads/writes SharedState, calls Repository)
    → SchedulerRepository (SchedulerRepositoryProtocol)
      → APIService (static URLSession methods)
        → Backend API
```

### Nested ViewModel Pattern

Each feature has a `View` and a nested `ViewModel` defined via extension in a separate file:

- `CalendarView.swift` + `CalendarView+ViewModel.swift`
- `AvailabilityView.swift` + `AvailabilityView+ViewModel.swift`
- etc.

ViewModels are `@Observable` classes nested inside the View's extension:

```swift
// In FeatureView+ViewModel.swift
extension FeatureView {
    @Observable
    class ViewModel {
        let sharedState: SharedState
        private let repository: SchedulerRepositoryProtocol
        init(sharedState:, repository:) { ... }
    }
}

// In FeatureView.swift
struct FeatureView: View {
    @State private var viewModel: ViewModel
    init(viewModel: ViewModel) {
        _viewModel = State(initialValue: viewModel)
    }
}
```

### SharedState

`SharedState` (`Core/SharedState.swift`) is the `@Observable` singleton holding all cross-cutting data: `users`, `availabilities`, `meetings`, `currentUserId`, `use24HourTime`. ViewModels proxy properties from SharedState and mutate it directly. `currentUserId` and `use24HourTime` are persisted to UserDefaults.

### Optimistic Updates

All mutations update SharedState immediately, then fire the API call in a background `Task`. On API error, `sharedState.fetchData()` is called to resync from the server.

### API Configuration

Base URL is set in `APIService.swift` (`Data/Networking/APIService.swift`). Currently points to an ngrok tunnel. For local development, change to `http://localhost:6969`.

## Key Conventions

- Time is represented as decimal hours (`9.5` = 9:30 AM, `0.5` increments for 30-min blocks)
- Date strings use ISO format `YYYY-MM-DD`
- Helper functions for time/date/slot logic live in `Shared/Utilities/Utils.swift`
- Shared UI components (`UserAvatar`, `HeaderView`) live in `Shared/Views/`
- Models are all `Codable` structs in `Data/Models/Models.swift`
- SPM dependency linking requires both `XCSwiftPackageProductDependency` and `PBXBuildFile` entries in `project.pbxproj`
