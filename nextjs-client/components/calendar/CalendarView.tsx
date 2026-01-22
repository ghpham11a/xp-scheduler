'use client';

import { useMemo, useState, useEffect } from 'react';
import { useSchedulerStore } from '@/lib/store';
import { Meeting, TimeSlot } from '@/types';
import { formatTime, getUserInitials } from '@/lib/utils';

const DAY_NAMES = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
const DAY_NAMES_FULL = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
const MONTH_NAMES = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

type ViewMode = 'week' | 'day' | 'agenda';

// Generate hours for display (we'll show 6am-10pm by default, with option to show all)
function getVisibleHours(showAllHours: boolean): number[] {
  if (showAllHours) {
    return Array.from({ length: 48 }, (_, i) => i * 0.5);
  }
  // 6am to 10pm
  return Array.from({ length: 32 }, (_, i) => 6 + i * 0.5);
}

function isSlotAvailable(slots: TimeSlot[], date: string, hour: number): boolean {
  return slots.some(
    (slot) =>
      slot.date === date &&
      hour >= slot.startHour &&
      hour + 0.5 <= slot.endHour
  );
}

function getWeekDates(weekOffset: number): { date: Date; dateString: string }[] {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const currentDay = today.getDay();

  const dates: { date: Date; dateString: string }[] = [];

  // Start from Sunday of the selected week
  for (let i = 0; i < 7; i++) {
    const diff = i - currentDay + (weekOffset * 7);
    const date = new Date(today);
    date.setDate(today.getDate() + diff);
    dates.push({
      date,
      dateString: date.toISOString().split('T')[0],
    });
  }

  return dates;
}

// Get current time as decimal hours
function getCurrentTimeDecimal(): number {
  const now = new Date();
  return now.getHours() + now.getMinutes() / 60;
}

// Check if two meetings overlap in time
function meetingsOverlap(a: Meeting, b: Meeting): boolean {
  return a.startHour < b.endHour && a.endHour > b.startHour;
}

// Group overlapping meetings and assign column positions
function getOverlappingGroups(meetings: Meeting[]): Map<string, { column: number; totalColumns: number }> {
  const result = new Map<string, { column: number; totalColumns: number }>();

  if (meetings.length === 0) return result;

  // Sort by start time
  const sorted = [...meetings].sort((a, b) => a.startHour - b.startHour);

  // Find groups of overlapping meetings
  const groups: Meeting[][] = [];
  let currentGroup: Meeting[] = [sorted[0]];

  for (let i = 1; i < sorted.length; i++) {
    const meeting = sorted[i];
    const overlapsWithGroup = currentGroup.some(m => meetingsOverlap(m, meeting));

    if (overlapsWithGroup) {
      currentGroup.push(meeting);
    } else {
      groups.push(currentGroup);
      currentGroup = [meeting];
    }
  }
  groups.push(currentGroup);

  // Assign columns within each group
  for (const group of groups) {
    const columns: Meeting[][] = [];

    for (const meeting of group) {
      let placed = false;
      for (let col = 0; col < columns.length; col++) {
        const canPlace = !columns[col].some(m => meetingsOverlap(m, meeting));
        if (canPlace) {
          columns[col].push(meeting);
          result.set(meeting.id, { column: col, totalColumns: 0 });
          placed = true;
          break;
        }
      }
      if (!placed) {
        columns.push([meeting]);
        result.set(meeting.id, { column: columns.length - 1, totalColumns: 0 });
      }
    }

    // Update total columns for all meetings in group
    for (const meeting of group) {
      const info = result.get(meeting.id)!;
      info.totalColumns = columns.length;
    }
  }

  return result;
}

export function CalendarView() {
  const { currentUserId, users, availabilities, meetings, cancelMeeting } = useSchedulerStore();
  const [showAllHours, setShowAllHours] = useState(false);
  const [weekOffset, setWeekOffset] = useState(0);
  const [currentTime, setCurrentTime] = useState(getCurrentTimeDecimal());
  const [selectedMeeting, setSelectedMeeting] = useState<Meeting | null>(null);
  const [viewMode, setViewMode] = useState<ViewMode>('week');
  const [selectedDayIndex, setSelectedDayIndex] = useState(new Date().getDay());
  const [isMobile, setIsMobile] = useState(false);

  // Detect mobile on mount and window resize
  useEffect(() => {
    const checkMobile = () => {
      const mobile = window.innerWidth < 768;
      setIsMobile(mobile);
      // Auto-switch to day view on mobile if currently on week view
      if (mobile && viewMode === 'week') {
        setViewMode('day');
      }
    };

    checkMobile();
    window.addEventListener('resize', checkMobile);
    return () => window.removeEventListener('resize', checkMobile);
  }, [viewMode]);

  // Update current time every minute
  useEffect(() => {
    const interval = setInterval(() => {
      setCurrentTime(getCurrentTimeDecimal());
    }, 60000);
    return () => clearInterval(interval);
  }, []);

  const currentUser = users.find((u) => u.id === currentUserId);
  const currentAvailability = availabilities.find((a) => a.userId === currentUserId);
  const slots = currentAvailability?.slots ?? [];

  const weekDates = useMemo(() => getWeekDates(weekOffset), [weekOffset]);
  const visibleHours = getVisibleHours(showAllHours);

  // Get week date range for header
  const weekStartDate = weekDates[0]?.date;
  const weekEndDate = weekDates[6]?.date;
  const weekLabel = weekStartDate && weekEndDate
    ? `${MONTH_NAMES[weekStartDate.getMonth()]} ${weekStartDate.getDate()} - ${MONTH_NAMES[weekEndDate.getMonth()]} ${weekEndDate.getDate()}, ${weekEndDate.getFullYear()}`
    : '';

  // Get meetings for current user within this week
  const weekDateStrings = new Set(weekDates.map(d => d.dateString));
  const userMeetings = meetings.filter(
    (m) => (m.organizerId === currentUserId || m.participantId === currentUserId) &&
           weekDateStrings.has(m.date)
  );

  const getOtherUser = (meeting: Meeting) => {
    const otherId = meeting.organizerId === currentUserId
      ? meeting.participantId
      : meeting.organizerId;
    return users.find((u) => u.id === otherId);
  };

  // Calculate meeting position and height with column support
  const getMeetingStyle = (meeting: Meeting, hourHeight: number, column: number, totalColumns: number) => {
    const minHour = showAllHours ? 0 : 6;
    const startOffset = (meeting.startHour - minHour) * hourHeight * 2;
    const duration = meeting.endHour - meeting.startHour;
    const height = duration * hourHeight * 2;

    const width = totalColumns > 1 ? `calc(${100 / totalColumns}% - 4px)` : 'calc(100% - 4px)';
    const left = totalColumns > 1 ? `calc(${(column / totalColumns) * 100}% + 2px)` : '2px';

    return {
      top: `${startOffset}px`,
      height: `${height}px`,
      width,
      left,
    };
  };

  // Calculate current time indicator position
  const getCurrentTimePosition = () => {
    const minHour = showAllHours ? 0 : 6;
    const maxHour = showAllHours ? 24 : 22;
    if (currentTime < minHour || currentTime > maxHour) return null;
    return (currentTime - minHour) * 24; // 24px per hour (12px per 30min * 2)
  };

  // Calculate hours available this week
  const weekAvailableHours = slots
    .filter(slot => weekDateStrings.has(slot.date))
    .reduce((acc, slot) => acc + (slot.endHour - slot.startHour), 0);

  // Calculate total meeting hours this week
  const weekMeetingHours = userMeetings.reduce((acc, m) => acc + (m.endHour - m.startHour), 0);

  const handleCancelMeeting = (meetingId: string) => {
    cancelMeeting(meetingId);
    setSelectedMeeting(null);
  };

  const isCurrentWeek = weekOffset === 0;
  const todayString = new Date().toISOString().split('T')[0];

  // Selected day for day view
  const selectedDay = weekDates[selectedDayIndex];
  const selectedDayMeetings = selectedDay
    ? userMeetings.filter(m => m.date === selectedDay.dateString).sort((a, b) => a.startHour - b.startHour)
    : [];

  // Navigate days
  const goToPrevDay = () => {
    if (selectedDayIndex > 0) {
      setSelectedDayIndex(i => i - 1);
    } else {
      setWeekOffset(w => w - 1);
      setSelectedDayIndex(6);
    }
  };

  const goToNextDay = () => {
    if (selectedDayIndex < 6) {
      setSelectedDayIndex(i => i + 1);
    } else {
      setWeekOffset(w => w + 1);
      setSelectedDayIndex(0);
    }
  };

  const goToToday = () => {
    setWeekOffset(0);
    setSelectedDayIndex(new Date().getDay());
  };

  // View mode icons
  const ViewIcon = ({ mode }: { mode: ViewMode }) => {
    if (mode === 'week') {
      return (
        <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 10h16M4 14h16M4 18h16" />
        </svg>
      );
    }
    if (mode === 'day') {
      return (
        <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
        </svg>
      );
    }
    return (
      <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h7" />
      </svg>
    );
  };

  // Render the week grid view
  const renderWeekView = () => (
    <div className="overflow-x-auto">
      <div className="min-w-[800px]">
        {/* Day Headers */}
        <div className="flex border-b border-zinc-200 dark:border-zinc-700 sticky top-0 bg-white dark:bg-zinc-900 z-10">
          <div className="w-16 shrink-0 p-2 text-xs text-zinc-500 dark:text-zinc-400" />
          {weekDates.map(({ date, dateString }) => {
            const isToday = date.toDateString() === new Date().toDateString();
            return (
              <div
                key={dateString}
                className={`flex-1 p-2 text-center border-l border-zinc-200 dark:border-zinc-700 ${
                  isToday ? 'bg-blue-50 dark:bg-blue-900/20' : ''
                }`}
              >
                <div className={`text-sm font-medium ${isToday ? 'text-blue-600 dark:text-blue-400' : 'text-zinc-900 dark:text-zinc-100'}`}>
                  {DAY_NAMES[date.getDay()]}
                </div>
                <div className={`text-xs ${isToday ? 'text-blue-500 dark:text-blue-400' : 'text-zinc-500 dark:text-zinc-400'}`}>
                  {date.getDate()}
                </div>
              </div>
            );
          })}
        </div>

        {/* Time Grid */}
        <div className="relative flex">
          {/* Time Labels */}
          <div className="w-16 shrink-0">
            {visibleHours.filter((_, i) => i % 2 === 0).map((hour) => (
              <div
                key={hour}
                className="h-12 border-b border-zinc-100 dark:border-zinc-800 pr-2 text-right text-xs text-zinc-400 dark:text-zinc-500"
              >
                {formatTime(hour)}
              </div>
            ))}
          </div>

          {/* Day Columns */}
          {weekDates.map(({ dateString }) => {
            const dayMeetings = userMeetings.filter((m) => m.date === dateString);
            const isToday = dateString === todayString;
            const overlappingInfo = getOverlappingGroups(dayMeetings);
            const currentTimePos = getCurrentTimePosition();

            return (
              <div
                key={dateString}
                className={`flex-1 border-l border-zinc-200 dark:border-zinc-700 relative ${
                  isToday ? 'bg-blue-50/50 dark:bg-blue-900/10' : ''
                }`}
              >
                {/* Availability Background + Grid Lines */}
                {visibleHours.map((hour, index) => {
                  const isAvailable = isSlotAvailable(slots, dateString, hour);
                  const isHourMark = index % 2 === 0;

                  return (
                    <div
                      key={hour}
                      className={`h-6 ${
                        isHourMark ? 'border-b border-zinc-100 dark:border-zinc-800' : 'border-b border-zinc-50 dark:border-zinc-850'
                      } ${
                        isAvailable
                          ? 'bg-blue-100/70 dark:bg-blue-900/30'
                          : ''
                      }`}
                    />
                  );
                })}

                {/* Meetings Overlay */}
                {dayMeetings.map((meeting) => {
                  const otherUser = getOtherUser(meeting);
                  const posInfo = overlappingInfo.get(meeting.id) || { column: 0, totalColumns: 1 };
                  const style = getMeetingStyle(meeting, 12, posInfo.column, posInfo.totalColumns);

                  // Check if meeting is within visible range
                  const minHour = showAllHours ? 0 : 6;
                  const maxHour = showAllHours ? 24 : 22;
                  if (meeting.endHour <= minHour || meeting.startHour >= maxHour) {
                    return null;
                  }

                  return (
                    <div
                      key={meeting.id}
                      onClick={() => setSelectedMeeting(meeting)}
                      className="absolute rounded px-1 py-0.5 overflow-hidden cursor-pointer hover:ring-2 hover:ring-blue-400 hover:z-10 transition-shadow"
                      style={{
                        ...style,
                        backgroundColor: otherUser?.avatarColor || '#3B82F6',
                      }}
                      title={`${meeting.title} with ${otherUser?.name || 'Unknown'}`}
                    >
                      <div className="text-xs font-medium text-white truncate">
                        {meeting.title}
                      </div>
                      {parseFloat(style.height) > 30 && (
                        <div className="text-xs text-white/80 truncate">
                          {otherUser?.name}
                        </div>
                      )}
                      {parseFloat(style.height) > 45 && (
                        <div className="text-xs text-white/70">
                          {formatTime(meeting.startHour)} - {formatTime(meeting.endHour)}
                        </div>
                      )}
                    </div>
                  );
                })}

                {/* Current Time Indicator */}
                {isToday && isCurrentWeek && currentTimePos !== null && (
                  <div
                    className="absolute left-0 right-0 z-20 pointer-events-none"
                    style={{ top: `${currentTimePos}px` }}
                  >
                    <div className="relative">
                      <div className="absolute -left-1 -top-1 h-2.5 w-2.5 rounded-full bg-red-500" />
                      <div className="h-0.5 bg-red-500" />
                    </div>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );

  // Render the single day view (mobile-friendly)
  const renderDayView = () => {
    if (!selectedDay) return null;

    const isToday = selectedDay.dateString === todayString;
    const overlappingInfo = getOverlappingGroups(selectedDayMeetings);
    const currentTimePos = getCurrentTimePosition();

    return (
      <div>
        {/* Day Selector - horizontal scroll on mobile */}
        <div className="flex border-b border-zinc-200 dark:border-zinc-700 overflow-x-auto">
          {weekDates.map(({ date, dateString }, index) => {
            const isDayToday = dateString === todayString;
            const isSelected = index === selectedDayIndex;
            return (
              <button
                key={dateString}
                onClick={() => setSelectedDayIndex(index)}
                className={`flex-1 min-w-[48px] p-2 text-center transition-colors ${
                  isSelected
                    ? 'bg-blue-500 text-white'
                    : isDayToday
                    ? 'bg-blue-50 dark:bg-blue-900/20'
                    : 'hover:bg-zinc-100 dark:hover:bg-zinc-800'
                }`}
              >
                <div className={`text-xs font-medium ${
                  isSelected ? 'text-white' : isDayToday ? 'text-blue-600 dark:text-blue-400' : 'text-zinc-900 dark:text-zinc-100'
                }`}>
                  {DAY_NAMES[date.getDay()]}
                </div>
                <div className={`text-lg font-semibold ${
                  isSelected ? 'text-white' : isDayToday ? 'text-blue-500 dark:text-blue-400' : 'text-zinc-700 dark:text-zinc-300'
                }`}>
                  {date.getDate()}
                </div>
              </button>
            );
          })}
        </div>

        {/* Day Header */}
        <div className="p-3 bg-zinc-50 dark:bg-zinc-800/50 border-b border-zinc-200 dark:border-zinc-700">
          <div className="flex items-center justify-between">
            <div>
              <div className="font-medium text-zinc-900 dark:text-zinc-100">
                {DAY_NAMES_FULL[selectedDay.date.getDay()]}
              </div>
              <div className="text-sm text-zinc-500 dark:text-zinc-400">
                {selectedDay.date.toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' })}
              </div>
            </div>
            <div className="text-sm text-zinc-500 dark:text-zinc-400">
              {selectedDayMeetings.length} meeting{selectedDayMeetings.length !== 1 ? 's' : ''}
            </div>
          </div>
        </div>

        {/* Time Grid */}
        <div className="relative flex">
          {/* Time Labels */}
          <div className="w-14 shrink-0">
            {visibleHours.filter((_, i) => i % 2 === 0).map((hour) => (
              <div
                key={hour}
                className="h-12 border-b border-zinc-100 dark:border-zinc-800 pr-2 text-right text-xs text-zinc-400 dark:text-zinc-500"
              >
                {formatTime(hour)}
              </div>
            ))}
          </div>

          {/* Day Column */}
          <div className={`flex-1 relative ${isToday ? 'bg-blue-50/50 dark:bg-blue-900/10' : ''}`}>
            {/* Availability Background + Grid Lines */}
            {visibleHours.map((hour, index) => {
              const isAvailable = isSlotAvailable(slots, selectedDay.dateString, hour);
              const isHourMark = index % 2 === 0;

              return (
                <div
                  key={hour}
                  className={`h-6 ${
                    isHourMark ? 'border-b border-zinc-100 dark:border-zinc-800' : 'border-b border-zinc-50 dark:border-zinc-850'
                  } ${
                    isAvailable ? 'bg-blue-100/70 dark:bg-blue-900/30' : ''
                  }`}
                />
              );
            })}

            {/* Meetings Overlay */}
            {selectedDayMeetings.map((meeting) => {
              const otherUser = getOtherUser(meeting);
              const posInfo = overlappingInfo.get(meeting.id) || { column: 0, totalColumns: 1 };
              const style = getMeetingStyle(meeting, 12, posInfo.column, posInfo.totalColumns);

              const minHour = showAllHours ? 0 : 6;
              const maxHour = showAllHours ? 24 : 22;
              if (meeting.endHour <= minHour || meeting.startHour >= maxHour) {
                return null;
              }

              return (
                <div
                  key={meeting.id}
                  onClick={() => setSelectedMeeting(meeting)}
                  className="absolute rounded-lg px-2 py-1 overflow-hidden cursor-pointer hover:ring-2 hover:ring-blue-400 hover:z-10 transition-shadow"
                  style={{
                    ...style,
                    backgroundColor: otherUser?.avatarColor || '#3B82F6',
                  }}
                >
                  <div className="text-sm font-medium text-white truncate">
                    {meeting.title}
                  </div>
                  <div className="text-xs text-white/80 truncate">
                    {otherUser?.name}
                  </div>
                  <div className="text-xs text-white/70">
                    {formatTime(meeting.startHour)} - {formatTime(meeting.endHour)}
                  </div>
                </div>
              );
            })}

            {/* Current Time Indicator */}
            {isToday && isCurrentWeek && currentTimePos !== null && (
              <div
                className="absolute left-0 right-0 z-20 pointer-events-none"
                style={{ top: `${currentTimePos}px` }}
              >
                <div className="relative">
                  <div className="absolute -left-1 -top-1 h-2.5 w-2.5 rounded-full bg-red-500" />
                  <div className="h-0.5 bg-red-500" />
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    );
  };

  // Render the agenda list view (most mobile-friendly)
  const renderAgendaView = () => {
    // Group meetings by date
    const meetingsByDate = new Map<string, Meeting[]>();
    for (const meeting of userMeetings) {
      if (!meetingsByDate.has(meeting.date)) {
        meetingsByDate.set(meeting.date, []);
      }
      meetingsByDate.get(meeting.date)!.push(meeting);
    }

    // Sort each day's meetings by time
    meetingsByDate.forEach((dayMeetings) => {
      dayMeetings.sort((a, b) => a.startHour - b.startHour);
    });

    // Get sorted dates
    const sortedDates = Array.from(meetingsByDate.keys()).sort();

    return (
      <div className="divide-y divide-zinc-200 dark:divide-zinc-700">
        {sortedDates.length === 0 ? (
          <div className="p-8 text-center">
            <div className="inline-flex h-12 w-12 items-center justify-center rounded-full bg-zinc-100 dark:bg-zinc-800 mb-3">
              <svg className="h-6 w-6 text-zinc-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </div>
            <p className="text-zinc-500 dark:text-zinc-400">No meetings this week</p>
          </div>
        ) : (
          sortedDates.map((dateString) => {
            const dayMeetings = meetingsByDate.get(dateString)!;
            const date = new Date(dateString + 'T00:00:00');
            const isToday = dateString === todayString;
            const daySlots = slots.filter(s => s.date === dateString);
            const dayAvailableHours = daySlots.reduce((acc, s) => acc + (s.endHour - s.startHour), 0);

            return (
              <div key={dateString} className={isToday ? 'bg-blue-50/50 dark:bg-blue-900/10' : ''}>
                {/* Date Header */}
                <div className="px-4 py-3 bg-zinc-50 dark:bg-zinc-800/50 sticky top-0">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      {isToday && (
                        <span className="px-2 py-0.5 text-xs font-medium rounded-full bg-blue-500 text-white">
                          Today
                        </span>
                      )}
                      <span className="font-medium text-zinc-900 dark:text-zinc-100">
                        {DAY_NAMES_FULL[date.getDay()]}
                      </span>
                      <span className="text-sm text-zinc-500 dark:text-zinc-400">
                        {MONTH_NAMES[date.getMonth()]} {date.getDate()}
                      </span>
                    </div>
                    {dayAvailableHours > 0 && (
                      <span className="text-xs text-zinc-400 dark:text-zinc-500">
                        {dayAvailableHours}h available
                      </span>
                    )}
                  </div>
                </div>

                {/* Meetings for this day */}
                <div className="divide-y divide-zinc-100 dark:divide-zinc-800">
                  {dayMeetings.map((meeting) => {
                    const otherUser = getOtherUser(meeting);
                    const duration = meeting.endHour - meeting.startHour;

                    return (
                      <div
                        key={meeting.id}
                        onClick={() => setSelectedMeeting(meeting)}
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
                  })}
                </div>
              </div>
            );
          })
        )}
      </div>
    );
  };

  return (
    <div className="rounded-lg border border-zinc-200 bg-white dark:border-zinc-800 dark:bg-zinc-900">
      {/* Header */}
      <div className="p-4 border-b border-zinc-200 dark:border-zinc-700">
        <div className="flex items-center justify-between mb-3">
          <div className="flex items-center gap-3">
            {currentUser && (
              <div
                className="hidden sm:flex h-10 w-10 items-center justify-center rounded-full text-sm font-semibold text-white"
                style={{ backgroundColor: currentUser.avatarColor }}
              >
                {getUserInitials(currentUser.name)}
              </div>
            )}
            <div>
              <h2 className="text-lg font-semibold text-zinc-900 dark:text-zinc-100">
                Calendar
              </h2>
              <p className="text-sm text-zinc-500 dark:text-zinc-400 hidden sm:block">
                Your availability and scheduled meetings
              </p>
            </div>
          </div>

          {/* View Mode Switcher */}
          <div className="flex items-center gap-1 bg-zinc-100 dark:bg-zinc-800 rounded-lg p-1">
            {(['day', 'week', 'agenda'] as ViewMode[]).map((mode) => (
              <button
                key={mode}
                onClick={() => setViewMode(mode)}
                className={`flex items-center gap-1.5 px-2 sm:px-3 py-1.5 rounded-md text-sm font-medium transition-colors ${
                  viewMode === mode
                    ? 'bg-white dark:bg-zinc-700 text-zinc-900 dark:text-zinc-100 shadow-sm'
                    : 'text-zinc-600 dark:text-zinc-400 hover:text-zinc-900 dark:hover:text-zinc-100'
                }`}
              >
                <ViewIcon mode={mode} />
                <span className="hidden sm:inline capitalize">{mode}</span>
              </button>
            ))}
          </div>
        </div>

        {/* Navigation */}
        <div className="flex items-center justify-between gap-2">
          <div className="flex items-center gap-1 sm:gap-2">
            <button
              onClick={viewMode === 'day' ? goToPrevDay : () => setWeekOffset(w => w - 1)}
              className="p-1.5 sm:p-2 rounded-lg hover:bg-zinc-100 dark:hover:bg-zinc-800 text-zinc-600 dark:text-zinc-400"
              title={viewMode === 'day' ? 'Previous day' : 'Previous week'}
            >
              <svg className="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
              </svg>
            </button>
            <button
              onClick={goToToday}
              className={`px-2 sm:px-3 py-1.5 rounded-lg text-sm font-medium transition-colors ${
                isCurrentWeek && (viewMode !== 'day' || selectedDay?.dateString === todayString)
                  ? 'bg-blue-100 text-blue-700 dark:bg-blue-900/40 dark:text-blue-300'
                  : 'hover:bg-zinc-100 dark:hover:bg-zinc-800 text-zinc-600 dark:text-zinc-400'
              }`}
            >
              Today
            </button>
            <button
              onClick={viewMode === 'day' ? goToNextDay : () => setWeekOffset(w => w + 1)}
              className="p-1.5 sm:p-2 rounded-lg hover:bg-zinc-100 dark:hover:bg-zinc-800 text-zinc-600 dark:text-zinc-400"
              title={viewMode === 'day' ? 'Next day' : 'Next week'}
            >
              <svg className="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
              </svg>
            </button>
          </div>

          {/* Week Label */}
          <div className="text-sm font-medium text-zinc-700 dark:text-zinc-300 text-center">
            {weekLabel}
          </div>

          {/* Controls */}
          <label className="hidden sm:flex items-center gap-2 text-sm text-zinc-600 dark:text-zinc-400">
            <input
              type="checkbox"
              checked={showAllHours}
              onChange={(e) => setShowAllHours(e.target.checked)}
              className="rounded border-zinc-300 dark:border-zinc-600"
            />
            <span className="hidden md:inline">Show all 24 hours</span>
            <span className="md:hidden">24h</span>
          </label>
        </div>

        {/* Legend - only show on week/day view */}
        {viewMode !== 'agenda' && (
          <div className="flex items-center gap-4 mt-3 text-sm">
            <div className="flex items-center gap-2">
              <div className="h-3 w-3 rounded bg-blue-100 dark:bg-blue-900/40 border border-blue-200 dark:border-blue-800" />
              <span className="text-zinc-600 dark:text-zinc-400 text-xs">Available</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="h-3 w-3 rounded bg-blue-500" />
              <span className="text-zinc-600 dark:text-zinc-400 text-xs">Meeting</span>
            </div>
            {isCurrentWeek && (
              <div className="flex items-center gap-2">
                <div className="h-0.5 w-3 bg-red-500" />
                <span className="text-zinc-600 dark:text-zinc-400 text-xs">Now</span>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Calendar Content */}
      {viewMode === 'week' && renderWeekView()}
      {viewMode === 'day' && renderDayView()}
      {viewMode === 'agenda' && renderAgendaView()}

      {/* Summary */}
      <div className="p-3 sm:p-4 border-t border-zinc-200 dark:border-zinc-700 bg-zinc-50 dark:bg-zinc-800/50">
        <div className="flex flex-wrap gap-3 sm:gap-4 text-sm">
          <div className="text-zinc-600 dark:text-zinc-400">
            <span className="font-medium text-zinc-900 dark:text-zinc-100">{userMeetings.length}</span> meeting{userMeetings.length !== 1 ? 's' : ''}
          </div>
          <div className="text-zinc-600 dark:text-zinc-400">
            <span className="font-medium text-zinc-900 dark:text-zinc-100">{weekMeetingHours}</span>h scheduled
          </div>
          <div className="text-zinc-600 dark:text-zinc-400">
            <span className="font-medium text-zinc-900 dark:text-zinc-100">{weekAvailableHours}</span>h available
          </div>
        </div>
      </div>

      {/* Meeting Details Modal */}
      {selectedMeeting && (
        <div
          className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-black/50"
          onClick={() => setSelectedMeeting(null)}
        >
          <div
            className="bg-white dark:bg-zinc-900 rounded-t-xl sm:rounded-lg shadow-xl max-w-md w-full sm:mx-4 overflow-hidden"
            onClick={(e) => e.stopPropagation()}
          >
            {/* Modal Header */}
            <div
              className="px-4 py-3"
              style={{ backgroundColor: getOtherUser(selectedMeeting)?.avatarColor || '#3B82F6' }}
            >
              <div className="flex items-center justify-between">
                <h3 className="text-lg font-semibold text-white">{selectedMeeting.title}</h3>
                <button
                  onClick={() => setSelectedMeeting(null)}
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
                  style={{ backgroundColor: getOtherUser(selectedMeeting)?.avatarColor || '#3B82F6' }}
                >
                  {getUserInitials(getOtherUser(selectedMeeting)?.name || '?')}
                </div>
                <div>
                  <div className="text-sm text-zinc-500 dark:text-zinc-400">
                    {selectedMeeting.organizerId === currentUserId ? 'Meeting with' : 'Organized by'}
                  </div>
                  <div className="font-medium text-zinc-900 dark:text-zinc-100">
                    {getOtherUser(selectedMeeting)?.name || 'Unknown'}
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
                    {new Date(selectedMeeting.date).toLocaleDateString('en-US', {
                      weekday: 'long',
                      month: 'long',
                      day: 'numeric',
                      year: 'numeric'
                    })}
                  </div>
                  <div className="text-sm text-zinc-500 dark:text-zinc-400">
                    {formatTime(selectedMeeting.startHour)} - {formatTime(selectedMeeting.endHour)}
                    <span className="ml-2">
                      ({selectedMeeting.endHour - selectedMeeting.startHour} hour{selectedMeeting.endHour - selectedMeeting.startHour !== 1 ? 's' : ''})
                    </span>
                  </div>
                </div>
              </div>
            </div>

            {/* Modal Footer */}
            <div className="px-4 py-3 bg-zinc-50 dark:bg-zinc-800/50 flex gap-2 justify-end">
              <button
                onClick={() => setSelectedMeeting(null)}
                className="px-4 py-2 rounded-lg text-sm font-medium text-zinc-700 dark:text-zinc-300 hover:bg-zinc-200 dark:hover:bg-zinc-700"
              >
                Close
              </button>
              <button
                onClick={() => handleCancelMeeting(selectedMeeting.id)}
                className="px-4 py-2 rounded-lg text-sm font-medium text-white bg-red-500 hover:bg-red-600"
              >
                Cancel Meeting
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
