import { User, TimeSlot, Availability, Meeting } from '@/types';

const API_BASE = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:6969';

async function fetchJson<T>(url: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${url}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
  });

  if (!res.ok) {
    throw new Error(`API error: ${res.status} ${res.statusText}`);
  }

  return res.json();
}

// Users API
export const usersApi = {
  getAll: () => fetchJson<User[]>('/users'),
  getOne: (id: string) => fetchJson<User>(`/users/${id}`),
};

// Availabilities API
export const availabilitiesApi = {
  getAll: () => fetchJson<Availability[]>('/availabilities'),
  getOne: (userId: string) => fetchJson<Availability>(`/availabilities/${userId}`),
  update: (userId: string, slots: TimeSlot[]) =>
    fetchJson<Availability>(`/availabilities/${userId}`, {
      method: 'PUT',
      body: JSON.stringify(slots),
    }),
};

// Meetings API
export const meetingsApi = {
  getAll: () => fetchJson<Meeting[]>('/meetings'),
  getOne: (id: string) => fetchJson<Meeting>(`/meetings/${id}`),
  create: (meeting: Omit<Meeting, 'id'>) =>
    fetchJson<Meeting>('/meetings', {
      method: 'POST',
      body: JSON.stringify(meeting),
    }),
  delete: (id: string) =>
    fetchJson<{ status: string; id: string }>(`/meetings/${id}`, {
      method: 'DELETE',
    }),
};
