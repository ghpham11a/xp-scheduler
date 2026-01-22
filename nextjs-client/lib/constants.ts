import { User } from '@/types';

export const MOCK_USERS: User[] = [
  {
    id: 'user-1',
    name: 'Alice Johnson',
    email: 'alice@example.com',
    avatarColor: '#3B82F6', // blue
  },
  {
    id: 'user-2',
    name: 'Bob Smith',
    email: 'bob@example.com',
    avatarColor: '#10B981', // green
  },
  {
    id: 'user-3',
    name: 'Carol Williams',
    email: 'carol@example.com',
    avatarColor: '#F59E0B', // amber
  },
  {
    id: 'user-4',
    name: 'David Brown',
    email: 'david@example.com',
    avatarColor: '#EF4444', // red
  },
];

export const DAYS_OF_WEEK = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

export const HOURS = Array.from({ length: 24 }, (_, i) => i);

export const WORK_HOURS = { start: 8, end: 18 };
