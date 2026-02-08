'use client';

import { useState, useMemo } from 'react';
import { User, Meeting } from '@/types';
import { DAYS_OF_WEEK } from '@/lib/constants';
import { formatTimeRange, getDateForDayOfWeek, hasConflict } from '@/lib/utils';
import { useSchedulerStore } from '@/lib/store';

const DURATION_OPTIONS = [
  { value: 0.25, label: '15 min' },
  { value: 0.5, label: '30 min' },
  { value: 0.75, label: '45 min' },
  { value: 1, label: '60 min' },
  { value: 1.5, label: '90 min' },
  { value: 2, label: '120 min' },
];

interface MeetingFormProps {
  selectedSlot: { day: number; hour: number } | null;
  participant: User;
  currentUserId: string;
  meetings: Meeting[];
  onSchedule: (title: string, date: string, startHour: number, endHour: number) => void;
  onCancel: () => void;
}

export function MeetingForm({
  selectedSlot,
  participant,
  currentUserId,
  meetings,
  onSchedule,
  onCancel,
}: MeetingFormProps) {
  const { use24HourTime } = useSchedulerStore();
  const [title, setTitle] = useState('');
  const [duration, setDuration] = useState(0.5);

  const conflict = useMemo(() => {
    if (!selectedSlot) return null;
    const date = getDateForDayOfWeek(selectedSlot.day);
    const startHour = selectedSlot.hour;
    const endHour = selectedSlot.hour + duration;

    // Check conflicts for both the current user (organizer) and participant
    const organizerConflict = hasConflict(meetings, currentUserId, date, startHour, endHour);
    const participantConflict = hasConflict(meetings, participant.id, date, startHour, endHour);

    if (organizerConflict) {
      return { type: 'organizer', meeting: organizerConflict };
    }
    if (participantConflict) {
      return { type: 'participant', meeting: participantConflict };
    }
    return null;
  }, [selectedSlot, duration, meetings, currentUserId, participant.id]);

  if (!selectedSlot) {
    return (
      <div className="rounded-lg border border-dashed border-zinc-300 p-6 text-center dark:border-zinc-700">
        <p className="text-sm text-zinc-500 dark:text-zinc-400">
          Select an available time slot to schedule a meeting
        </p>
      </div>
    );
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || conflict) return;

    const date = getDateForDayOfWeek(selectedSlot.day);
    onSchedule(title.trim(), date, selectedSlot.hour, selectedSlot.hour + duration);
    setTitle('');
    setDuration(0.5);
  };

  return (
    <form
      onSubmit={handleSubmit}
      className="rounded-lg border border-zinc-200 bg-zinc-50 p-4 dark:border-zinc-700 dark:bg-zinc-800"
    >
      <h4 className="mb-4 font-medium text-zinc-900 dark:text-zinc-100">
        Schedule Meeting
      </h4>

      <div className="mb-4 rounded-lg bg-white p-3 dark:bg-zinc-900">
        <div className="text-sm text-zinc-500 dark:text-zinc-400">
          {DAYS_OF_WEEK[selectedSlot.day]} at {formatTimeRange(selectedSlot.hour, selectedSlot.hour + duration, use24HourTime)}
        </div>
        <div className="mt-1 flex items-center gap-2">
          <div
            className="h-4 w-4 rounded-full"
            style={{ backgroundColor: participant.avatarColor }}
          />
          <span className="text-sm font-medium text-zinc-900 dark:text-zinc-100">
            with {participant.name}
          </span>
        </div>
      </div>

      <div className="mb-4">
        <label
          htmlFor="title"
          className="mb-1 block text-sm font-medium text-zinc-700 dark:text-zinc-300"
        >
          Meeting Title
        </label>
        <input
          id="title"
          type="text"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="e.g., Project sync"
          className="w-full rounded-lg border border-zinc-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 dark:border-zinc-600 dark:bg-zinc-900 dark:text-zinc-100"
          required
        />
      </div>

      <div className="mb-4">
        <label
          htmlFor="duration"
          className="mb-1 block text-sm font-medium text-zinc-700 dark:text-zinc-300"
        >
          Duration
        </label>
        <select
          id="duration"
          value={duration}
          onChange={(e) => setDuration(Number(e.target.value))}
          className="w-full rounded-lg border border-zinc-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 dark:border-zinc-600 dark:bg-zinc-900 dark:text-zinc-100"
        >
          {DURATION_OPTIONS.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </div>

      {conflict && (
        <div className="mb-4 rounded-lg border border-red-200 bg-red-50 p-3 dark:border-red-900 dark:bg-red-950">
          <div className="flex items-center gap-2 text-sm text-red-700 dark:text-red-400">
            <svg className="h-4 w-4 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
            <div>
              <span className="font-medium">Conflict detected:</span>{' '}
              {conflict.type === 'organizer' ? 'You already have' : `${participant.name} already has`}{' '}
              &quot;{conflict.meeting.title}&quot; scheduled during this time.
            </div>
          </div>
        </div>
      )}

      <div className="flex gap-2">
        <button
          type="submit"
          disabled={!!conflict}
          className={`flex-1 rounded-lg px-4 py-2 text-sm font-medium text-white focus:outline-none focus:ring-2 focus:ring-offset-2 ${
            conflict
              ? 'cursor-not-allowed bg-zinc-400 dark:bg-zinc-600'
              : 'bg-blue-500 hover:bg-blue-600 focus:ring-blue-500'
          }`}
        >
          {conflict ? 'Cannot Schedule' : 'Schedule'}
        </button>
        <button
          type="button"
          onClick={onCancel}
          className="rounded-lg border border-zinc-300 px-4 py-2 text-sm font-medium text-zinc-700 hover:bg-zinc-100 focus:outline-none focus:ring-2 focus:ring-zinc-500 focus:ring-offset-2 dark:border-zinc-600 dark:text-zinc-300 dark:hover:bg-zinc-800"
        >
          Cancel
        </button>
      </div>
    </form>
  );
}
