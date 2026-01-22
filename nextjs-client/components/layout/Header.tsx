'use client';

import { useSchedulerStore } from '@/lib/store';
import { getUserInitials } from '@/lib/utils';

export function Header() {
  const { currentUserId, users, setCurrentUser } = useSchedulerStore();
  const currentUser = users.find((u) => u.id === currentUserId);

  return (
    <header className="border-b border-zinc-200 bg-white px-6 py-4 dark:border-zinc-800 dark:bg-zinc-900">
      <div className="mx-auto flex max-w-6xl items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-blue-500 text-white font-semibold">
            S
          </div>
          <h1 className="text-xl font-semibold text-zinc-900 dark:text-zinc-100">
            Scheduler
          </h1>
        </div>

        <div className="flex items-center gap-3">
          <span className="text-sm text-zinc-500 dark:text-zinc-400">
            Logged in as:
          </span>
          <div className="relative">
            <select
              value={currentUserId}
              onChange={(e) => setCurrentUser(e.target.value)}
              className="appearance-none rounded-lg border border-zinc-300 bg-white py-2 pl-10 pr-8 text-sm font-medium text-zinc-900 focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 dark:border-zinc-700 dark:bg-zinc-800 dark:text-zinc-100"
            >
              {users.map((user) => (
                <option key={user.id} value={user.id}>
                  {user.name}
                </option>
              ))}
            </select>
            {currentUser && (
              <div
                className="absolute left-2 top-1/2 flex h-6 w-6 -translate-y-1/2 items-center justify-center rounded-full text-xs font-semibold text-white"
                style={{ backgroundColor: currentUser.avatarColor }}
              >
                {getUserInitials(currentUser.name)}
              </div>
            )}
            <div className="pointer-events-none absolute right-2 top-1/2 -translate-y-1/2 text-zinc-400">
              <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
              </svg>
            </div>
          </div>
        </div>
      </div>
    </header>
  );
}
