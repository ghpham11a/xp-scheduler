package com.example.scheduler.utils

import com.example.scheduler.data.models.Meeting
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

import com.example.scheduler.data.models.TimeSlot

// Day names
val DAYS = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
val SHORT_DAYS = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

// Format decimal hour to time string (e.g., 9.5 -> "9:30 AM" or "09:30" in 24-hour format)
fun formatHour(hour: Double, use24HourFormat: Boolean = false): String {
    val hourInt = hour.toInt()
    val minutes = ((hour - hourInt) * 60).roundToInt()

    return if (use24HourFormat) {
        val displayHour = if (hourInt == 24) 0 else hourInt
        "${displayHour.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}"
    } else {
        val displayHour = when {
            hourInt == 0 || hourInt == 24 -> 12
            hourInt > 12 -> hourInt - 12
            else -> hourInt
        }
        val amPm = if (hourInt < 12) "AM" else "PM"
        if (minutes == 0) {
            "$displayHour $amPm"
        } else {
            "$displayHour:${minutes.toString().padStart(2, '0')} $amPm"
        }
    }
}

// Format time range (e.g., "9:30 AM - 10:30 AM" or "09:30 - 10:30" in 24-hour format)
fun formatTimeRange(startHour: Double, endHour: Double, use24HourFormat: Boolean = false): String {
    return "${formatHour(startHour, use24HourFormat)} - ${formatHour(endHour, use24HourFormat)}"
}

// Get next N days starting from today
fun getNextDays(count: Int): List<LocalDate> {
    val today = LocalDate.now()
    return (0 until count).map { today.plusDays(it.toLong()) }
}

// Format date as ISO string (YYYY-MM-DD)
fun LocalDate.toIsoString(): String {
    return this.format(DateTimeFormatter.ISO_LOCAL_DATE)
}

// Parse ISO string to LocalDate
fun String.toLocalDate(): LocalDate {
    return LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE)
}

// Format date for display (e.g., "Mon 23")
fun formatDateShort(date: LocalDate): String {
    val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    return "$dayName ${date.dayOfMonth}"
}

// Format date as "Today", "Tomorrow", or "Mon 23"
fun formatDateRelative(date: LocalDate): String {
    val today = LocalDate.now()
    return when {
        date == today -> "Today"
        date == today.plusDays(1) -> "Tomorrow"
        else -> formatDateShort(date)
    }
}

// Format full date (e.g., "Monday, January 23")
fun formatDateFull(date: LocalDate): String {
    val dayName = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val monthName = date.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    return "$dayName, $monthName ${date.dayOfMonth}"
}

// Merge adjacent time slots
fun mergeTimeSlots(slots: List<TimeSlot>): List<TimeSlot> {
    if (slots.isEmpty()) return emptyList()

    // Group by date
    val byDate = slots.groupBy { it.date }

    return byDate.flatMap { (date, dateSlots) ->
        val sorted = dateSlots.sortedBy { it.startHour }
        val merged = mutableListOf<TimeSlot>()

        for (slot in sorted) {
            if (merged.isEmpty()) {
                merged.add(slot)
            } else {
                val last = merged.last()
                if (last.endHour >= slot.startHour) {
                    // Merge overlapping/adjacent slots
                    merged[merged.lastIndex] = last.copy(
                        endHour = maxOf(last.endHour, slot.endHour)
                    )
                } else {
                    merged.add(slot)
                }
            }
        }
        merged
    }
}

// Check if a time slot conflicts with a meeting
fun hasConflict(
    date: String,
    startHour: Double,
    endHour: Double,
    meetings: List<Meeting>,
    userId: String
): Boolean {
    return meetings.any { meeting ->
        meeting.date == date &&
        (meeting.organizerId == userId || meeting.participantId == userId) &&
        startHour < meeting.endHour &&
        endHour > meeting.startHour
    }
}

// Get total available hours from slots
fun getTotalAvailableHours(slots: List<TimeSlot>): Double {
    return slots.sumOf { it.endHour - it.startHour }
}

// Get days with availability
fun getDaysWithAvailability(slots: List<TimeSlot>): Int {
    return slots.map { it.date }.distinct().size
}

// Check if a specific hour is within any slot
fun isHourInSlots(date: String, hour: Double, slots: List<TimeSlot>): Boolean {
    return slots.any { slot ->
        slot.date == date && hour >= slot.startHour && hour < slot.endHour
    }
}

// Get the start of the week (Sunday) for a given date
fun getWeekStart(date: LocalDate): LocalDate {
    val dayOfWeek = date.dayOfWeek.value % 7 // Sunday = 0
    return date.minusDays(dayOfWeek.toLong())
}

// Generate hours array for time grid (in half-hour increments)
fun generateHours(startHour: Int = 6, endHour: Int = 22): List<Double> {
    val hours = mutableListOf<Double>()
    var h = startHour.toDouble()
    while (h < endHour) {
        hours.add(h)
        h += 0.5
    }
    return hours
}

// Quick availability presets
enum class AvailabilityPreset(val displayName: String, val startHour: Double, val endHour: Double) {
    FULL_DAY("24h", 0.0, 24.0),
    BUSINESS("9-5", 9.0, 17.0),
    MORNING("Morning", 6.0, 12.0),
    AFTERNOON("Afternoon", 12.0, 18.0),
    EVENING("Evening", 18.0, 22.0)
}

// Duration options for meeting scheduling
enum class MeetingDuration(val displayName: String, val hours: Double) {
    FIFTEEN_MIN("15 min", 0.25),
    THIRTY_MIN("30 min", 0.5),
    FORTY_FIVE_MIN("45 min", 0.75),
    ONE_HOUR("1 hour", 1.0),
    NINETY_MIN("90 min", 1.5),
    TWO_HOURS("2 hours", 2.0)
}
