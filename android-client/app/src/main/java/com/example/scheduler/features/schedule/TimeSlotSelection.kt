package com.example.scheduler.features.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.scheduler.data.models.Availability
import com.example.scheduler.data.models.Meeting
import com.example.scheduler.data.models.TimeSlot
import com.example.scheduler.data.models.User
import com.example.scheduler.features.calendar.MonthAgendaView
import com.example.scheduler.shared.components.UserAvatar
import com.example.scheduler.utils.*

@Composable
fun TimeSlotSelection(
    currentUserId: String,
    participant: User,
    availabilities: List<Availability>,
    meetings: List<Meeting>,
    use24HourFormat: Boolean = false,
    onSelect: (String, Double, MeetingDuration) -> Unit,
    onBack: () -> Unit
) {
    var selectedDuration by remember { mutableStateOf(MeetingDuration.THIRTY_MIN) }
    val next7Days = remember { getNextDays(7) }
    val participantSlots = availabilities.find { it.userId == participant.id }?.slots ?: emptyList()
    val currentUserSlots = availabilities.find { it.userId == currentUserId }?.slots ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        TextButton(
            onClick = onBack
        ) {
            Text("Back")
        }

        // Header with participant info
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(
                text = "Schedule with",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            UserAvatar(user = participant, size = 24)
            Text(
                text = participant.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }

        // Duration selection chips
        Text(
            text = "Duration",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            items(MeetingDuration.entries.toList()) { duration ->
                FilterChip(
                    selected = selectedDuration == duration,
                    onClick = { selectedDuration = duration },
                    label = { Text(duration.displayName) }
                )
            }
        }

        // Time slots header
        Text(
            text = "Available Times",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Time slots by day
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(next7Days) { date ->
                val dateStr = date.toIsoString()
                val dayParticipantSlots = participantSlots.filter { it.date == dateStr }
                val dayCurrentUserSlots = currentUserSlots.filter { it.date == dateStr }

                // Find available time slots with duration-based increments
                val availableSlots = findAvailableSlots(
                    dateStr = dateStr,
                    participantSlots = dayParticipantSlots,
                    currentUserSlots = dayCurrentUserSlots,
                    duration = selectedDuration.hours,
                    meetings = meetings,
                    currentUserId = currentUserId,
                    participantId = participant.id
                )

                if (availableSlots.isNotEmpty()) {
                    Column {
                        Text(
                            text = formatDateRelative(date),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Wrap time blocks in a flow layout
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableSlots.forEach { startHour ->
                                OutlinedButton(
                                    onClick = { onSelect(dateStr, startHour, selectedDuration) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(formatHour(startHour, use24HourFormat))
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
// Increments by the duration amount (e.g., 15min duration = slots at 12:00, 12:15, 12:30...)
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

    // Increment by duration amount (minimum 0.25 = 15 min)
    val increment = maxOf(duration, 0.25)

    // Check each slot from 6am to 10pm
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

        hour += increment
    }

    return availableSlots
}
