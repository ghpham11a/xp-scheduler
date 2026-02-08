import { User, TimeSlot, Availability, Meeting } from '@/types';

const API_BASE = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:6969';
const TIMEOUT_MS = 10000; // 10 second timeout
const MAX_RETRIES = 2;
const RETRY_DELAY_MS = 1000;

export class ApiError extends Error {
  constructor(
    message: string,
    public status?: number,
    public isRetryable: boolean = false
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

function delay(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

async function fetchWithTimeout(
  url: string,
  options?: RequestInit
): Promise<Response> {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), TIMEOUT_MS);

  try {
    const response = await fetch(url, {
      ...options,
      signal: controller.signal,
    });
    return response;
  } finally {
    clearTimeout(timeoutId);
  }
}

async function fetchJson<T>(url: string, options?: RequestInit): Promise<T> {
  let lastError: Error | null = null;

  for (let attempt = 0; attempt <= MAX_RETRIES; attempt++) {
    try {
      const res = await fetchWithTimeout(`${API_BASE}${url}`, {
        ...options,
        headers: {
          'Content-Type': 'application/json',
          ...options?.headers,
        },
      });

      if (!res.ok) {
        // Don't retry client errors (4xx), only server errors (5xx)
        const isServerError = res.status >= 500;
        throw new ApiError(
          `Request failed: ${res.status} ${res.statusText}`,
          res.status,
          isServerError
        );
      }

      return res.json();
    } catch (error) {
      lastError = error as Error;

      // Check if error is retryable
      const isRetryable =
        error instanceof ApiError
          ? error.isRetryable
          : error instanceof TypeError || // Network error
            (error instanceof DOMException && error.name === 'AbortError'); // Timeout

      // Don't retry if not retryable or we've exhausted retries
      if (!isRetryable || attempt === MAX_RETRIES) {
        break;
      }

      // Wait before retrying with exponential backoff
      await delay(RETRY_DELAY_MS * Math.pow(2, attempt));
    }
  }

  // Throw appropriate error
  if (lastError instanceof DOMException && lastError.name === 'AbortError') {
    throw new ApiError('Request timed out. Please try again.', undefined, true);
  }

  if (lastError instanceof TypeError) {
    throw new ApiError(
      'Network error. Please check your connection.',
      undefined,
      true
    );
  }

  if (lastError instanceof ApiError) {
    throw lastError;
  }

  throw new ApiError(
    lastError?.message || 'An unexpected error occurred',
    undefined,
    false
  );
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
