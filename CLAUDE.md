# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

XP Scheduler is a meeting scheduling application built with Next.js 16. Users can set their availability for the next 2 weeks and schedule meetings with other users based on overlapping available time slots.

## Commands

### Next.js Client (from `nextjs-client/`)

```bash
npm run dev      # Start development server (localhost:3000)
npm run build    # Production build
npm run start    # Start production server
npm run lint     # Run ESLint
```

### FastAPI Server (from `server/`)

```bash
# Activate virtual environment first
.\env\Scripts\activate  # Windows
source env/bin/activate  # Mac/Linux

# Run server
uvicorn app.main:app --host 0.0.0.0 --port 6969 --reload
```

The client expects the API at `http://localhost:6969` (configurable via `NEXT_PUBLIC_API_URL`).

## Architecture

### Tech Stack
- **Frontend**: Next.js 16, React 19, TypeScript, Zustand, Tailwind CSS v4
- **Backend**: FastAPI (Python), Pydantic, JSON file storage

### Project Structure

```
nextjs-client/
├── app/              # Next.js App Router
│   ├── layout.tsx    # Root layout with StoreProvider
│   └── page.tsx      # Main page with tab navigation (Calendar/Availability/Schedule)
├── components/
│   ├── availability/ # AvailabilityPicker, AvailabilityView
│   ├── calendar/     # CalendarView (weekly calendar display)
│   ├── layout/       # Header
│   ├── providers/    # StoreProvider (hydration + API fetch)
│   └── scheduling/   # MeetingForm, MeetingList, ScheduleMeetingView, TimeSlotPicker, UserAvailabilityGrid
├── lib/
│   ├── api.ts        # API client for FastAPI backend
│   ├── constants.ts  # Day names, hour arrays
│   ├── store.ts      # Zustand store (syncs with API)
│   └── utils.ts      # Time formatting, slot merging, conflict detection
└── types/
    └── index.ts      # TypeScript interfaces (User, TimeSlot, Meeting, etc.)

server/
├── app/
│   ├── main.py       # FastAPI app with CORS
│   ├── models.py     # Pydantic models
│   ├── storage.py    # JSON file read/write helpers
│   └── routers/
│       ├── users.py         # GET /users
│       ├── availabilities.py # GET/PUT /availabilities
│       └── meetings.py      # GET/POST/DELETE /meetings
└── data/             # JSON storage (auto-created)
    ├── users.json
    ├── availabilities.json
    └── meetings.json
```

### State Management

The Zustand store (`lib/store.ts`) manages:
- `currentUserId`: Active user (switchable via Header dropdown) - persisted to localStorage
- `users`: Array of users (fetched from API)
- `availabilities`: Per-user time slots (synced with API)
- `meetings`: Scheduled meetings (synced with API)

The store uses optimistic updates - local state is updated immediately, then synced with the server. On error, it refetches from the API.

### Key Data Types

```typescript
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

### Component Patterns

- All components use `'use client'` directive (client-side rendering)
- StoreProvider handles hydration to prevent SSR/client mismatch
- Time is represented in decimal hours (9.5 = 9:30 AM)
- The `@/*` path alias maps to the project root

### Calendar Views

CalendarView (`components/calendar/CalendarView.tsx`) supports three view modes:
- **Week**: Traditional 7-column grid (default on desktop)
- **Day**: Single day with horizontal day selector (default on mobile <768px)
- **Agenda**: Vertical list grouped by day (most mobile-friendly)

The calendar auto-switches to day view on mobile devices.
