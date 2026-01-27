package com.example.scheduler.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.scheduler.data.Availability
import com.example.scheduler.data.Meeting
import com.example.scheduler.data.TimeSlot
import com.example.scheduler.data.User
import com.example.scheduler.ui.components.UserAvatar
import com.example.scheduler.utils.*
import java.time.LocalDate

enum class ScheduleStep {
    SELECT_PARTICIPANT,
    SELECT_DURATION,
    SELECT_TIME,
    CONFIRM
}

@Composable
fun ScheduleScreen(
    currentUserId: String,
    users: List<User>,
    availabilities: List<Availability>,
    meetings: List<Meeting>,
    onScheduleMeeting: (
        organizerId: String,
        participantId: String,
        date: String,
        startHour: Double,
        endHour: Double,
        title: String
    ) -> Unit,
    onCancelMeeting: (String) -> Unit,
    getUserById: (String) -> User?,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf(ScheduleStep.SELECT_PARTICIPANT) }
    var selectedParticipant by remember { mutableStateOf<User?>(null) }
    var selectedDuration by remember { mutableStateOf<MeetingDuration?>(null) }
    var selectedSlot by remember { mutableStateOf<Pair<String, Double>?>(null) } // date, startHour
    var meetingTitle by remember { mutableStateOf("") }

    val otherUsers = users.filter { it.id != currentUserId }
    val currentUserMeetings = meetings.filter {
        it.organizerId == currentUserId || it.participantId == currentUserId
    }

    fun resetWizard() {
        currentStep = ScheduleStep.SELECT_PARTICIPANT
        selectedParticipant = null
        selectedDuration = null
        selectedSlot = null
        meetingTitle = ""
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Progress indicator
        if (currentStep != ScheduleStep.SELECT_PARTICIPANT) {
            WizardProgress(
                currentStep = currentStep,
                onBack = {
                    when (currentStep) {
                        ScheduleStep.SELECT_DURATION -> currentStep = ScheduleStep.SELECT_PARTICIPANT
                        ScheduleStep.SELECT_TIME -> currentStep = ScheduleStep.SELECT_DURATION
                        ScheduleStep.CONFIRM -> currentStep = ScheduleStep.SELECT_TIME
                        else -> {}
                    }
                },
                onReset = { resetWizard() }
            )
        }

        when (currentStep) {
            ScheduleStep.SELECT_PARTICIPANT -> ParticipantSelection(
                otherUsers = otherUsers,
                availabilities = availabilities,
                onSelect = { user ->
                    selectedParticipant = user
                    currentStep = ScheduleStep.SELECT_DURATION
                }
            )

            ScheduleStep.SELECT_DURATION -> DurationSelection(
                onSelect = { duration ->
                    selectedDuration = duration
                    currentStep = ScheduleStep.SELECT_TIME
                }
            )

            ScheduleStep.SELECT_TIME -> TimeSlotSelection(
                currentUserId = currentUserId,
                participant = selectedParticipant!!,
                duration = selectedDuration!!,
                availabilities = availabilities,
                meetings = meetings,
                onSelect = { date, startHour ->
                    selectedSlot = date to startHour
                    currentStep = ScheduleStep.CONFIRM
                }
            )

            ScheduleStep.CONFIRM -> ConfirmationScreen(
                participant = selectedParticipant!!,
                duration = selectedDuration!!,
                selectedDate = selectedSlot!!.first,
                selectedStartHour = selectedSlot!!.second,
                meetingTitle = meetingTitle,
                onTitleChange = { meetingTitle = it },
                onConfirm = {
                    onScheduleMeeting(
                        currentUserId,
                        selectedParticipant!!.id,
                        selectedSlot!!.first,
                        selectedSlot!!.second,
                        selectedSlot!!.second + selectedDuration!!.hours,
                        meetingTitle
                    )
                    resetWizard()
                }
            )
        }

        // Meeting list (always visible at bottom)
        if (currentStep == ScheduleStep.SELECT_PARTICIPANT && currentUserMeetings.isNotEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            MeetingListSection(
                meetings = currentUserMeetings,
                currentUserId = currentUserId,
                getUserById = getUserById,
                onCancelMeeting = onCancelMeeting
            )
        }
    }
}

@Composable
private fun WizardProgress(
    currentStep: ScheduleStep,
    onBack: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Go back")
        }

        // Step indicators
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScheduleStep.entries.take(4).forEachIndexed { index, step ->
                if (index > 0) {
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(2.dp)
                            .background(
                                if (currentStep.ordinal > index - 1)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                }
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (currentStep.ordinal >= index)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (index + 1).toString(),
                        color = if (currentStep.ordinal >= index)
                            Color.White
                        else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        TextButton(onClick = onReset) {
            Text("Cancel")
        }
    }
}

@Composable
private fun ParticipantSelection(
    otherUsers: List<User>,
    availabilities: List<Availability>,
    onSelect: (User) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Schedule a Meeting",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Select who you want to meet with",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(otherUsers) { user ->
                val userAvailability = availabilities.find { it.userId == user.id }
                val hasAvailability = (userAvailability?.slots?.isNotEmpty()) == true

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = hasAvailability) { onSelect(user) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (hasAvailability)
                            MaterialTheme.colorScheme.surface
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        UserAvatar(user = user, size = 48)

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (hasAvailability) {
                                    val hours = userAvailability?.slots?.sumOf { it.endHour - it.startHour } ?: 0.0
                                    "${hours.toInt()}h available"
                                } else "No availability set",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (hasAvailability)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error
                            )
                        }

                        if (hasAvailability) {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DurationSelection(
    onSelect: (MeetingDuration) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Meeting Duration",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "How long should the meeting be?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        // Duration grid
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MeetingDuration.entries.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { duration ->
                        OutlinedButton(
                            onClick = { onSelect(duration) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            Text(duration.displayName)
                        }
                    }
                    // Fill empty space if row has less than 3 items
                    repeat(3 - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeSlotSelection(
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

@Composable
private fun ConfirmationScreen(
    participant: User,
    duration: MeetingDuration,
    selectedDate: String,
    selectedStartHour: Double,
    meetingTitle: String,
    onTitleChange: (String) -> Unit,
    onConfirm: () -> Unit
) {
    val date = selectedDate.toLocalDate()
    val endHour = selectedStartHour + duration.hours

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Confirm Meeting",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Meeting summary card
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null)
                    Column {
                        Text("Participant", style = MaterialTheme.typography.labelSmall)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            UserAvatar(user = participant, size = 24)
                            Text(participant.name, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                    Column {
                        Text("Date", style = MaterialTheme.typography.labelSmall)
                        Text(formatDateFull(date), fontWeight = FontWeight.Medium)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                    Column {
                        Text("Time", style = MaterialTheme.typography.labelSmall)
                        Text(
                            "${formatTimeRange(selectedStartHour, endHour)} (${duration.displayName})",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Title input
        OutlinedTextField(
            value = meetingTitle,
            onValueChange = onTitleChange,
            label = { Text("Meeting Title") },
            placeholder = { Text("Enter a title for this meeting") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth(),
            enabled = meetingTitle.isNotBlank()
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Schedule Meeting")
        }
    }
}

@Composable
private fun MeetingListSection(
    meetings: List<Meeting>,
    currentUserId: String,
    getUserById: (String) -> User?,
    onCancelMeeting: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Your Meetings",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val sortedMeetings = meetings.sortedWith(compareBy({ it.date }, { it.startHour }))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sortedMeetings, key = { it.id }) { meeting ->
                val otherUserId = if (meeting.organizerId == currentUserId)
                    meeting.participantId else meeting.organizerId
                val otherUser = getUserById(otherUserId)
                val isOrganizer = meeting.organizerId == currentUserId

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        otherUser?.let {
                            UserAvatar(user = it, size = 40)
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = meeting.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "with ${otherUser?.name ?: "Unknown"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${formatDateRelative(meeting.date.toLocalDate())} • ${formatTimeRange(meeting.startHour, meeting.endHour)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isOrganizer) {
                            IconButton(
                                onClick = { onCancelMeeting(meeting.id) }
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Cancel meeting",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper function to find available time slots
private fun findAvailableSlots(
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
