'use client';

import { Meeting, User } from '@/types';
import { DAYS_OF_WEEK } from '@/lib/constants';
import { formatTimeRange, getDayOfWeekFromDate, getUserInitials } from '@/lib/utils';

interface MeetingListProps {
  meetings: Meeting[];
  currentUserId: string;
  users: User[];
  onCancelMeeting: (meetingId: string) => void;
}

export function MeetingList({
  meetings,
  currentUserId,
  users,
  onCancelMeeting,
}: MeetingListProps) {
  if (meetings.length === 0) {
    return (
      <div className="rounded-lg border border-dashed border-zinc-300 p-6 text-center dark:border-zinc-700">
        <p className="text-sm text-zinc-500 dark:text-zinc-400">
          No scheduled meetings yet
        </p>
      </div>
    );
  }

  const getOtherUser = (meeting: Meeting): User | undefined => {
    const otherId =
      meeting.organizerId === currentUserId
        ? meeting.participantId
        : meeting.organizerId;
    return users.find((u) => u.id === otherId);
  };

  const sortedMeetings = [...meetings].sort((a, b) => {
    const dateCompare = a.date.localeCompare(b.date);
    if (dateCompare !== 0) return dateCompare;
    return a.startHour - b.startHour;
  });

  return (
    <div className="space-y-3">
      {sortedMeetings.map((meeting) => {
        const otherUser = getOtherUser(meeting);
        const isOrganizer = meeting.organizerId === currentUserId;
        const dayOfWeek = getDayOfWeekFromDate(meeting.date);

        return (
          <div
            key={meeting.id}
            className="flex items-center justify-between rounded-lg border border-zinc-200 bg-white p-4 dark:border-zinc-700 dark:bg-zinc-800"
          >
            <div className="flex items-center gap-3">
              {otherUser && (
                <div
                  className="flex h-10 w-10 items-center justify-center rounded-full text-sm font-semibold text-white"
                  style={{ backgroundColor: otherUser.avatarColor }}
                >
                  {getUserInitials(otherUser.name)}
                </div>
              )}
              <div>
                <h4 className="font-medium text-zinc-900 dark:text-zinc-100">
                  {meeting.title}
                </h4>
                <div className="mt-0.5 text-sm text-zinc-500 dark:text-zinc-400">
                  {DAYS_OF_WEEK[dayOfWeek]} {meeting.date} &bull;{' '}
                  {formatTimeRange(meeting.startHour, meeting.endHour)}
                </div>
                <div className="mt-0.5 text-xs text-zinc-400 dark:text-zinc-500">
                  {isOrganizer ? (
                    <>with {otherUser?.name}</>
                  ) : (
                    <>organized by {otherUser?.name}</>
                  )}
                </div>
              </div>
            </div>
            <button
              onClick={() => onCancelMeeting(meeting.id)}
              className="rounded-lg px-3 py-1.5 text-sm text-red-600 hover:bg-red-50 dark:text-red-400 dark:hover:bg-red-900/20"
            >
              Cancel
            </button>
          </div>
        );
      })}
    </div>
  );
}
