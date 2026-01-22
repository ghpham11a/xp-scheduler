'use client';

import { useState } from 'react';
import { useSchedulerStore } from '@/lib/store';
import { getMeetingsForUser, getUserInitials } from '@/lib/utils';
import { TimeSlotPicker } from './TimeSlotPicker';
import { MeetingList } from './MeetingList';

const DURATION_OPTIONS = [
  { value: 0.25, label: '15 min' },
  { value: 0.5, label: '30 min' },
  { value: 0.75, label: '45 min' },
  { value: 1, label: '60 min' },
  { value: 1.5, label: '90 min' },
  { value: 2, label: '120 min' },
];

export function ScheduleMeetingView() {
  const {
    currentUserId,
    users,
    availabilities,
    meetings,
    addMeeting,
    cancelMeeting,
  } = useSchedulerStore();

  const [selectedUserId, setSelectedUserId] = useState<string | null>(null);
  const [duration, setDuration] = useState(0.5);
  const [selectedBlock, setSelectedBlock] = useState<{
    date: string;
    startHour: number;
    endHour: number;
  } | null>(null);
  const [title, setTitle] = useState('');
  const [showConfirm, setShowConfirm] = useState(false);

  const otherUsers = users.filter((u) => u.id !== currentUserId);
  const selectedUser = selectedUserId ? users.find((u) => u.id === selectedUserId) : null;
  const selectedUserAvailability = selectedUserId
    ? availabilities.find((a) => a.userId === selectedUserId)
    : null;

  const userMeetings = getMeetingsForUser(meetings, currentUserId);

  const handleBlockSelect = (date: string, startHour: number, endHour: number) => {
    setSelectedBlock({ date, startHour, endHour });
    setShowConfirm(true);
  };

  const handleSchedule = () => {
    if (!selectedUserId || !selectedBlock || !title.trim()) return;

    addMeeting({
      organizerId: currentUserId,
      participantId: selectedUserId,
      date: selectedBlock.date,
      startHour: selectedBlock.startHour,
      endHour: selectedBlock.endHour,
      title: title.trim(),
    });

    // Reset state
    setSelectedBlock(null);
    setTitle('');
    setShowConfirm(false);
    setSelectedUserId(null);
  };

  const handleCancel = () => {
    setSelectedBlock(null);
    setTitle('');
    setShowConfirm(false);
  };

  const formatSelectedTime = () => {
    if (!selectedBlock) return '';
    const date = new Date(selectedBlock.date);
    const dayName = date.toLocaleDateString('en-US', { weekday: 'long' });
    const dateStr = date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });

    const formatHour = (h: number) => {
      const totalMin = Math.round(h * 60);
      const hours = Math.floor(totalMin / 60);
      const mins = totalMin % 60;
      const period = hours >= 12 ? 'PM' : 'AM';
      const displayHour = hours === 0 ? 12 : hours > 12 ? hours - 12 : hours;
      return mins === 0 ? `${displayHour}${period}` : `${displayHour}:${mins.toString().padStart(2, '0')}${period}`;
    };

    return `${dayName}, ${dateStr} at ${formatHour(selectedBlock.startHour)} - ${formatHour(selectedBlock.endHour)}`;
  };

  return (
    <div className="space-y-6">
      {/* Schedule New Meeting Section */}
      <div className="rounded-lg border border-zinc-200 bg-white p-4 sm:p-6 dark:border-zinc-800 dark:bg-zinc-900">
        <h2 className="mb-4 text-lg font-semibold text-zinc-900 dark:text-zinc-100">
          Schedule a Meeting
        </h2>

        {/* Step 1: User selection */}
        <div className="mb-6">
          <label className="mb-2 block text-sm font-medium text-zinc-700 dark:text-zinc-300">
            1. Select a person to meet with
          </label>
          <div className="flex flex-wrap gap-2">
            {otherUsers.map((user) => {
              const hasAvailability = availabilities.some(
                (a) => a.userId === user.id && a.slots.length > 0
              );
              const isSelected = selectedUserId === user.id;

              return (
                <button
                  key={user.id}
                  onClick={() => {
                    setSelectedUserId(isSelected ? null : user.id);
                    setSelectedBlock(null);
                    setShowConfirm(false);
                  }}
                  disabled={!hasAvailability}
                  className={`flex items-center gap-2 rounded-full px-3 py-2 text-sm font-medium transition-colors ${
                    isSelected
                      ? 'ring-2 ring-offset-2 ring-blue-500'
                      : ''
                  } ${
                    hasAvailability
                      ? 'hover:opacity-80'
                      : 'cursor-not-allowed opacity-50'
                  }`}
                  style={{
                    backgroundColor: user.avatarColor + '20',
                    color: user.avatarColor,
                  }}
                >
                  <div
                    className="flex h-6 w-6 items-center justify-center rounded-full text-xs text-white"
                    style={{ backgroundColor: user.avatarColor }}
                  >
                    {getUserInitials(user.name)}
                  </div>
                  <span className="hidden sm:inline">{user.name}</span>
                  <span className="sm:hidden">{user.name.split(' ')[0]}</span>
                  {!hasAvailability && (
                    <span className="text-xs text-zinc-400">(no availability)</span>
                  )}
                </button>
              );
            })}
          </div>
        </div>

        {/* Step 2: Duration selection */}
        {selectedUserId && (
          <div className="mb-6">
            <label className="mb-2 block text-sm font-medium text-zinc-700 dark:text-zinc-300">
              2. Choose meeting duration
            </label>
            <div className="flex flex-wrap gap-2">
              {DURATION_OPTIONS.map((option) => (
                <button
                  key={option.value}
                  onClick={() => {
                    setDuration(option.value);
                    setSelectedBlock(null);
                    setShowConfirm(false);
                  }}
                  className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                    duration === option.value
                      ? 'bg-blue-500 text-white'
                      : 'bg-zinc-100 text-zinc-700 hover:bg-zinc-200 dark:bg-zinc-800 dark:text-zinc-300 dark:hover:bg-zinc-700'
                  }`}
                >
                  {option.label}
                </button>
              ))}
            </div>
          </div>
        )}

        {/* Step 3: Time slot selection */}
        {selectedUser && selectedUserAvailability && !showConfirm && (
          <div>
            <label className="mb-3 block text-sm font-medium text-zinc-700 dark:text-zinc-300">
              3. Pick a time slot
            </label>
            <TimeSlotPicker
              duration={duration}
              slots={selectedUserAvailability.slots}
              meetings={meetings}
              currentUserId={currentUserId}
              participantId={selectedUserId!}
              selectedBlock={selectedBlock}
              onBlockSelect={handleBlockSelect}
            />
          </div>
        )}

        {/* Confirmation dialog */}
        {showConfirm && selectedUser && selectedBlock && (
          <div className="rounded-lg border border-blue-200 bg-blue-50 p-4 dark:border-blue-900 dark:bg-blue-950">
            <h3 className="font-medium text-zinc-900 dark:text-zinc-100 mb-3">
              Confirm Meeting
            </h3>

            <div className="mb-4 space-y-2 text-sm">
              <div className="flex items-center gap-2 text-zinc-600 dark:text-zinc-400">
                <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
                <span>with <strong className="text-zinc-900 dark:text-zinc-100">{selectedUser.name}</strong></span>
              </div>
              <div className="flex items-center gap-2 text-zinc-600 dark:text-zinc-400">
                <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <span>{formatSelectedTime()}</span>
              </div>
            </div>

            <div className="mb-4">
              <label
                htmlFor="meeting-title"
                className="mb-1 block text-sm font-medium text-zinc-700 dark:text-zinc-300"
              >
                Meeting Title
              </label>
              <input
                id="meeting-title"
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="e.g., Project sync, 1:1, Coffee chat"
                className="w-full rounded-lg border border-zinc-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 dark:border-zinc-600 dark:bg-zinc-900 dark:text-zinc-100"
                autoFocus
              />
            </div>

            <div className="flex gap-2">
              <button
                onClick={handleSchedule}
                disabled={!title.trim()}
                className={`flex-1 rounded-lg px-4 py-2 text-sm font-medium text-white transition-colors ${
                  title.trim()
                    ? 'bg-blue-500 hover:bg-blue-600'
                    : 'bg-zinc-400 cursor-not-allowed'
                }`}
              >
                Schedule Meeting
              </button>
              <button
                onClick={handleCancel}
                className="rounded-lg border border-zinc-300 px-4 py-2 text-sm font-medium text-zinc-700 hover:bg-zinc-100 dark:border-zinc-600 dark:text-zinc-300 dark:hover:bg-zinc-800"
              >
                Back
              </button>
            </div>
          </div>
        )}

        {!selectedUserId && (
          <div className="rounded-lg border border-dashed border-zinc-300 p-8 text-center dark:border-zinc-700">
            <p className="text-sm text-zinc-500 dark:text-zinc-400">
              Select a person above to start scheduling
            </p>
          </div>
        )}
      </div>

      {/* Scheduled Meetings Section */}
      <div className="rounded-lg border border-zinc-200 bg-white p-4 sm:p-6 dark:border-zinc-800 dark:bg-zinc-900">
        <h2 className="mb-4 text-lg font-semibold text-zinc-900 dark:text-zinc-100">
          Your Meetings
        </h2>
        <MeetingList
          meetings={userMeetings}
          currentUserId={currentUserId}
          users={users}
          onCancelMeeting={cancelMeeting}
        />
      </div>
    </div>
  );
}
