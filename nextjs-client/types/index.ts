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
  isLoading: boolean;
  error: string | null;
  // Per-operation loading states
  isSavingAvailability: boolean;
  isCreatingMeeting: boolean;
  cancellingMeetingId: string | null;
  // Settings
  use24HourTime: boolean;
}

export interface SchedulerActions {
  fetchData: () => Promise<void>;
  setCurrentUser: (userId: string) => void;
  setAvailability: (userId: string, slots: TimeSlot[]) => Promise<void>;
  addMeeting: (meeting: Omit<Meeting, 'id'>) => Promise<void>;
  cancelMeeting: (meetingId: string) => Promise<void>;
  setUse24HourTime: (value: boolean) => void;
}
