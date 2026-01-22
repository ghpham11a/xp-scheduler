'use client';

import { useEffect, useState, ReactNode } from 'react';
import { useSchedulerStore } from '@/lib/store';

interface StoreProviderProps {
  children: ReactNode;
}

export function StoreProvider({ children }: StoreProviderProps) {
  const [isHydrated, setIsHydrated] = useState(false);
  const { fetchData, isLoading, error, users } = useSchedulerStore();

  useEffect(() => {
    setIsHydrated(true);
  }, []);

  // Fetch data from API once hydrated
  useEffect(() => {
    if (isHydrated && users.length === 0) {
      fetchData();
    }
  }, [isHydrated, users.length, fetchData]);

  if (!isHydrated || (isLoading && users.length === 0)) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center">
          <div className="h-8 w-8 mx-auto animate-spin rounded-full border-4 border-blue-500 border-t-transparent" />
          <p className="mt-2 text-sm text-zinc-500">Loading...</p>
        </div>
      </div>
    );
  }

  if (error && users.length === 0) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-center max-w-md mx-auto p-4">
          <div className="h-12 w-12 mx-auto mb-4 rounded-full bg-red-100 dark:bg-red-900/30 flex items-center justify-center">
            <svg className="h-6 w-6 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
          </div>
          <h2 className="text-lg font-semibold text-zinc-900 dark:text-zinc-100 mb-2">
            Connection Error
          </h2>
          <p className="text-sm text-zinc-500 dark:text-zinc-400 mb-4">
            {error}
          </p>
          <p className="text-xs text-zinc-400 dark:text-zinc-500 mb-4">
            Make sure the API server is running on port 6969
          </p>
          <button
            onClick={() => fetchData()}
            className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 text-sm font-medium"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  return <>{children}</>;
}
