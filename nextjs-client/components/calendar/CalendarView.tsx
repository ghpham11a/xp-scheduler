'use client';

import { useMemo, useState } from 'react';
import { useSchedulerStore } from '@/lib/store';
import { Meeting } from '@/types';
import { formatTime, getUserInitials } from '@/lib/utils';

const DAY_NAMES = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
const DAY_NAMES_FULL = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
const MONTH_NAMES = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
const MONTH_NAMES_SHORT = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

type ViewMode = 'day' | 'month';

function getMonthDates(year: number, month: number): string[] {
  const dates: string[] = [];
  const daysInMonth = new Date(year, month + 1, 0).getDate();

  for (let day = 1; day <= daysInMonth; day++) {
    const date = new Date(year, month, day);
    dates.push(date.toISOString().split('T')[0]);
  }

  return dates;
}

interface CalendarViewProps {
  showAllHours: boolean;
}

export function CalendarView({ showAllHours }: CalendarViewProps) {
  const { currentUserId, users, meetings, cancelMeeting, cancellingMeetingId } = useSchedulerStore();
  const [viewMode, setViewMode] = useState<ViewMode>('day');
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [selectedMeeting, setSelectedMeeting] = useState<Meeting | null>(null);

  const currentUser = users.find((u) => u.id === currentUserId);

  // Get user's meetings
  const userMeetings = meetings.filter(
    (m) => m.organizerId === currentUserId || m.participantId === currentUserId
  );

  // For day view - get meetings for selected date
  const selectedDateString = selectedDate.toISOString().split('T')[0];
  const dayMeetings = userMeetings
    .filter((m) => m.date === selectedDateString)
    .sort((a, b) => a.startHour - b.startHour);

  // For month view - get all meetings in the month grouped by date
  const monthDates = useMemo(() =>
    getMonthDates(selectedDate.getFullYear(), selectedDate.getMonth()),
    [selectedDate.getFullYear(), selectedDate.getMonth()]
  );

  const monthMeetingsByDate = useMemo(() => {
    const monthDateSet = new Set(monthDates);
    const filtered = userMeetings.filter((m) => monthDateSet.has(m.date));
    const grouped = new Map<string, Meeting[]>();

    for (const meeting of filtered) {
      if (!grouped.has(meeting.date)) {
        grouped.set(meeting.date, []);
      }
      grouped.get(meeting.date)!.push(meeting);
    }

    // Sort meetings within each day
    grouped.forEach((meetings) => {
      meetings.sort((a, b) => a.startHour - b.startHour);
    });

    return grouped;
  }, [monthDates, userMeetings]);

  // Get dates that have meetings (sorted)
  const datesWithMeetings = useMemo(() =>
    Array.from(monthMeetingsByDate.keys()).sort(),
    [monthMeetingsByDate]
  );

  const getOtherUser = (meeting: Meeting) => {
    const otherId = meeting.organizerId === currentUserId
      ? meeting.participantId
      : meeting.organizerId;
    return users.find((u) => u.id === otherId);
  };

  const handleCancelMeeting = (meetingId: string) => {
    cancelMeeting(meetingId);
    setSelectedMeeting(null);
  };

  const todayString = new Date().toISOString().split('T')[0];
  const isToday = selectedDateString === todayString;

  // Navigation functions
  const goToPrevDay = () => {
    const newDate = new Date(selectedDate);
    newDate.setDate(newDate.getDate() - 1);
    setSelectedDate(newDate);
  };

  const goToNextDay = () => {
    const newDate = new Date(selectedDate);
    newDate.setDate(newDate.getDate() + 1);
    setSelectedDate(newDate);
  };

  const goToPrevMonth = () => {
    const newDate = new Date(selectedDate);
    newDate.setMonth(newDate.getMonth() - 1);
    setSelectedDate(newDate);
  };

  const goToNextMonth = () => {
    const newDate = new Date(selectedDate);
    newDate.setMonth(newDate.getMonth() + 1);
    setSelectedDate(newDate);
  };

  const goToToday = () => {
    setSelectedDate(new Date());
  };

  // Calculate total meeting hours
  const totalMeetingHours = (viewMode === 'day' ? dayMeetings : userMeetings.filter(m => monthDates.includes(m.date)))
    .reduce((acc, m) => acc + (m.endHour - m.startHour), 0);

  const meetingCount = viewMode === 'day'
    ? dayMeetings.length
    : datesWithMeetings.reduce((acc, date) => acc + (monthMeetingsByDate.get(date)?.length || 0), 0);

  return (
    <div className="rounded-lg border border-zinc-200 bg-white dark:border-zinc-800 dark:bg-zinc-900">
      {/* Header */}
      <div className="p-4 border-b border-zinc-200 dark:border-zinc-700">
        <div className="flex items-center justify-between mb-3">
          <div/>
          {/* View Mode Switcher */}
          <div className="flex items-center gap-1 bg-zinc-100 dark:bg-zinc-800 rounded-lg p-1">
            <button
              onClick={() => setViewMode('day')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-md text-sm font-medium transition-colors ${
                viewMode === 'day'
                  ? 'bg-white dark:bg-zinc-700 text-zinc-900 dark:text-zinc-100 shadow-sm'
                  : 'text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-100'
              }`}
            >
              <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              Day
            </button>
            <button
              onClick={() => setViewMode('month')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-md text-sm font-medium transition-colors ${
                viewMode === 'month'
                  ? 'bg-white dark:bg-zinc-700 text-zinc-900 dark:text-zinc-100 shadow-sm'
                  : 'text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-100'
              }`}
            >
              <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h7" />
              </svg>
              Month
            </button>
          </div>
        </div>

        {/* Navigation */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <button
              onClick={viewMode === 'day' ? goToPrevDay : goToPrevMonth}
              className="p-2 rounded-lg hover:bg-zinc-100 dark:hover:bg-zinc-800 text-zinc-600 dark:text-zinc-400"
            >
              <svg className="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
              </svg>
            </button>
            <button
              onClick={goToToday}
              className={`px-3 py-1.5 rounded-lg text-sm font-medium transition-colors ${
                isToday
                  ? 'bg-blue-100 text-blue-700 dark:bg-blue-900/40 dark:text-blue-300'
                  : 'hover:bg-zinc-100 dark:hover:bg-zinc-800 text-zinc-600 dark:text-zinc-400'
              }`}
            >
              Today
            </button>
            <button
              onClick={viewMode === 'day' ? goToNextDay : goToNextMonth}
              className="p-2 rounded-lg hover:bg-zinc-100 dark:hover:bg-zinc-800 text-zinc-600 dark:text-zinc-400"
            >
              <svg className="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
              </svg>
            </button>
          </div>

          {/* Date Label */}
          <div className="text-sm font-medium text-zinc-700 dark:text-zinc-300">
            {viewMode === 'day' ? (
              <>
                {DAY_NAMES_FULL[selectedDate.getDay()]}, {MONTH_NAMES[selectedDate.getMonth()]} {selectedDate.getDate()}, {selectedDate.getFullYear()}
              </>
            ) : (
              <>
                {MONTH_NAMES[selectedDate.getMonth()]} {selectedDate.getFullYear()}
              </>
            )}
          </div>
        </div>
      </div>

      {/* Content */}
      <div className="min-h-[300px]">
        {viewMode === 'day' ? (
          // Day View - Agenda for selected day
          <div>
            {dayMeetings.length === 0 ? (
              <div className="p-8 text-center">
                <div className="inline-flex h-12 w-12 items-center justify-center rounded-full bg-zinc-100 dark:bg-zinc-800 mb-3">
                  <svg className="h-6 w-6 text-zinc-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                </div>
                <p className="text-zinc-500 dark:text-zinc-400">No meetings scheduled for this day</p>
              </div>
            ) : (
              <div className="divide-y divide-zinc-100 dark:divide-zinc-800">
                {dayMeetings.map((meeting) => (
                  <MeetingRow
                    key={meeting.id}
                    meeting={meeting}
                    otherUser={getOtherUser(meeting)}
                    onClick={() => setSelectedMeeting(meeting)}
                  />
                ))}
              </div>
            )}
          </div>
        ) : (
          // Month View - Scrollable agenda for the month
          <div>
            {datesWithMeetings.length === 0 ? (
              <div className="p-8 text-center">
                <div className="inline-flex h-12 w-12 items-center justify-center rounded-full bg-zinc-100 dark:bg-zinc-800 mb-3">
                  <svg className="h-6 w-6 text-zinc-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                </div>
                <p className="text-zinc-500 dark:text-zinc-400">No meetings scheduled for this month</p>
              </div>
            ) : (
              <div className="divide-y divide-zinc-200 dark:divide-zinc-700">
                {datesWithMeetings.map((dateString) => {
                  const meetings = monthMeetingsByDate.get(dateString)!;
                  const date = new Date(dateString + 'T00:00:00');
                  const isDateToday = dateString === todayString;

                  return (
                    <div key={dateString} className={isDateToday ? 'bg-blue-50/50 dark:bg-blue-900/10' : ''}>
                      {/* Date Header */}
                      <div className="px-4 py-3 bg-zinc-50 dark:bg-zinc-800/50 sticky top-0">
                        <div className="flex items-center gap-2">
                          {isDateToday && (
                            <span className="px-2 py-0.5 text-xs font-medium rounded-full bg-blue-500 text-white">
                              Today
                            </span>
                          )}
                          <span className="font-medium text-zinc-900 dark:text-zinc-100">
                            {DAY_NAMES_FULL[date.getDay()]}
                          </span>
                          <span className="text-sm text-zinc-500 dark:text-zinc-400">
                            {MONTH_NAMES_SHORT[date.getMonth()]} {date.getDate()}
                          </span>
                          <span className="ml-auto text-xs text-zinc-400 dark:text-zinc-500">
                            {meetings.length} meeting{meetings.length !== 1 ? 's' : ''}
                          </span>
                        </div>
                      </div>

                      {/* Meetings for this day */}
                      <div className="divide-y divide-zinc-100 dark:divide-zinc-800">
                        {meetings.map((meeting) => (
                          <MeetingRow
                            key={meeting.id}
                            meeting={meeting}
                            otherUser={getOtherUser(meeting)}
                            onClick={() => setSelectedMeeting(meeting)}
                          />
                        ))}
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        )}
      </div>

      {/* Summary */}
      <div className="p-3 sm:p-4 border-t border-zinc-200 dark:border-zinc-700 bg-zinc-50 dark:bg-zinc-800/50">
        <div className="flex flex-wrap gap-3 sm:gap-4 text-sm">
          <div className="text-zinc-600 dark:text-zinc-400">
            <span className="font-medium text-zinc-900 dark:text-zinc-100">{meetingCount}</span> meeting{meetingCount !== 1 ? 's' : ''}
          </div>
          <div className="text-zinc-600 dark:text-zinc-400">
            <span className="font-medium text-zinc-900 dark:text-zinc-100">{totalMeetingHours}</span>h scheduled
          </div>
        </div>
      </div>

      {/* Meeting Details Modal */}
      {selectedMeeting && (
        <MeetingModal
          meeting={selectedMeeting}
          currentUserId={currentUserId}
          otherUser={getOtherUser(selectedMeeting)}
          onClose={() => setSelectedMeeting(null)}
          onCancel={() => handleCancelMeeting(selectedMeeting.id)}
          isCancelling={cancellingMeetingId === selectedMeeting.id}
        />
      )}
    </div>
  );
}

// Meeting Row Component
function MeetingRow({
  meeting,
  otherUser,
  onClick
}: {
  meeting: Meeting;
  otherUser: { id: string; name: string; email: string; avatarColor: string } | undefined;
  onClick: () => void;
}) {
  const duration = meeting.endHour - meeting.startHour;

  return (
    <div
      onClick={onClick}
      className="flex items-stretch cursor-pointer hover:bg-zinc-50 dark:hover:bg-zinc-800/50 transition-colors"
    >
      {/* Color bar */}
      <div
        className="w-1 shrink-0"
        style={{ backgroundColor: otherUser?.avatarColor || '#3B82F6' }}
      />

      {/* Time */}
      <div className="w-20 shrink-0 px-3 py-3 text-right">
        <div className="text-sm font-medium text-zinc-900 dark:text-zinc-100">
          {formatTime(meeting.startHour)}
        </div>
        <div className="text-xs text-zinc-400 dark:text-zinc-500">
          {duration}h
        </div>
      </div>

      {/* Meeting Info */}
      <div className="flex-1 px-3 py-3 min-w-0">
        <div className="font-medium text-zinc-900 dark:text-zinc-100 truncate">
          {meeting.title}
        </div>
        <div className="flex items-center gap-2 mt-1">
          <div
            className="flex h-5 w-5 items-center justify-center rounded-full text-xs text-white shrink-0"
            style={{ backgroundColor: otherUser?.avatarColor || '#3B82F6' }}
          >
            {getUserInitials(otherUser?.name || '?')}
          </div>
          <span className="text-sm text-zinc-500 dark:text-zinc-400 truncate">
            {otherUser?.name || 'Unknown'}
          </span>
        </div>
      </div>

      {/* Arrow */}
      <div className="flex items-center px-3">
        <svg className="h-4 w-4 text-zinc-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
        </svg>
      </div>
    </div>
  );
}

// Meeting Modal Component
function MeetingModal({
  meeting,
  currentUserId,
  otherUser,
  onClose,
  onCancel,
  isCancelling,
}: {
  meeting: Meeting;
  currentUserId: string;
  otherUser: { id: string; name: string; email: string; avatarColor: string } | undefined;
  onClose: () => void;
  onCancel: () => void;
  isCancelling?: boolean;
}) {
  return (
    <div
      className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-black/50"
      onClick={onClose}
    >
      <div
        className="bg-white dark:bg-zinc-900 rounded-t-xl sm:rounded-lg shadow-xl max-w-md w-full sm:mx-4 overflow-hidden"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Modal Header */}
        <div
          className="px-4 py-3"
          style={{ backgroundColor: otherUser?.avatarColor || '#3B82F6' }}
        >
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-semibold text-white">{meeting.title}</h3>
            <button
              onClick={onClose}
              className="text-white/80 hover:text-white p-1"
            >
              <svg className="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>

        {/* Modal Body */}
        <div className="p-4 space-y-4">
          {/* Participant */}
          <div className="flex items-center gap-3">
            <div
              className="flex h-10 w-10 items-center justify-center rounded-full text-sm font-semibold text-white"
              style={{ backgroundColor: otherUser?.avatarColor || '#3B82F6' }}
            >
              {getUserInitials(otherUser?.name || '?')}
            </div>
            <div>
              <div className="text-sm text-zinc-500 dark:text-zinc-400">
                {meeting.organizerId === currentUserId ? 'Meeting with' : 'Organized by'}
              </div>
              <div className="font-medium text-zinc-900 dark:text-zinc-100">
                {otherUser?.name || 'Unknown'}
              </div>
            </div>
          </div>

          {/* Date & Time */}
          <div className="flex items-start gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-full bg-zinc-100 dark:bg-zinc-800">
              <svg className="h-5 w-5 text-zinc-600 dark:text-zinc-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </div>
            <div>
              <div className="font-medium text-zinc-900 dark:text-zinc-100">
                {new Date(meeting.date).toLocaleDateString('en-US', {
                  weekday: 'long',
                  month: 'long',
                  day: 'numeric',
                  year: 'numeric'
                })}
              </div>
              <div className="text-sm text-zinc-500 dark:text-zinc-400">
                {formatTime(meeting.startHour)} - {formatTime(meeting.endHour)}
                <span className="ml-2">
                  ({meeting.endHour - meeting.startHour} hour{meeting.endHour - meeting.startHour !== 1 ? 's' : ''})
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Modal Footer */}
        <div className="px-4 py-3 bg-zinc-50 dark:bg-zinc-800/50 flex gap-2 justify-end">
          <button
            onClick={onClose}
            disabled={isCancelling}
            className="px-4 py-2 rounded-lg text-sm font-medium text-zinc-700 dark:text-zinc-300 hover:bg-zinc-200 dark:hover:bg-zinc-700 disabled:opacity-50"
          >
            Close
          </button>
          {meeting.organizerId === currentUserId && (
            <button
              onClick={onCancel}
              disabled={isCancelling}
              className="px-4 py-2 rounded-lg text-sm font-medium text-white bg-red-500 hover:bg-red-600 disabled:opacity-50"
            >
              {isCancelling ? (
                <span className="flex items-center gap-2">
                  <svg className="h-4 w-4 animate-spin" viewBox="0 0 24 24" fill="none">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                  </svg>
                  Cancelling...
                </span>
              ) : (
                'Cancel Meeting'
              )}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
