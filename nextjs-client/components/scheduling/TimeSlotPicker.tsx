'use client';

import { useMemo } from 'react';
import { TimeSlot, Meeting } from '@/types';
import { formatTime, hasConflict } from '@/lib/utils';
import { useSchedulerStore } from '@/lib/store';

interface TimeBlock {
  startHour: number;
  endHour: number;
  isAvailable: boolean;
  hasConflict: boolean;
  conflictTitle?: string;
}

interface DaySchedule {
  date: string;
  dayName: string;
  dateDisplay: string;
  blocks: TimeBlock[];
}

interface TimeSlotPickerProps {
  duration: number;
  slots: TimeSlot[];
  meetings: Meeting[];
  currentUserId: string;
  participantId: string;
  selectedBlock: { date: string; startHour: number; endHour: number } | null;
  onBlockSelect: (date: string, startHour: number, endHour: number) => void;
}

const DAY_NAMES = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];

function getNextSevenDays(): { date: Date; dateString: string }[] {
  const days: { date: Date; dateString: string }[] = [];
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  for (let i = 0; i < 7; i++) {
    const date = new Date(today);
    date.setDate(today.getDate() + i);
    days.push({
      date,
      dateString: date.toISOString().split('T')[0],
    });
  }

  return days;
}

function formatDateDisplay(date: Date): string {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const tomorrow = new Date(today);
  tomorrow.setDate(today.getDate() + 1);

  if (date.toDateString() === today.toDateString()) {
    return 'Today';
  }
  if (date.toDateString() === tomorrow.toDateString()) {
    return 'Tomorrow';
  }

  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}

// Check if a time range is available for a specific date
function isSlotAvailable(slots: TimeSlot[], date: string, startHour: number, endHour: number): boolean {
  return slots.some(
    (slot) =>
      slot.date === date &&
      startHour >= slot.startHour &&
      endHour <= slot.endHour
  );
}

function generateTimeBlocks(
  duration: number,
  date: string,
  slots: TimeSlot[],
  meetings: Meeting[],
  currentUserId: string,
  participantId: string
): TimeBlock[] {
  const blocks: TimeBlock[] = [];

  // Generate blocks for all 24 hours based on duration
  for (let hour = 0; hour + duration <= 24; hour += duration) {
    const blockStart = hour;
    const blockEnd = hour + duration;

    const isAvailable = isSlotAvailable(slots, date, blockStart, blockEnd);

    // Check for conflicts for both organizer and participant
    const organizerConflict = hasConflict(meetings, currentUserId, date, blockStart, blockEnd);
    const participantConflict = hasConflict(meetings, participantId, date, blockStart, blockEnd);
    const conflict = organizerConflict || participantConflict;

    blocks.push({
      startHour: blockStart,
      endHour: blockEnd,
      isAvailable,
      hasConflict: !!conflict,
      conflictTitle: conflict?.title,
    });
  }

  return blocks;
}

export function TimeSlotPicker({
  duration,
  slots,
  meetings,
  currentUserId,
  participantId,
  selectedBlock,
  onBlockSelect,
}: TimeSlotPickerProps) {
  const { use24HourTime } = useSchedulerStore();
  const daySchedules = useMemo<DaySchedule[]>(() => {
    const days = getNextSevenDays();

    return days.map(({ date, dateString }) => {
      const blocks = generateTimeBlocks(
        duration,
        dateString,
        slots,
        meetings,
        currentUserId,
        participantId
      );

      return {
        date: dateString,
        dayName: DAY_NAMES[date.getDay()],
        dateDisplay: formatDateDisplay(date),
        blocks,
      };
    });
  }, [duration, slots, meetings, currentUserId, participantId]);

  const hasAnyAvailableSlots = daySchedules.some(day =>
    day.blocks.some(block => block.isAvailable && !block.hasConflict)
  );

  if (!hasAnyAvailableSlots) {
    return (
      <div className="rounded-lg border border-dashed border-zinc-300 p-6 text-center dark:border-zinc-700">
        <p className="text-sm text-zinc-500 dark:text-zinc-400">
          No available time slots for a {duration * 60} minute meeting this week.
        </p>
        <p className="text-xs text-zinc-400 dark:text-zinc-500 mt-1">
          The person you selected hasn&apos;t set availability for these dates.
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-4 max-h-[60vh] overflow-y-auto pr-1">
      {daySchedules.map((day) => {
        const availableBlocks = day.blocks.filter(b => b.isAvailable);

        if (availableBlocks.length === 0) {
          return null;
        }

        return (
          <div key={day.date} className="rounded-lg border border-zinc-200 dark:border-zinc-700 overflow-hidden">
            {/* Day Header */}
            <div className="bg-zinc-50 dark:bg-zinc-800 px-4 py-2 border-b border-zinc-200 dark:border-zinc-700">
              <div className="flex items-center justify-between">
                <span className="font-medium text-zinc-900 dark:text-zinc-100">
                  {day.dayName}
                </span>
                <span className="text-sm text-zinc-500 dark:text-zinc-400">
                  {day.dateDisplay}
                </span>
              </div>
            </div>

            {/* Time Blocks */}
            <div className="p-3 flex flex-wrap gap-2">
              {day.blocks.map((block) => {
                if (!block.isAvailable) return null;

                const isSelected =
                  selectedBlock?.date === day.date &&
                  selectedBlock?.startHour === block.startHour &&
                  selectedBlock?.endHour === block.endHour;

                const isDisabled = block.hasConflict;

                return (
                  <button
                    key={`${block.startHour}-${block.endHour}`}
                    onClick={() => !isDisabled && onBlockSelect(day.date, block.startHour, block.endHour)}
                    disabled={isDisabled}
                    className={`
                      px-3 py-2 rounded-lg text-sm font-medium transition-all
                      ${isSelected
                        ? 'bg-blue-500 text-white ring-2 ring-blue-500 ring-offset-2 dark:ring-offset-zinc-900'
                        : isDisabled
                          ? 'bg-zinc-100 text-zinc-400 cursor-not-allowed dark:bg-zinc-800 dark:text-zinc-600 line-through'
                          : 'bg-blue-50 text-blue-700 hover:bg-blue-100 dark:bg-blue-900/30 dark:text-blue-300 dark:hover:bg-blue-900/50'
                      }
                    `}
                    title={isDisabled ? `Conflict: ${block.conflictTitle}` : undefined}
                  >
                    {formatTime(block.startHour, use24HourTime)} - {formatTime(block.endHour, use24HourTime)}
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
