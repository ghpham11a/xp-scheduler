'use client';

import { TimeSlot, Meeting } from '@/types';
import { DAYS_OF_WEEK, HOURS, WORK_HOURS } from '@/lib/constants';
import { formatHour, hasTimeSlot, getConflictsForSlot } from '@/lib/utils';

interface UserAvailabilityGridProps {
  slots: TimeSlot[];
  selectedSlot: { day: number; hour: number } | null;
  onSlotSelect: (day: number, hour: number) => void;
  userColor: string;
  meetings?: Meeting[];
  participantId?: string | null;
}

export function UserAvailabilityGrid({
  slots,
  selectedSlot,
  onSlotSelect,
  userColor,
  meetings = [],
  participantId,
}: UserAvailabilityGridProps) {
  const visibleHours = HOURS.filter(
    (h) => h >= WORK_HOURS.start && h < WORK_HOURS.end
  );

  return (
    <div className="overflow-x-auto">
      <div className="min-w-[600px] select-none">
        {/* Header row with day names */}
        <div className="flex">
          <div className="w-16 shrink-0" />
          {DAYS_OF_WEEK.map((day) => (
            <div
              key={day}
              className="flex-1 border-b border-r border-zinc-200 bg-zinc-50 px-2 py-2 text-center text-sm font-medium text-zinc-700 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-300"
            >
              {day}
            </div>
          ))}
        </div>

        {/* Time rows */}
        {visibleHours.map((hour) => (
          <div key={hour} className="flex">
            <div className="w-16 shrink-0 border-b border-r border-zinc-200 bg-zinc-50 px-2 py-1 text-right text-xs text-zinc-500 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-400">
              {formatHour(hour)}
            </div>
            {DAYS_OF_WEEK.map((_, dayIndex) => {
              const isAvailable = hasTimeSlot(slots, dayIndex, hour);
              const isSelected =
                selectedSlot?.day === dayIndex && selectedSlot?.hour === hour;
              const hasBooking = participantId
                ? getConflictsForSlot(meetings, participantId, dayIndex, hour)
                : null;

              return (
                <div key={dayIndex} className="flex-1 relative">
                  <div
                    className={`h-6 w-full border-b border-r border-zinc-200 transition-colors dark:border-zinc-700 ${
                      isAvailable
                        ? isSelected
                          ? 'cursor-pointer ring-2 ring-inset ring-zinc-900 dark:ring-white'
                          : 'cursor-pointer hover:opacity-80'
                        : 'cursor-not-allowed bg-zinc-100 dark:bg-zinc-800'
                    }`}
                    style={
                      isAvailable ? { backgroundColor: userColor + '80' } : {}
                    }
                    onClick={() => isAvailable && onSlotSelect(dayIndex, hour)}
                  >
                    {hasBooking && isAvailable && (
                      <div className="absolute inset-0 flex items-center justify-center">
                        <div className="h-2 w-2 rounded-full bg-red-500" title={`Booked: ${hasBooking.title}`} />
                      </div>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        ))}
      </div>
    </div>
  );
}
