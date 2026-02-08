package com.example.scheduler.features.availability

import com.example.scheduler.data.models.TimeSlot

// Helper function to add a 30-min time block
fun addTimeBlock(
    currentSlots: List<TimeSlot>,
    date: String,
    hour: Double
): List<TimeSlot> {
    return currentSlots + TimeSlot(date, hour, hour + 0.5)
}

// Helper function to remove a 30-min time block
fun removeTimeBlock(
    currentSlots: List<TimeSlot>,
    date: String,
    hour: Double
): List<TimeSlot> {
    val result = mutableListOf<TimeSlot>()

    for (slot in currentSlots) {
        if (slot.date != date) {
            result.add(slot)
            continue
        }

        // Check if this hour is within this slot
        if (hour >= slot.startHour && hour < slot.endHour) {
            // Split the slot if needed
            if (hour > slot.startHour) {
                result.add(TimeSlot(date, slot.startHour, hour))
            }
            if (hour + 0.5 < slot.endHour) {
                result.add(TimeSlot(date, hour + 0.5, slot.endHour))
            }
        } else {
            result.add(slot)
        }
    }

    return result
}
