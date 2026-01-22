'use client';

import { useCallback, useMemo } from 'react';
import { TimeSlot } from '@/types';
import { formatTime, mergeAdjacentSlots } from '@/lib/utils';

interface AvailabilityPickerProps {
  slots: TimeSlot[];
  onSlotsChange: (slots: TimeSlot[]) => void;
}

const DAY_NAMES = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];

// Generate next 14 days
function getUpcomingDates(): { date: Date; dateString: string; dayName: string; isToday: boolean; isTomorrow: boolean }[] {
  const dates: { date: Date; dateString: string; dayName: string; isToday: boolean; isTomorrow: boolean }[] = [];
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  for (let i = 0; i < 14; i++) {
    const date = new Date(today);
    date.setDate(today.getDate() + i);
    dates.push({
      date,
      dateString: date.toISOString().split('T')[0],
      dayName: DAY_NAMES[date.getDay()],
      isToday: i === 0,
      isTomorrow: i === 1,
    });
  }

  return dates;
}

// 30-minute increments for all 24 hours
function generateTimeBlocks(): number[] {
  const blocks: number[] = [];
  for (let hour = 0; hour < 24; hour += 0.5) {
    blocks.push(hour);
  }
  return blocks;
}

// Check if a specific 30-min block is available for a date
function hasTimeBlock(slots: TimeSlot[], date: string, startHour: number): boolean {
  const endHour = startHour + 0.5;
  return slots.some(
    (slot) =>
      slot.date === date &&
      startHour >= slot.startHour &&
      endHour <= slot.endHour
  );
}

// Updated mergeAdjacentSlots for date-based slots
function mergeAdjacentSlotsForDate(slots: TimeSlot[]): TimeSlot[] {
  // Group by date
  const byDate = new Map<string, TimeSlot[]>();
  slots.forEach((slot) => {
    if (!byDate.has(slot.date)) byDate.set(slot.date, []);
    byDate.get(slot.date)!.push(slot);
  });

  const merged: TimeSlot[] = [];

  byDate.forEach((dateSlots, date) => {
    // Sort by start hour
    dateSlots.sort((a, b) => a.startHour - b.startHour);

    let current: TimeSlot | null = null;

    dateSlots.forEach((slot) => {
      if (!current) {
        current = { ...slot };
      } else if (slot.startHour <= current.endHour) {
        // Overlapping or adjacent - merge
        current.endHour = Math.max(current.endHour, slot.endHour);
      } else {
        // Gap - push current and start new
        merged.push(current);
        current = { ...slot };
      }
    });

    if (current) merged.push(current);
  });

  return merged;
}

export function AvailabilityPicker({ slots, onSlotsChange }: AvailabilityPickerProps) {
  const upcomingDates = useMemo(() => getUpcomingDates(), []);
  const timeBlocks = generateTimeBlocks();

  const toggleBlock = useCallback(
    (date: string, startHour: number) => {
      const endHour = startHour + 0.5;
      const isAvailable = hasTimeBlock(slots, date, startHour);
      let newSlots: TimeSlot[];

      if (isAvailable) {
        // Remove this 30-min block
        newSlots = slots
          .map((slot) => {
            if (slot.date !== date) return slot;
            if (startHour < slot.startHour || endHour > slot.endHour) return slot;

            const result: TimeSlot[] = [];
            if (slot.startHour < startHour) {
              result.push({ ...slot, endHour: startHour });
            }
            if (endHour < slot.endHour) {
              result.push({ ...slot, startHour: endHour });
            }
            return result;
          })
          .flat()
          .filter((slot) => slot.startHour < slot.endHour);
      } else {
        // Add this 30-min block
        newSlots = [...slots, { date, startHour, endHour }];
      }

      onSlotsChange(mergeAdjacentSlotsForDate(newSlots));
    },
    [slots, onSlotsChange]
  );

  const selectAllDay = useCallback(
    (date: string) => {
      let newSlots = slots.filter((s) => s.date !== date);
      newSlots.push({
        date,
        startHour: 0,
        endHour: 24,
      });
      onSlotsChange(mergeAdjacentSlotsForDate(newSlots));
    },
    [slots, onSlotsChange]
  );

  const clearDay = useCallback(
    (date: string) => {
      const newSlots = slots.filter((s) => s.date !== date);
      onSlotsChange(newSlots);
    },
    [slots, onSlotsChange]
  );

  const selectMorning = useCallback(
    (date: string) => {
      let newSlots = slots.filter((s) => s.date !== date);
      newSlots.push({
        date,
        startHour: 6,
        endHour: 12,
      });
      onSlotsChange(mergeAdjacentSlotsForDate(newSlots));
    },
    [slots, onSlotsChange]
  );

  const selectAfternoon = useCallback(
    (date: string) => {
      let newSlots = slots.filter((s) => s.date !== date);
      newSlots.push({
        date,
        startHour: 12,
        endHour: 18,
      });
      onSlotsChange(mergeAdjacentSlotsForDate(newSlots));
    },
    [slots, onSlotsChange]
  );

  const selectEvening = useCallback(
    (date: string) => {
      let newSlots = slots.filter((s) => s.date !== date);
      newSlots.push({
        date,
        startHour: 18,
        endHour: 22,
      });
      onSlotsChange(mergeAdjacentSlotsForDate(newSlots));
    },
    [slots, onSlotsChange]
  );

  const selectBusinessHours = useCallback(
    (date: string) => {
      let newSlots = slots.filter((s) => s.date !== date);
      newSlots.push({
        date,
        startHour: 9,
        endHour: 17,
      });
      onSlotsChange(mergeAdjacentSlotsForDate(newSlots));
    },
    [slots, onSlotsChange]
  );

  const getDayAvailableHours = (date: string): number => {
    return timeBlocks.filter((hour) => hasTimeBlock(slots, date, hour)).length * 0.5;
  };

  const formatDateLabel = (item: typeof upcomingDates[0]): string => {
    if (item.isToday) return 'Today';
    if (item.isTomorrow) return 'Tomorrow';
    return item.date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  };

  return (
    <div className="space-y-4 max-h-[65vh] overflow-y-auto pr-1">
      {upcomingDates.map((item) => {
        const availableHours = getDayAvailableHours(item.dateString);

        return (
          <div
            key={item.dateString}
            className={`rounded-lg border overflow-hidden ${
              item.isToday
                ? 'border-blue-300 dark:border-blue-700'
                : 'border-zinc-200 dark:border-zinc-700'
            }`}
          >
            {/* Day Header */}
            <div className={`px-4 py-3 border-b ${
              item.isToday
                ? 'bg-blue-50 dark:bg-blue-900/30 border-blue-200 dark:border-blue-800'
                : 'bg-zinc-50 dark:bg-zinc-800 border-zinc-200 dark:border-zinc-700'
            }`}>
              <div className="flex items-center justify-between mb-2">
                <div className="flex items-center gap-2">
                  <span className={`font-medium ${item.isToday ? 'text-blue-700 dark:text-blue-300' : 'text-zinc-900 dark:text-zinc-100'}`}>
                    {item.dayName}
                  </span>
                  <span className={`text-sm ${item.isToday ? 'text-blue-600 dark:text-blue-400' : 'text-zinc-500 dark:text-zinc-400'}`}>
                    {formatDateLabel(item)}
                  </span>
                </div>
                <span className="text-sm text-zinc-500 dark:text-zinc-400">
                  {availableHours > 0 ? `${availableHours} hrs` : 'No availability'}
                </span>
              </div>

              {/* Quick actions */}
              <div className="flex flex-wrap gap-1.5">
                <button
                  onClick={() => selectAllDay(item.dateString)}
                  className="px-2 py-1 text-xs rounded bg-blue-100 text-blue-700 hover:bg-blue-200 dark:bg-blue-900/40 dark:text-blue-300 dark:hover:bg-blue-900/60"
                >
                  24h
                </button>
                <button
                  onClick={() => selectBusinessHours(item.dateString)}
                  className="px-2 py-1 text-xs rounded bg-zinc-100 text-zinc-700 hover:bg-zinc-200 dark:bg-zinc-700 dark:text-zinc-300 dark:hover:bg-zinc-600"
                >
                  9-5
                </button>
                <button
                  onClick={() => selectMorning(item.dateString)}
                  className="px-2 py-1 text-xs rounded bg-zinc-100 text-zinc-700 hover:bg-zinc-200 dark:bg-zinc-700 dark:text-zinc-300 dark:hover:bg-zinc-600"
                >
                  Morning
                </button>
                <button
                  onClick={() => selectAfternoon(item.dateString)}
                  className="px-2 py-1 text-xs rounded bg-zinc-100 text-zinc-700 hover:bg-zinc-200 dark:bg-zinc-700 dark:text-zinc-300 dark:hover:bg-zinc-600"
                >
                  Afternoon
                </button>
                <button
                  onClick={() => selectEvening(item.dateString)}
                  className="px-2 py-1 text-xs rounded bg-zinc-100 text-zinc-700 hover:bg-zinc-200 dark:bg-zinc-700 dark:text-zinc-300 dark:hover:bg-zinc-600"
                >
                  Evening
                </button>
                <button
                  onClick={() => clearDay(item.dateString)}
                  className="px-2 py-1 text-xs rounded bg-zinc-100 text-zinc-500 hover:bg-red-100 hover:text-red-600 dark:bg-zinc-700 dark:text-zinc-400 dark:hover:bg-red-900/40 dark:hover:text-red-400"
                >
                  Clear
                </button>
              </div>
            </div>

            {/* Time Blocks - 30 min increments */}
            <div className="p-3 flex flex-wrap gap-1">
              {timeBlocks.map((startHour) => {
                const isAvailable = hasTimeBlock(slots, item.dateString, startHour);

                return (
                  <button
                    key={startHour}
                    onClick={() => toggleBlock(item.dateString, startHour)}
                    className={`
                      px-1.5 py-1 rounded text-xs font-medium transition-all min-w-[52px]
                      ${isAvailable
                        ? 'bg-blue-500 text-white'
                        : 'bg-zinc-100 text-zinc-500 hover:bg-zinc-200 dark:bg-zinc-800 dark:text-zinc-400 dark:hover:bg-zinc-700'
                      }
                    `}
                  >
                    {formatTime(startHour)}
                  </button>
                );
              })}
            </div>
          </div>
        );
      })}
    </div>
  );
}
