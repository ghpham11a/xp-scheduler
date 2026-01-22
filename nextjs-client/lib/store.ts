import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { SchedulerState, SchedulerActions, TimeSlot, Meeting } from '@/types';
import { MOCK_USERS } from './constants';
import { generateId } from './utils';

type SchedulerStore = SchedulerState & SchedulerActions;

export const useSchedulerStore = create<SchedulerStore>()(
  persist(
    (set) => ({
      // Initial state
      currentUserId: MOCK_USERS[0].id,
      users: MOCK_USERS,
      availabilities: [],
      meetings: [],

      // Actions
      setCurrentUser: (userId: string) => {
        set({ currentUserId: userId });
      },

      setAvailability: (userId: string, slots: TimeSlot[]) => {
        set((state) => {
          const existing = state.availabilities.findIndex(
            (a) => a.userId === userId
          );
          const newAvailabilities = [...state.availabilities];

          if (existing >= 0) {
            newAvailabilities[existing] = { userId, slots };
          } else {
            newAvailabilities.push({ userId, slots });
          }

          return { availabilities: newAvailabilities };
        });
      },

      addMeeting: (meeting: Omit<Meeting, 'id'>) => {
        set((state) => ({
          meetings: [...state.meetings, { ...meeting, id: generateId() }],
        }));
      },

      cancelMeeting: (meetingId: string) => {
        set((state) => ({
          meetings: state.meetings.filter((m) => m.id !== meetingId),
        }));
      },
    }),
    {
      name: 'scheduler-storage',
      partialize: (state) => ({
        currentUserId: state.currentUserId,
        availabilities: state.availabilities,
        meetings: state.meetings,
      }),
    }
  )
);
