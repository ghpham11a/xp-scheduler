# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

XP Scheduler is a meeting scheduling application with three clients (Next.js web, Android) sharing a FastAPI backend. Users set their availability for the next 2 weeks and schedule meetings based on overlapping available time slots.

## Commands

### Next.js Client (from `nextjs-client/`)

```bash
npm run dev      # Start development server (localhost:3000)
npm run build    # Production build
npm run start    # Start production server
npm run lint     # Run ESLint
```

### Android Client (from `android-client/`)

Open in Android Studio and run, or use Gradle:

```bash
./gradlew assembleDebug    # Build debug APK
./gradlew installDebug     # Install on connected device/emulator
./gradlew test             # Run unit tests
./gradlew connectedAndroidTest  # Run instrumented tests
```

### FastAPI Server (from `server/`)

```bash
# Activate virtual environment first
.\env\Scripts\activate  # Windows
source env/bin/activate  # Mac/Linux

# Run server
uvicorn app.main:app --host 0.0.0.0 --port 6969 --reload
```

The clients expect the API at `http://localhost:6969` (Next.js: configurable via `NEXT_PUBLIC_API_URL`, Android emulator uses `10.0.2.2:6969`).

## Architecture

### Tech Stack
- **Web Frontend**: Next.js 16, React 19, TypeScript, Zustand, Tailwind CSS v4
- **Android**: Kotlin, Jetpack Compose, Material 3, Retrofit, Moshi, ViewModel, DataStore
- **Backend**: FastAPI (Python), Pydantic, JSON file storage

### Project Structure

```
nextjs-client/
├── app/              # Next.js App Router (layout.tsx, page.tsx)
├── components/       # React components (availability/, calendar/, scheduling/)
├── lib/              # API client, Zustand store, utilities
└── types/            # TypeScript interfaces

android-client/app/src/main/java/com/example/scheduler/
├── MainActivity.kt           # Entry point
├── data/                     # Models.kt, ApiService.kt (Retrofit)
├── viewmodel/                # SchedulerViewModel.kt (state management)
├── utils/                    # Utils.kt (time formatting, helpers)
└── ui/
    ├── components/           # Header.kt
    ├── screens/              # CalendarScreen, AvailabilityScreen, ScheduleScreen
    ├── navigation/           # AppNavigation.kt (bottom nav)
    └── theme/                # Color.kt, Theme.kt

server/
├── app/
│   ├── main.py       # FastAPI app with CORS
│   ├── models.py     # Pydantic models
│   ├── storage.py    # JSON file read/write helpers
│   └── routers/      # users.py, availabilities.py, meetings.py
└── data/             # JSON storage (auto-created)
```

### State Management

**Next.js (Zustand)**: `lib/store.ts` manages currentUserId (persisted to localStorage), users, availabilities, and meetings with optimistic updates.

**Android (ViewModel)**: `SchedulerViewModel.kt` mirrors the same pattern using StateFlow and DataStore for persistence.

Both use optimistic updates - local state updates immediately, then syncs with server. On error, refetch from API.

### Key Data Types

```typescript
// Shared across all clients
interface TimeSlot {
  date: string;      // ISO date (YYYY-MM-DD)
  startHour: number; // 0-24, supports 0.5 increments for 30-min blocks
  endHour: number;
}

interface Meeting {
  id: string;
  organizerId: string;
  participantId: string;
  date: string;
  startHour: number;
  endHour: number;
  title: string;
}
```

### API Endpoints

- `GET /users` - List all users
- `GET/PUT /availabilities/{userId}` - Get/update user availability (PUT body: `TimeSlot[]`)
- `GET/POST/DELETE /meetings` - CRUD for meetings

### Component Patterns

**Next.js**:
- All components use `'use client'` directive
- StoreProvider handles hydration to prevent SSR/client mismatch
- `@/*` path alias maps to project root

**Android**:
- Jetpack Compose with Material 3
- Bottom navigation between Calendar/Availability/Schedule screens
- Retrofit with Moshi for JSON serialization

### Time Representation

Time is represented in decimal hours across all clients (9.5 = 9:30 AM). Availability uses 30-minute blocks. The calendar supports Week, Day, and Agenda view modes.
