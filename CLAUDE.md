# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

XP Scheduler is a meeting scheduling application built with Next.js 16. Users can set their availability for the next 2 weeks and schedule meetings with other users based on overlapping available time slots.

## Commands

All commands run from `nextjs-client/`:

```bash
npm run dev      # Start development server (localhost:3000)
npm run build    # Production build
npm run start    # Start production server
npm run lint     # Run ESLint
```

## Architecture

### Tech Stack
- Next.js 16 with App Router
- React 19
- TypeScript (strict mode)
- Zustand for state management (persisted to localStorage)
- Tailwind CSS v4

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
│   ├── providers/    # StoreProvider (hydration handling)
│   └── scheduling/   # MeetingForm, MeetingList, ScheduleMeetingView, TimeSlotPicker, UserAvailabilityGrid
├── lib/
│   ├── constants.ts  # Mock users, day names, hour arrays
│   ├── store.ts      # Zustand store with persist middleware
│   └── utils.ts      # Time formatting, slot merging, conflict detection
└── types/
    └── index.ts      # TypeScript interfaces (User, TimeSlot, Meeting, etc.)
```

### State Management

The Zustand store (`lib/store.ts`) manages:
- `currentUserId`: Active user (switchable via Header dropdown)
- `users`: Array of mock users
- `availabilities`: Per-user time slots (date + startHour/endHour)
- `meetings`: Scheduled meetings between users

State is persisted to localStorage under key `scheduler-storage`.

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
