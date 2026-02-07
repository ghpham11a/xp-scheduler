# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
./gradlew assembleDebug           # Build debug APK
./gradlew installDebug            # Install on connected device/emulator
./gradlew test                    # Run unit tests
./gradlew connectedAndroidTest    # Run instrumented tests
```

## Architecture

### Tech Stack
- Kotlin, Jetpack Compose, Material 3
- Hilt for dependency injection
- Retrofit + Moshi for networking
- DataStore for preferences persistence
- StateFlow for reactive state management

### Project Structure

```
app/src/main/java/com/example/scheduler/
├── MainActivity.kt              # Entry point, hosts root SchedulerViewModel
├── SchedulerApplication.kt      # Hilt application class
├── viewmodel/
│   └── SchedulerViewModel.kt    # Root state: users, currentUserId, loading/error
├── features/                    # Each screen has its own ViewModel
│   ├── calendar/
│   │   ├── CalendarScreen.kt
│   │   └── CalendarViewModel.kt
│   ├── availability/
│   │   ├── AvailabilityScreen.kt
│   │   └── AvailabilityViewModel.kt
│   ├── schedule/
│   │   ├── ScheduleScreen.kt
│   │   └── ScheduleViewModel.kt
│   └── settings/
│       ├── SettingsScreen.kt
│       └── SettingsViewModel.kt
├── core/
│   ├── navigation/AppNavigation.kt   # Bottom nav, screen routing
│   └── designsystem/theme/           # Color, Theme, Type
├── shared/components/                # Header, UserAvatar
├── data/
│   ├── models/                       # User, Meeting, TimeSlot, Availability
│   ├── networking/ApiService.kt      # Retrofit API interface
│   └── repositories/SchedulerRepository.kt
├── di/                               # Hilt modules
│   ├── NetworkModule.kt
│   └── DataStoreModule.kt
└── utils/Utils.kt                    # Time formatting, slot logic
```

### ViewModel Architecture

Each screen has its own `@HiltViewModel` that:
- Injects `SchedulerRepository` for API calls
- Manages screen-specific state with `StateFlow`
- Implements optimistic updates with error recovery

The root `SchedulerViewModel` provides shared state (users, currentUserId) that screens access.

```
MainActivity
  └── SchedulerViewModel (root: users, currentUserId, isLoading)
       └── MainScreen
            ├── CalendarScreen + CalendarViewModel
            ├── AvailabilityScreen + AvailabilityViewModel
            ├── ScheduleScreen + ScheduleViewModel
            └── SettingsScreen + SettingsViewModel
```

### API Configuration

The API URL is configured in `app/build.gradle.kts`:
```kotlin
buildConfigField("String", "API_URL", "\"https://your-api.ngrok.io/\"")
```

For emulator connecting to host localhost, use `10.0.2.2:6969`.

### Key Data Types

Time is represented in decimal hours (9.5 = 9:30 AM). Availability uses 30-minute blocks.

```kotlin
data class TimeSlot(
    val date: String,       // ISO date (YYYY-MM-DD)
    val startHour: Double,  // 0-24, supports 0.5 increments
    val endHour: Double
)

data class Meeting(
    val id: String,
    val organizerId: String,
    val participantId: String,
    val date: String,
    val startHour: Double,
    val endHour: Double,
    val title: String
)
```

### State Management Pattern

All ViewModels use optimistic updates:
1. Update local state immediately
2. Call API
3. On error, refetch from API to restore correct state

DataStore persists `currentUserId` across app restarts (handled in `SettingsViewModel`).
