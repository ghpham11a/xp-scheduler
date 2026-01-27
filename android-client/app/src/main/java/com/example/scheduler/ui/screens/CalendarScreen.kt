package com.example.scheduler.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scheduler.data.Availability
import com.example.scheduler.data.Meeting
import com.example.scheduler.data.User
import com.example.scheduler.ui.components.UserAvatar
import com.example.scheduler.utils.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

enum class CalendarViewMode {
    WEEK, DAY, AGENDA
}

@Composable
fun CalendarScreen(
    currentUserId: String,
    users: List<User>,
    availabilities: List<Availability>,
    meetings: List<Meeting>,
    onCancelMeeting: (String) -> Unit,
    getUserById: (String) -> User?,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(CalendarViewMode.WEEK) }
    var weekOffset by remember { mutableIntStateOf(0) }
    var selectedDay by remember { mutableStateOf(LocalDate.now()) }
    var selectedMeeting by remember { mutableStateOf<Meeting?>(null) }
    var showAllHours by remember { mutableStateOf(false) }

    val today = LocalDate.now()
    val weekStart = getWeekStart(today.plusWeeks(weekOffset.toLong()))
    val weekDays = (0..6).map { weekStart.plusDays(it.toLong()) }

    val currentUserMeetings = meetings.filter {
        it.organizerId == currentUserId || it.participantId == currentUserId
    }

    val currentUserAvailability = availabilities.find { it.userId == currentUserId }?.slots ?: emptyList()

    Column(modifier = modifier.fillMaxSize()) {
        // View mode selector and navigation
        CalendarHeader(
            viewMode = viewMode,
            onViewModeChange = { viewMode = it },
            weekStart = weekStart,
            weekDays = weekDays,
            weekOffset = weekOffset,
            selectedDay = selectedDay,
            onWeekOffsetChange = { weekOffset = it },
            onSelectedDayChange = { selectedDay = it },
            showAllHours = showAllHours,
            onShowAllHoursChange = { showAllHours = it }
        )

        when (viewMode) {
            CalendarViewMode.WEEK -> WeekView(
                weekDays = weekDays,
                currentUserMeetings = currentUserMeetings,
                currentUserAvailability = currentUserAvailability,
                showAllHours = showAllHours,
                getUserById = getUserById,
                onMeetingClick = { selectedMeeting = it }
            )
            CalendarViewMode.DAY -> DayView(
                selectedDay = selectedDay,
                weekDays = weekDays,
                onDaySelected = { selectedDay = it },
                currentUserMeetings = currentUserMeetings,
                currentUserAvailability = currentUserAvailability,
                showAllHours = showAllHours,
                getUserById = getUserById,
                onMeetingClick = { selectedMeeting = it }
            )
            CalendarViewMode.AGENDA -> AgendaView(
                weekDays = weekDays,
                currentUserMeetings = currentUserMeetings,
                currentUserAvailability = currentUserAvailability,
                getUserById = getUserById,
                onMeetingClick = { selectedMeeting = it }
            )
        }
    }

    // Meeting details dialog
    selectedMeeting?.let { meeting ->
        MeetingDetailsDialog(
            meeting = meeting,
            getUserById = getUserById,
            currentUserId = currentUserId,
            onDismiss = { selectedMeeting = null },
            onCancel = {
                onCancelMeeting(meeting.id)
                selectedMeeting = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarHeader(
    viewMode: CalendarViewMode,
    onViewModeChange: (CalendarViewMode) -> Unit,
    weekStart: LocalDate,
    weekDays: List<LocalDate>,
    weekOffset: Int,
    selectedDay: LocalDate,
    onWeekOffsetChange: (Int) -> Unit,
    onSelectedDayChange: (LocalDate) -> Unit,
    showAllHours: Boolean,
    onShowAllHoursChange: (Boolean) -> Unit
) {
    val weekEnd = weekDays.last()
    val today = LocalDate.now()

    Column {
        // View mode tabs and navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Navigation
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onWeekOffsetChange(weekOffset - 1) }) {
                    Icon(Icons.Default.ChevronLeft, "Previous week")
                }

                TextButton(
                    onClick = {
                        onWeekOffsetChange(0)
                        onSelectedDayChange(today)
                    }
                ) {
                    Text("Today")
                }

                IconButton(onClick = { onWeekOffsetChange(weekOffset + 1) }) {
                    Icon(Icons.Default.ChevronRight, "Next week")
                }
            }

            // Date range display
            Text(
                text = "${weekStart.month.name.take(3)} ${weekStart.dayOfMonth} - ${weekEnd.month.name.take(3)} ${weekEnd.dayOfMonth}",
                style = MaterialTheme.typography.titleMedium
            )
        }

        // View mode selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SingleChoiceSegmentedButtonRow {
                CalendarViewMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = viewMode == mode,
                        onClick = { onViewModeChange(mode) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = CalendarViewMode.entries.size
                        ),
                        icon = {
                            when (mode) {
                                CalendarViewMode.WEEK -> Icon(
                                    Icons.Default.CalendarViewWeek,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                CalendarViewMode.DAY -> Icon(
                                    Icons.Default.CalendarViewDay,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                CalendarViewMode.AGENDA -> Icon(
                                    Icons.AutoMirrored.Filled.List,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    ) {
                        Text(
                            text = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Show all hours toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("24h", style = MaterialTheme.typography.bodySmall)
                Switch(
                    checked = showAllHours,
                    onCheckedChange = onShowAllHoursChange,
                    modifier = Modifier.height(24.dp)
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun WeekView(
    weekDays: List<LocalDate>,
    currentUserMeetings: List<Meeting>,
    currentUserAvailability: List<com.example.scheduler.data.TimeSlot>,
    showAllHours: Boolean,
    getUserById: (String) -> User?,
    onMeetingClick: (Meeting) -> Unit
) {
    val today = LocalDate.now()
    val startHour = if (showAllHours) 0 else 6
    val endHour = if (showAllHours) 24 else 22
    val hours = (startHour until endHour).toList()

    val scrollState = rememberScrollState()

    Row(modifier = Modifier.fillMaxSize()) {
        // Time column
        Column(
            modifier = Modifier
                .width(50.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(40.dp)) // Header space
            hours.forEach { hour ->
                Box(
                    modifier = Modifier
                        .height(60.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Text(
                        text = formatHour(hour.toDouble()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }
        }

        // Days columns
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState())
        ) {
            weekDays.forEach { day ->
                Column(
                    modifier = Modifier
                        .width(100.dp)
                        .verticalScroll(scrollState)
                ) {
                    // Day header
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .fillMaxWidth()
                            .background(
                                if (day == today) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = SHORT_DAYS[day.dayOfWeek.value % 7],
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = day.dayOfMonth.toString(),
                                fontWeight = if (day == today) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    // Time slots
                    Box(modifier = Modifier.height((hours.size * 60).dp)) {
                        // Grid lines
                        hours.forEachIndexed { index, _ ->
                            HorizontalDivider(
                                modifier = Modifier.offset(y = (index * 60).dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }

                        // Availability blocks
                        currentUserAvailability
                            .filter { it.date == day.toIsoString() }
                            .forEach { slot ->
                                val top = ((slot.startHour - startHour) * 60).dp
                                val height = ((slot.endHour - slot.startHour) * 60).dp
                                if (slot.startHour >= startHour && slot.endHour <= endHour) {
                                    Box(
                                        modifier = Modifier
                                            .offset(y = top)
                                            .fillMaxWidth()
                                            .height(height)
                                            .background(
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                            )
                                    )
                                }
                            }

                        // Meetings
                        currentUserMeetings
                            .filter { it.date == day.toIsoString() }
                            .forEach { meeting ->
                                val top = ((meeting.startHour - startHour) * 60).dp
                                val height = ((meeting.endHour - meeting.startHour) * 60).dp
                                if (meeting.startHour >= startHour && meeting.endHour <= endHour) {
                                    val otherUserId = if (meeting.organizerId == currentUserMeetings.firstOrNull()?.let {
                                            if (it.organizerId == meeting.organizerId) it.organizerId else it.participantId
                                        } ?: meeting.organizerId
                                    ) meeting.organizerId else meeting.participantId

                                    val user = getUserById(
                                        if (meeting.organizerId != currentUserMeetings.firstOrNull()?.organizerId?.takeIf {
                                                currentUserMeetings.any { m -> m.organizerId == it || m.participantId == it }
                                            }) meeting.organizerId else meeting.participantId
                                    )
                                    val color = try {
                                        Color(android.graphics.Color.parseColor(user?.avatarColor ?: "#3B82F6"))
                                    } catch (e: Exception) {
                                        MaterialTheme.colorScheme.primary
                                    }

                                    Card(
                                        modifier = Modifier
                                            .offset(y = top)
                                            .fillMaxWidth()
                                            .height(height)
                                            .padding(1.dp)
                                            .clickable { onMeetingClick(meeting) },
                                        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.8f)),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = meeting.title,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(4.dp)
                                        )
                                    }
                                }
                            }

                        // Current time indicator
                        if (day == today) {
                            val now = LocalTime.now()
                            val currentHour = now.hour + now.minute / 60.0
                            if (currentHour >= startHour && currentHour <= endHour) {
                                val top = ((currentHour - startHour) * 60).dp
                                Box(
                                    modifier = Modifier
                                        .offset(y = top)
                                        .fillMaxWidth()
                                        .height(2.dp)
                                        .background(Color.Red)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayView(
    selectedDay: LocalDate,
    weekDays: List<LocalDate>,
    onDaySelected: (LocalDate) -> Unit,
    currentUserMeetings: List<Meeting>,
    currentUserAvailability: List<com.example.scheduler.data.TimeSlot>,
    showAllHours: Boolean,
    getUserById: (String) -> User?,
    onMeetingClick: (Meeting) -> Unit
) {
    val today = LocalDate.now()

    Column(modifier = Modifier.fillMaxSize()) {
        // Day selector
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(weekDays) { day ->
                FilterChip(
                    selected = day == selectedDay,
                    onClick = { onDaySelected(day) },
                    label = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(SHORT_DAYS[day.dayOfWeek.value % 7])
                            Text(
                                text = day.dayOfMonth.toString(),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (day == today)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            }
        }

        // Single day grid
        WeekView(
            weekDays = listOf(selectedDay),
            currentUserMeetings = currentUserMeetings,
            currentUserAvailability = currentUserAvailability,
            showAllHours = showAllHours,
            getUserById = getUserById,
            onMeetingClick = onMeetingClick
        )
    }
}

@Composable
private fun AgendaView(
    weekDays: List<LocalDate>,
    currentUserMeetings: List<Meeting>,
    currentUserAvailability: List<com.example.scheduler.data.TimeSlot>,
    getUserById: (String) -> User?,
    onMeetingClick: (Meeting) -> Unit
) {
    val meetingsByDay = currentUserMeetings
        .filter { meeting -> weekDays.any { it.toIsoString() == meeting.date } }
        .groupBy { it.date }
        .toSortedMap()

    if (meetingsByDay.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.EventBusy,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No meetings this week",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            meetingsByDay.forEach { (dateStr, meetings) ->
                val date = dateStr.toLocalDate()
                val dayAvailability = currentUserAvailability.filter { it.date == dateStr }
                val availableHours = dayAvailability.sumOf { it.endHour - it.startHour }

                item(key = dateStr) {
                    Column {
                        // Day header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatDateFull(date),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (availableHours > 0) {
                                Text(
                                    text = "${availableHours}h available",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Meetings for this day
                        meetings.sortedBy { it.startHour }.forEach { meeting ->
                            MeetingCard(
                                meeting = meeting,
                                getUserById = getUserById,
                                onClick = { onMeetingClick(meeting) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MeetingCard(
    meeting: Meeting,
    getUserById: (String) -> User?,
    onClick: () -> Unit
) {
    val otherUser = getUserById(meeting.participantId) ?: getUserById(meeting.organizerId)
    val color = try {
        Color(android.graphics.Color.parseColor(otherUser?.avatarColor ?: "#3B82F6"))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Color indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = meeting.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatTimeRange(meeting.startHour, meeting.endHour),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            otherUser?.let {
                UserAvatar(user = it, size = 32)
            }
        }
    }
}

@Composable
private fun MeetingDetailsDialog(
    meeting: Meeting,
    getUserById: (String) -> User?,
    currentUserId: String,
    onDismiss: () -> Unit,
    onCancel: () -> Unit
) {
    val organizer = getUserById(meeting.organizerId)
    val participant = getUserById(meeting.participantId)
    val isOrganizer = meeting.organizerId == currentUserId

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(meeting.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                    Text(formatTimeRange(meeting.startHour, meeting.endHour))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                    Text(formatDateFull(meeting.date.toLocalDate()))
                }

                HorizontalDivider()

                Text("Organizer", style = MaterialTheme.typography.labelMedium)
                organizer?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UserAvatar(user = it, size = 32)
                        Text(it.name)
                    }
                }

                Text("Participant", style = MaterialTheme.typography.labelMedium)
                participant?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UserAvatar(user = it, size = 32)
                        Text(it.name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        dismissButton = {
            if (isOrganizer) {
                TextButton(
                    onClick = onCancel,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Cancel Meeting")
                }
            }
        }
    )
}
