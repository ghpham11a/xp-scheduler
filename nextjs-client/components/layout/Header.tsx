'use client';

import { useSchedulerStore } from '@/lib/store';
import { getUserInitials } from '@/lib/utils';

export function Header() {
  const { currentUserId, users } = useSchedulerStore();
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

        {currentUser && (
          <div className="flex items-center gap-3">
            <div
              className="flex h-8 w-8 items-center justify-center rounded-full text-xs font-semibold text-white"
              style={{ backgroundColor: currentUser.avatarColor }}
            >
              {getUserInitials(currentUser.name)}
            </div>
            <span className="text-sm text-zinc-500 dark:text-zinc-400">
              {currentUser.name}
            </span>
          </div>
        )}
      </div>
    </header>
  );
}
