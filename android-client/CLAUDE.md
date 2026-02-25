# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
./gradlew assembleDebug           # Build debug APK
./gradlew installDebug            # Install on connected device/emulator
./gradlew test                    # Run unit tests
./gradlew test --tests "ClassName.methodName"  # Run single test
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
├── app/
│   ├── SchedulerApp.kt              # Hilt application class
│   └── MainActivity.kt              # Entry point, hosts MainViewModel
├── core/
│   ├── datastore/
│   │   └── DataStoreModule.kt       # Provides DataStore<Preferences>
│   ├── navigation/
│   │   ├── AppNavigation.kt         # MainScreen, bottom nav, screen routing
│   │   ├── MainViewModel.kt         # Fetches users, exposes AppPreferences
│   │   └── Tabs.kt                  # AppScreen enum (tab definitions)
│   └── networking/
│       └── NetworkModule.kt         # Provides Retrofit, Moshi, OkHttpClient
├── data/
│   ├── model/                       # User, Meeting, TimeSlot, Availability, etc.
│   └── repositories/
│       ├── SchedulerRepository.kt   # Repository with Result-wrapped API calls
│       └── SchedulerEndpoints.kt    # SchedulerApi interface (Retrofit)
├── features/                        # Feature modules with screen + ViewModel pairs
│   ├── calendar/                    # CalendarScreen, CalendarViewModel, components/
│   ├── availability/                # AvailabilityScreen, AvailabilityViewModel, components/
│   ├── schedule/                    # ScheduleScreen, ScheduleViewModel, components/
│   └── settings/                    # SettingsScreen, SettingsViewModel, components/
└── shared/
    ├── components/                  # Header, UserAvatar
    ├── state/
    │   └── AppPreferences.kt        # Singleton for currentUserId, use24HourFormat
    ├── theme/                       # Color, Theme, Type
    └── utils/                       # Time formatting, slot logic
```

### State Management

**AppPreferences** (`shared/state/AppPreferences.kt`) is a singleton that manages user preferences:
- `currentUserId: Flow<String>` - Currently selected user
- `use24HourFormat: Flow<Boolean>` - 24-hour time preference
- Persists to DataStore automatically

**MainViewModel** (`core/navigation/MainViewModel.kt`) is the root ViewModel:
- Fetches users list for the header
- Exposes `AppPreferences` flows as StateFlows
- Handles initial loading/error states

Each feature screen has its own `@HiltViewModel` that:
- Injects `SchedulerRepository` for API calls
- Injects `AppPreferences` when needed (e.g., SettingsViewModel)
- Manages screen-specific state with `StateFlow`
- Implements optimistic updates with error recovery

```
MainActivity
  └── MainViewModel (users, loading/error, exposes AppPreferences)
       └── MainScreen
            ├── CalendarScreen + CalendarViewModel
            ├── AvailabilityScreen + AvailabilityViewModel
            ├── ScheduleScreen + ScheduleViewModel
            └── SettingsScreen + SettingsViewModel
```

### API Configuration

The API URL is configured via BuildConfig in `app/build.gradle.kts`:
```kotlin
buildConfigField("String", "API_URL", "\"https://your-api.ngrok.io/\"")
```

The URL is consumed in `core/networking/NetworkModule.kt`. For emulator connecting to host localhost, change to `http://10.0.2.2:6969/`.

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

`AppPreferences` persists `currentUserId` and `use24HourFormat` across app restarts via DataStore.
