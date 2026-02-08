'use client';

import { useSchedulerStore } from '@/lib/store';
import { getUserInitials } from '@/lib/utils';

export function SettingsView() {
  const { currentUserId, users, setCurrentUser, use24HourTime, setUse24HourTime } = useSchedulerStore();
  const currentUser = users.find((u) => u.id === currentUserId);

  return (
    <div className="rounded-lg border border-zinc-200 bg-white dark:border-zinc-800 dark:bg-zinc-900">
      {/* Profile Section */}
      <div className="border-b border-zinc-200 dark:border-zinc-700">
        <div className="px-4 py-3 bg-zinc-50 dark:bg-zinc-800/50">
          <h3 className="text-sm font-medium text-zinc-500 dark:text-zinc-400 uppercase tracking-wide">
            Profile
          </h3>
        </div>
        <div className="divide-y divide-zinc-100 dark:divide-zinc-800">
          {users.map((user) => (
            <button
              key={user.id}
              onClick={() => setCurrentUser(user.id)}
              className={`w-full flex items-center gap-3 px-4 py-3 hover:bg-zinc-50 dark:hover:bg-zinc-800/50 transition-colors ${
                user.id === currentUserId ? 'bg-blue-50 dark:bg-blue-900/20' : ''
              }`}
            >
              <div
                className="flex h-10 w-10 items-center justify-center rounded-full text-sm font-semibold text-white"
                style={{ backgroundColor: user.avatarColor }}
              >
                {getUserInitials(user.name)}
              </div>
              <div className="flex-1 text-left">
                <div className="font-medium text-zinc-900 dark:text-zinc-100">
                  {user.name}
                </div>
                <div className="text-sm text-zinc-500 dark:text-zinc-400">
                  {user.email}
                </div>
              </div>
              {user.id === currentUserId && (
                <svg className="h-5 w-5 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
              )}
            </button>
          ))}
        </div>
      </div>

      {/* Display Section */}
      <div className="border-b border-zinc-200 dark:border-zinc-700">
        <div className="px-4 py-3 bg-zinc-50 dark:bg-zinc-800/50">
          <h3 className="text-sm font-medium text-zinc-500 dark:text-zinc-400 uppercase tracking-wide">
            Display
          </h3>
        </div>
        <div className="px-4 py-4">
          <label className="flex items-center justify-between cursor-pointer">
            <div>
              <div className="font-medium text-zinc-900 dark:text-zinc-100">
                Use 24-hour time
              </div>
              <div className="text-sm text-zinc-500 dark:text-zinc-400">
                Display times in military format (e.g., 14:00 instead of 2 PM)
              </div>
            </div>
            <div className="relative">
              <input
                type="checkbox"
                checked={use24HourTime}
                onChange={(e) => setUse24HourTime(e.target.checked)}
                className="sr-only peer"
              />
              <div className="w-11 h-6 bg-zinc-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 dark:peer-focus:ring-blue-800 rounded-full peer dark:bg-zinc-700 peer-checked:after:translate-x-full rtl:peer-checked:after:-translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:start-[2px] after:bg-white after:border-zinc-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-zinc-600 peer-checked:bg-blue-600"></div>
            </div>
          </label>
        </div>
      </div>

      {/* About Section */}
      <div>
        <div className="px-4 py-3 bg-zinc-50 dark:bg-zinc-800/50">
          <h3 className="text-sm font-medium text-zinc-500 dark:text-zinc-400 uppercase tracking-wide">
            About
          </h3>
        </div>
        <div className="px-4 py-4 flex items-center justify-between">
          <span className="text-zinc-900 dark:text-zinc-100">Version</span>
          <span className="text-zinc-500 dark:text-zinc-400">1.0.0</span>
        </div>
      </div>
    </div>
  );
}
