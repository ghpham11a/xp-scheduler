import { TimeSlot, Meeting } from '@/types';

export function formatHour(hour: number): string {
  const period = hour >= 12 ? 'PM' : 'AM';
  const displayHour = hour === 0 ? 12 : hour > 12 ? hour - 12 : hour;
  return `${displayHour}${period}`;
}

export function formatTime(hour: number): string {
  const totalMinutes = Math.round(hour * 60);
  const hours = Math.floor(totalMinutes / 60);
  const minutes = totalMinutes % 60;
  const period = hours >= 12 ? 'PM' : 'AM';
  const displayHour = hours === 0 ? 12 : hours > 12 ? hours - 12 : hours;

  if (minutes === 0) {
    return `${displayHour}${period}`;
  }
  return `${displayHour}:${minutes.toString().padStart(2, '0')}${period}`;
}

export function formatHourRange(startHour: number, endHour: number): string {
  return `${formatHour(startHour)} - ${formatHour(endHour)}`;
}

export function formatTimeRange(startHour: number, endHour: number): string {
  return `${formatTime(startHour)} - ${formatTime(endHour)}`;
}

export function generateId(): string {
  return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
}

export function mergeAdjacentSlots(slots: TimeSlot[]): TimeSlot[] {
  // Group by date
  const byDate = new Map<string, TimeSlot[]>();
  slots.forEach((slot) => {
    if (!byDate.has(slot.date)) byDate.set(slot.date, []);
    byDate.get(slot.date)!.push(slot);
  });

  const merged: TimeSlot[] = [];

  byDate.forEach((dateSlots) => {
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

export function getDayOfWeekFromDate(dateString: string): number {
  return new Date(dateString).getDay();
}

export function getDateForDayOfWeek(dayOfWeek: number): string {
  const today = new Date();
  const currentDay = today.getDay();
  const diff = dayOfWeek - currentDay;

  const targetDate = new Date(today);
  targetDate.setDate(today.getDate() + diff);

  return targetDate.toISOString().split('T')[0];
}

export function getMeetingsForUser(
  meetings: Meeting[],
  userId: string
): Meeting[] {
  return meetings.filter(
    (m) => m.organizerId === userId || m.participantId === userId
  );
}

export function getUserInitials(name: string): string {
  return name
    .split(' ')
    .map((n) => n[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);
}

export function hasTimeSlot(
  slots: TimeSlot[],
  dayOfWeek: number,
  hour: number
): boolean {
  const targetDate = getDateForDayOfWeek(dayOfWeek);
  return slots.some(
    (slot) =>
      slot.date === targetDate && hour >= slot.startHour && hour < slot.endHour
  );
}

export function getConflictsForSlot(
  meetings: Meeting[],
  userId: string,
  dayOfWeek: number,
  hour: number
): Meeting | null {
  const targetDate = getDateForDayOfWeek(dayOfWeek);
  return (
    meetings.find((meeting) => {
      if (meeting.date !== targetDate) return false;
      if (meeting.organizerId !== userId && meeting.participantId !== userId)
        return false;
      return hour >= meeting.startHour && hour < meeting.endHour;
    }) ?? null
  );
}

export function hasConflict(
  meetings: Meeting[],
  userId: string,
  date: string,
  startHour: number,
  endHour: number,
  excludeMeetingId?: string
): Meeting | null {
  return meetings.find((meeting) => {
    if (excludeMeetingId && meeting.id === excludeMeetingId) return false;
    if (meeting.date !== date) return false;
    if (meeting.organizerId !== userId && meeting.participantId !== userId) return false;

    // Check for time overlap
    const hasOverlap = startHour < meeting.endHour && endHour > meeting.startHour;
    return hasOverlap;
  }) ?? null;
}
