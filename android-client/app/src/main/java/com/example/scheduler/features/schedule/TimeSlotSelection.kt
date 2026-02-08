package com.example.scheduler.features.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.scheduler.data.models.Availability
import com.example.scheduler.data.models.Meeting
import com.example.scheduler.data.models.TimeSlot
import com.example.scheduler.data.models.User
import com.example.scheduler.shared.components.UserAvatar
import com.example.scheduler.utils.*

@Composable
fun TimeSlotSelection(
    currentUserId: String,
    participant: User,
    duration: MeetingDuration,
    availabilities: List<Availability>,
    meetings: List<Meeting>,
    onSelect: (String, Double) -> Unit
) {
    val next7Days = remember { getNextDays(7) }
    val participantSlots = availabilities.find { it.userId == participant.id }?.slots ?: emptyList()
    val currentUserSlots = availabilities.find { it.userId == currentUserId }?.slots ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Select Time",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        ) {
            Text(
                text = "Meeting with",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            UserAvatar(user = participant, size = 24)
            Text(
                text = participant.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "• ${duration.displayName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(next7Days) { date ->
                val dateStr = date.toIsoString()
                val dayParticipantSlots = participantSlots.filter { it.date == dateStr }
                val dayCurrentUserSlots = currentUserSlots.filter { it.date == dateStr }

                // Find available time slots
                val availableSlots = findAvailableSlots(
                    dateStr = dateStr,
                    participantSlots = dayParticipantSlots,
                    currentUserSlots = dayCurrentUserSlots,
                    duration = duration.hours,
                    meetings = meetings,
                    currentUserId = currentUserId,
                    participantId = participant.id
                )

                if (availableSlots.isNotEmpty()) {
                    Column {
                        Text(
                            text = formatDateRelative(date),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(availableSlots) { startHour ->
                                val hasConflict = hasConflict(
                                    date = dateStr,
                                    startHour = startHour,
                                    endHour = startHour + duration.hours,
                                    meetings = meetings,
                                    userId = currentUserId
                                ) || hasConflict(
                                    date = dateStr,
                                    startHour = startHour,
                                    endHour = startHour + duration.hours,
                                    meetings = meetings,
                                    userId = participant.id
                                )

                                OutlinedButton(
                                    onClick = { onSelect(dateStr, startHour) },
                                    enabled = !hasConflict,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(formatHour(startHour))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper function to find available time slots
fun findAvailableSlots(
    dateStr: String,
    participantSlots: List<TimeSlot>,
    currentUserSlots: List<TimeSlot>,
    duration: Double,
    meetings: List<Meeting>,
    currentUserId: String,
    participantId: String
): List<Double> {
    val availableSlots = mutableListOf<Double>()

    // Check each half-hour from 6am to 10pm
    var hour = 6.0
    while (hour + duration <= 22.0) {
        val endHour = hour + duration

        // Check if both users are available for this slot
        val participantAvailable = participantSlots.any { slot ->
            hour >= slot.startHour && endHour <= slot.endHour
        }
        val currentUserAvailable = currentUserSlots.any { slot ->
            hour >= slot.startHour && endHour <= slot.endHour
        }

        // Check for conflicts
        val hasCurrentUserConflict = hasConflict(dateStr, hour, endHour, meetings, currentUserId)
        val hasParticipantConflict = hasConflict(dateStr, hour, endHour, meetings, participantId)

        if (participantAvailable && currentUserAvailable && !hasCurrentUserConflict && !hasParticipantConflict) {
            availableSlots.add(hour)
        }

        hour += 0.5
    }

    return availableSlots
}
