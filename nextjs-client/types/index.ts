export interface User {
  id: string;
  name: string;
  email: string;
  avatarColor: string;
}

export interface TimeSlot {
  date: string;      // ISO date string (YYYY-MM-DD)
  startHour: number; // 0-23 (supports decimals for 30-min increments)
  endHour: number;   // 0-23
}

export interface Availability {
  userId: string;
  slots: TimeSlot[];
}

export interface Meeting {
  id: string;
  organizerId: string;
  participantId: string;
  date: string; // ISO date string (YYYY-MM-DD)
  startHour: number;
  endHour: number;
  title: string;
}

export interface SchedulerState {
  currentUserId: string;
  users: User[];
  availabilities: Availability[];
  meetings: Meeting[];
}

export interface SchedulerActions {
  setCurrentUser: (userId: string) => void;
  setAvailability: (userId: string, slots: TimeSlot[]) => void;
  addMeeting: (meeting: Omit<Meeting, 'id'>) => void;
  cancelMeeting: (meetingId: string) => void;
}
