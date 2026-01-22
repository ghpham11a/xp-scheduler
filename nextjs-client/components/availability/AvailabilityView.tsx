'use client';

import { useSchedulerStore } from '@/lib/store';
import { AvailabilityPicker } from './AvailabilityPicker';
import { TimeSlot } from '@/types';

export function AvailabilityView() {
  const { currentUserId, users, availabilities, setAvailability } = useSchedulerStore();
  const currentUser = users.find((u) => u.id === currentUserId);
  const currentAvailability = availabilities.find((a) => a.userId === currentUserId);

  const handleSlotsChange = (slots: TimeSlot[]) => {
    setAvailability(currentUserId, slots);
  };

  const totalAvailableHours = (currentAvailability?.slots ?? []).reduce((total, slot) => {
    return total + (slot.endHour - slot.startHour);
  }, 0);

  const daysWithAvailability = new Set(
    (currentAvailability?.slots ?? []).map((slot) => slot.date)
  ).size;

  return (
    <div className="rounded-lg border border-zinc-200 bg-white p-4 sm:p-6 dark:border-zinc-800 dark:bg-zinc-900">
      <div className="mb-4">
        <div className="flex items-start justify-between gap-4 mb-2">
          <div>
            <h2 className="text-lg font-semibold text-zinc-900 dark:text-zinc-100">
              Set Your Availability
            </h2>
            <p className="mt-1 text-sm text-zinc-500 dark:text-zinc-400">
              Set your availability for the next 2 weeks. Tap time blocks to toggle.
            </p>
          </div>
          {currentUser && (
            <div
              className="flex-shrink-0 flex h-10 w-10 items-center justify-center rounded-full text-sm font-semibold text-white"
              style={{ backgroundColor: currentUser.avatarColor }}
            >
              {currentUser.name.split(' ').map(n => n[0]).join('').slice(0, 2)}
            </div>
          )}
        </div>

        {/* Summary */}
        <div className="flex flex-wrap items-center gap-4 text-sm text-zinc-600 dark:text-zinc-400">
          <div className="flex items-center gap-2">
            <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <span>
              <strong className="text-zinc-900 dark:text-zinc-100">{totalAvailableHours}</strong> hours total
            </span>
          </div>
          <div className="flex items-center gap-2">
            <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
            <span>
              <strong className="text-zinc-900 dark:text-zinc-100">{daysWithAvailability}</strong> days
            </span>
          </div>
        </div>
      </div>

      {/* Availability Picker */}
      <AvailabilityPicker
        slots={currentAvailability?.slots ?? []}
        onSlotsChange={handleSlotsChange}
      />

      {/* Legend */}
      <div className="mt-4 pt-4 border-t border-zinc-200 dark:border-zinc-700 flex items-center gap-4 text-sm text-zinc-500 dark:text-zinc-400">
        <div className="flex items-center gap-2">
          <div className="h-4 w-4 rounded bg-blue-500" />
          <span>Available</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="h-4 w-4 rounded bg-zinc-100 dark:bg-zinc-800 border border-zinc-200 dark:border-zinc-700" />
          <span>Unavailable</span>
        </div>
      </div>
    </div>
  );
}
