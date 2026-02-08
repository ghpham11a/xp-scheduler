import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { SchedulerState, SchedulerActions, TimeSlot, Meeting } from '@/types';
import { usersApi, availabilitiesApi, meetingsApi } from './api';

type SchedulerStore = SchedulerState & SchedulerActions;

export const useSchedulerStore = create<SchedulerStore>()(
  persist(
    (set, get) => ({
      // Initial state
      currentUserId: '',
      users: [],
      availabilities: [],
      meetings: [],
      isLoading: false,
      error: null,
      // Per-operation loading states
      isSavingAvailability: false,
      isCreatingMeeting: false,
      cancellingMeetingId: null,
      // Settings
      use24HourTime: false,

      // Fetch all data from API
      fetchData: async () => {
        set({ isLoading: true, error: null });
        try {
          const [users, availabilities, meetings] = await Promise.all([
            usersApi.getAll(),
            availabilitiesApi.getAll(),
            meetingsApi.getAll(),
          ]);

          set({
            users,
            availabilities,
            meetings,
            // Set current user to first user if not set
            currentUserId: get().currentUserId || users[0]?.id || '',
            isLoading: false,
          });
        } catch (error) {
          set({
            error: error instanceof Error ? error.message : 'Failed to fetch data',
            isLoading: false,
          });
        }
      },

      // Actions
      setCurrentUser: (userId: string) => {
        set({ currentUserId: userId });
      },

      setAvailability: async (userId: string, slots: TimeSlot[]) => {
        // Optimistically update local state
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

          return { availabilities: newAvailabilities, isSavingAvailability: true };
        });

        // Sync with server
        try {
          await availabilitiesApi.update(userId, slots);
        } catch (error) {
          // Revert on failure by refetching
          console.error('Failed to save availability:', error);
          get().fetchData();
        } finally {
          set({ isSavingAvailability: false });
        }
      },

      addMeeting: async (meeting: Omit<Meeting, 'id'>) => {
        set({ isCreatingMeeting: true });
        try {
          const newMeeting = await meetingsApi.create(meeting);
          set((state) => ({
            meetings: [...state.meetings, newMeeting],
          }));
        } catch (error) {
          console.error('Failed to create meeting:', error);
          throw error;
        } finally {
          set({ isCreatingMeeting: false });
        }
      },

      cancelMeeting: async (meetingId: string) => {
        // Set loading state for this specific meeting
        set({ cancellingMeetingId: meetingId });

        // Optimistically remove from local state
        const previousMeetings = get().meetings;
        set((state) => ({
          meetings: state.meetings.filter((m) => m.id !== meetingId),
        }));

        // Sync with server
        try {
          await meetingsApi.delete(meetingId);
        } catch (error) {
          // Revert on failure
          console.error('Failed to cancel meeting:', error);
          set({ meetings: previousMeetings });
        } finally {
          set({ cancellingMeetingId: null });
        }
      },

      setUse24HourTime: (value: boolean) => {
        set({ use24HourTime: value });
      },
    }),
    {
      name: 'scheduler-storage',
      // Persist user preferences locally
      partialize: (state) => ({
        currentUserId: state.currentUserId,
        use24HourTime: state.use24HourTime,
      }),
    }
  )
);
