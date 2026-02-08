export const MEETING_TITLE_MAX_LENGTH = 100;
export const MEETING_TITLE_MIN_LENGTH = 1;

export interface ValidationResult {
  isValid: boolean;
  error?: string;
  sanitized: string;
}

/**
 * Validates and sanitizes a meeting title.
 * - Trims leading/trailing whitespace
 * - Collapses multiple consecutive spaces into one
 * - Checks length constraints
 */
export function validateMeetingTitle(title: string): ValidationResult {
  // Sanitize: trim and collapse multiple spaces
  const sanitized = title.trim().replace(/\s+/g, ' ');

  if (sanitized.length < MEETING_TITLE_MIN_LENGTH) {
    return {
      isValid: false,
      error: 'Meeting title is required',
      sanitized,
    };
  }

  if (sanitized.length > MEETING_TITLE_MAX_LENGTH) {
    return {
      isValid: false,
      error: `Title must be ${MEETING_TITLE_MAX_LENGTH} characters or less`,
      sanitized,
    };
  }

  return {
    isValid: true,
    sanitized,
  };
}

/**
 * Sanitizes a string for safe display.
 * Trims whitespace and collapses multiple spaces.
 */
export function sanitizeString(value: string): string {
  return value.trim().replace(/\s+/g, ' ');
}
