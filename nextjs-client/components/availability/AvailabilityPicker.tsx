'use client';

import { useCallback, useMemo, useState } from 'react';
import { TimeSlot } from '@/types';
import { formatTime } from '@/lib/utils';
import { useSchedulerStore } from '@/lib/store';

interface AvailabilityPickerProps {
  slots: TimeSlot[];
  onSlotsChange: (slots: TimeSlot[]) => void;
}

const DAY_NAMES = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
const DAY_NAMES_FULL = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];

// Generate next 14 days
function getUpcomingDates(): { date: Date; dateString: string; dayName: string; dayNameFull: string; isToday: boolean; isTomorrow: boolean }[] {
  const dates: { date: Date; dateString: string; dayName: string; dayNameFull: string; isToday: boolean; isTomorrow: boolean }[] = [];
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  for (let i = 0; i < 14; i++) {
    const date = new Date(today);
    date.setDate(today.getDate() + i);
    dates.push({
      date,
      dateString: date.toISOString().split('T')[0],
      dayName: DAY_NAMES[date.getDay()],
      dayNameFull: DAY_NAMES_FULL[date.getDay()],
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

// Merge adjacent slots for date-based slots
function mergeAdjacentSlotsForDate(slots: TimeSlot[]): TimeSlot[] {
  const byDate = new Map<string, TimeSlot[]>();
  slots.forEach((slot) => {
    if (!byDate.has(slot.date)) byDate.set(slot.date, []);
    byDate.get(slot.date)!.push(slot);
  });

  const merged: TimeSlot[] = [];

  byDate.forEach((dateSlots) => {
    dateSlots.sort((a, b) => a.startHour - b.startHour);

    let current: TimeSlot | null = null;

    dateSlots.forEach((slot) => {
      if (!current) {
        current = { ...slot };
      } else if (slot.startHour <= current.endHour) {
        current.endHour = Math.max(current.endHour, slot.endHour);
      } else {
        merged.push(current);
        current = { ...slot };
      }
    });

    if (current) merged.push(current);
  });

  return merged;
}

export function AvailabilityPicker({ slots, onSlotsChange }: AvailabilityPickerProps) {
  const { use24HourTime } = useSchedulerStore();
  const upcomingDates = useMemo(() => getUpcomingDates(), []);
  const timeBlocks = generateTimeBlocks();
  const [selectedDayIndex, setSelectedDayIndex] = useState(0);

  const currentDay = upcomingDates[selectedDayIndex];

  const toggleBlock = useCallback(
    (date: string, startHour: number) => {
      const endHour = startHour + 0.5;
      const isAvailable = hasTimeBlock(slots, date, startHour);
      let newSlots: TimeSlot[];

      if (isAvailable) {
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
        newSlots = [...slots, { date, startHour, endHour }];
      }

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

  const goToPrevDay = () => {
    setSelectedDayIndex((prev) => Math.max(0, prev - 1));
  };

  const goToNextDay = () => {
    setSelectedDayIndex((prev) => Math.min(upcomingDates.length - 1, prev + 1));
  };

  const availableHours = getDayAvailableHours(currentDay.dateString);

  return (
    <div className="space-y-3">
      {/* Day selector - horizontal scroll pills */}
      <div className="flex gap-1.5 overflow-x-auto pb-2 -mx-1 px-1 scrollbar-hide">
        {upcomingDates.map((item, index) => {
          const dayHours = getDayAvailableHours(item.dateString);
          const isSelected = index === selectedDayIndex;

          return (
            <button
              key={item.dateString}
              onClick={() => setSelectedDayIndex(index)}
              className={`
                flex-shrink-0 flex flex-col items-center px-3 py-2 rounded-lg transition-all min-w-[56px]
                ${isSelected
                  ? 'bg-blue-500 text-white'
                  : item.isToday
                    ? 'bg-blue-100 text-blue-700 dark:bg-blue-900/40 dark:text-blue-300'
                    : 'bg-zinc-100 text-zinc-600 hover:bg-zinc-200 dark:bg-zinc-800 dark:text-zinc-400 dark:hover:bg-zinc-700'
                }
              `}
            >
              <span className="text-xs font-medium">{item.dayName}</span>
              <span className={`text-lg font-semibold ${isSelected ? '' : ''}`}>
                {item.date.getDate()}
              </span>
              {dayHours > 0 && (
                <div className={`w-1.5 h-1.5 rounded-full mt-0.5 ${isSelected ? 'bg-white' : 'bg-blue-500'}`} />
              )}
            </button>
          );
        })}
      </div>

      {/* Current day header with nav */}
      <div className="flex items-center justify-between">
        <button
          onClick={goToPrevDay}
          disabled={selectedDayIndex === 0}
          className="p-2 rounded-lg hover:bg-zinc-100 dark:hover:bg-zinc-800 disabled:opacity-30 disabled:cursor-not-allowed"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
        </button>

        <div className="text-center">
          <div className="font-semibold text-zinc-900 dark:text-zinc-100">
            {currentDay.dayNameFull}
          </div>
          <div className="text-sm text-zinc-500 dark:text-zinc-400">
            {formatDateLabel(currentDay)} • {availableHours > 0 ? `${availableHours} hrs selected` : 'No availability'}
          </div>
        </div>

        <button
          onClick={goToNextDay}
          disabled={selectedDayIndex === upcomingDates.length - 1}
          className="p-2 rounded-lg hover:bg-zinc-100 dark:hover:bg-zinc-800 disabled:opacity-30 disabled:cursor-not-allowed"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
          </svg>
        </button>
      </div>

      {/* Vertical time blocks */}
      <div className="max-h-[50vh] overflow-y-auto rounded-lg border border-zinc-200 dark:border-zinc-700">
        {timeBlocks.map((startHour) => {
          const isAvailable = hasTimeBlock(slots, currentDay.dateString, startHour);
          const isHourMark = startHour === Math.floor(startHour);

          return (
            <button
              key={startHour}
              onClick={() => toggleBlock(currentDay.dateString, startHour)}
              className={`
                w-full flex items-center px-4 py-3 transition-all border-b border-zinc-100 dark:border-zinc-800 last:border-b-0
                ${isAvailable
                  ? 'bg-blue-500 text-white'
                  : 'bg-white hover:bg-zinc-50 dark:bg-zinc-900 dark:hover:bg-zinc-800'
                }
              `}
            >
              <span className={`
                w-16 text-left font-medium
                ${isAvailable ? 'text-white' : isHourMark ? 'text-zinc-900 dark:text-zinc-100' : 'text-zinc-400 dark:text-zinc-500'}
              `}>
                {formatTime(startHour, use24HourTime)}
              </span>
              <div className="flex-1 h-1 mx-3 rounded-full bg-zinc-100 dark:bg-zinc-800 overflow-hidden">
                {isAvailable && <div className="h-full bg-white/30 w-full" />}
              </div>
              <div className={`
                w-6 h-6 rounded-full border-2 flex items-center justify-center
                ${isAvailable
                  ? 'border-white bg-white/20'
                  : 'border-zinc-300 dark:border-zinc-600'
                }
              `}>
                {isAvailable && (
                  <svg className="w-4 h-4 text-white" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                  </svg>
                )}
              </div>
            </button>
          );
        })}
      </div>
    </div>
  );
}
