package com.example.scheduler.features.calendar

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.scheduler.data.models.Meeting
import com.example.scheduler.data.models.User
import com.example.scheduler.shared.components.UserAvatar
import com.example.scheduler.utils.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

enum class CalendarViewMode {
    DAY, MONTH
}

@Composable
fun CalendarScreen(
    currentUserId: String,
    viewModel: CalendarViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    // Update ViewModel with current user ID when it changes
    LaunchedEffect(currentUserId) {
        viewModel.setCurrentUserId(currentUserId)
    }

    var viewMode by remember { mutableStateOf(CalendarViewMode.DAY) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedMeeting by remember { mutableStateOf<Meeting?>(null) }

    val today = LocalDate.now()

    val currentUserMeetings = state.meetings.filter {
        it.organizerId == currentUserId || it.participantId == currentUserId
    }

    // Day view meetings
    val dayMeetings = currentUserMeetings
        .filter { it.date == selectedDate.toIsoString() }
        .sortedBy { it.startHour }

    // Month view meetings grouped by date
    val yearMonth = YearMonth.of(selectedDate.year, selectedDate.month)
    val monthDates = (1..yearMonth.lengthOfMonth()).map {
        LocalDate.of(selectedDate.year, selectedDate.month, it).toIsoString()
    }.toSet()

    val monthMeetingsByDate = currentUserMeetings
        .filter { it.date in monthDates }
        .groupBy { it.date }
        .toSortedMap()

    // Stats
    val meetingCount = if (viewMode == CalendarViewMode.DAY) {
        dayMeetings.size
    } else {
        monthMeetingsByDate.values.sumOf { it.size }
    }

    val totalHours = if (viewMode == CalendarViewMode.DAY) {
        dayMeetings.sumOf { it.endHour - it.startHour }
    } else {
        monthMeetingsByDate.values.flatten().sumOf { it.endHour - it.startHour }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Header
        CalendarHeader(
            viewMode = viewMode,
            onViewModeChange = { viewMode = it },
            selectedDate = selectedDate,
            onDateChange = { selectedDate = it },
            today = today
        )

        // Content
        Box(modifier = Modifier.weight(1f)) {
            when (viewMode) {
                CalendarViewMode.DAY -> DayAgendaView(
                    meetings = dayMeetings,
                    getUserById = { viewModel.getUserById(it) },
                    onMeetingClick = { selectedMeeting = it }
                )
                CalendarViewMode.MONTH -> MonthAgendaView(
                    meetingsByDate = monthMeetingsByDate,
                    today = today,
                    getUserById = { viewModel.getUserById(it) },
                    onMeetingClick = { selectedMeeting = it }
                )
            }
        }

        // Summary
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "$meetingCount meeting${if (meetingCount != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${totalHours.toInt()}h scheduled",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Meeting details dialog
    selectedMeeting?.let { meeting ->
        MeetingDetailsDialog(
            meeting = meeting,
            getUserById = { viewModel.getUserById(it) },
            currentUserId = currentUserId,
            onDismiss = { selectedMeeting = null },
            onCancel = {
                viewModel.cancelMeeting(meeting.id)
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
    selectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    today: LocalDate
) {
    val isToday = selectedDate == today

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Title and view mode switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Calendar",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Your scheduled meetings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // View mode toggle
            SingleChoiceSegmentedButtonRow {
                SegmentedButton(
                    selected = viewMode == CalendarViewMode.DAY,
                    onClick = { onViewModeChange(CalendarViewMode.DAY) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    icon = { Icon(Icons.Default.CalendarViewDay, null, Modifier.size(16.dp)) }
                ) {
                    Text("Day", fontSize = 12.sp)
                }
                SegmentedButton(
                    selected = viewMode == CalendarViewMode.MONTH,
                    onClick = { onViewModeChange(CalendarViewMode.MONTH) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon = { Icon(Icons.Default.ViewAgenda, null, Modifier.size(16.dp)) }
                ) {
                    Text("Month", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        onDateChange(
                            if (viewMode == CalendarViewMode.DAY)
                                selectedDate.minusDays(1)
                            else
                                selectedDate.minusMonths(1)
                        )
                    }
                ) {
                    Icon(Icons.Default.ChevronLeft, "Previous")
                }

                TextButton(
                    onClick = { onDateChange(today) },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (isToday)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            Color.Transparent
                    )
                ) {
                    Text("Today")
                }

                IconButton(
                    onClick = {
                        onDateChange(
                            if (viewMode == CalendarViewMode.DAY)
                                selectedDate.plusDays(1)
                            else
                                selectedDate.plusMonths(1)
                        )
                    }
                ) {
                    Icon(Icons.Default.ChevronRight, "Next")
                }
            }

            // Date label
            Text(
                text = if (viewMode == CalendarViewMode.DAY) {
                    "${selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())}, " +
                    "${selectedDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} " +
                    "${selectedDate.dayOfMonth}, ${selectedDate.year}"
                } else {
                    "${selectedDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${selectedDate.year}"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
        }

        HorizontalDivider(modifier = Modifier.padding(top = 16.dp))
    }
}

@Composable
private fun DayAgendaView(
    meetings: List<Meeting>,
    getUserById: (String) -> User?,
    onMeetingClick: (Meeting) -> Unit
) {
    if (meetings.isEmpty()) {
        EmptyState(message = "No meetings scheduled for this day")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(meetings, key = { it.id }) { meeting ->
                MeetingRow(
                    meeting = meeting,
                    getUserById = getUserById,
                    onClick = { onMeetingClick(meeting) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
private fun MonthAgendaView(
    meetingsByDate: Map<String, List<Meeting>>,
    today: LocalDate,
    getUserById: (String) -> User?,
    onMeetingClick: (Meeting) -> Unit
) {
    if (meetingsByDate.isEmpty()) {
        EmptyState(message = "No meetings scheduled for this month")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            meetingsByDate.forEach { (dateStr, dayMeetings) ->
                val date = dateStr.toLocalDate()
                val isToday = date == today

                // Date header
                item(key = "header_$dateStr") {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isToday) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = "Today",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                                Text(
                                    text = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())} ${date.dayOfMonth}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${dayMeetings.size} meeting${if (dayMeetings.size != 1) "s" else ""}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Meetings for this day
                items(dayMeetings.sortedBy { it.startHour }, key = { it.id }) { meeting ->
                    Surface(
                        color = if (isToday)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                        else
                            Color.Transparent
                    ) {
                        MeetingRow(
                            meeting = meeting,
                            getUserById = getUserById,
                            onClick = { onMeetingClick(meeting) }
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                }
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MeetingRow(
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
    val duration = meeting.endHour - meeting.startHour

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Color bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(72.dp)
                .background(color)
        )

        // Time
        Column(
            modifier = Modifier
                .width(64.dp)
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = formatHour(meeting.startHour),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${duration.toInt()}h",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Meeting info
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp)
        ) {
            Text(
                text = meeting.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                otherUser?.let { user ->
                    UserAvatar(user = user, size = 20)
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Arrow
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.padding(end = 12.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
    val otherUser = if (meeting.organizerId == currentUserId) participant else organizer
    val isOrganizer = meeting.organizerId == currentUserId

    val color = try {
        Color(android.graphics.Color.parseColor(otherUser?.avatarColor ?: "#3B82F6"))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Surface(
                color = color,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = meeting.title,
                    modifier = Modifier.padding(16.dp),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Other user
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    otherUser?.let { user ->
                        UserAvatar(user = user, size = 40)
                        Column {
                            Text(
                                text = if (isOrganizer) "Meeting with" else "Organized by",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = user.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                HorizontalDivider()

                // Date and time
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null)
                        }
                    }
                    Column {
                        Text(
                            text = formatDateFull(meeting.date.toLocalDate()),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${formatTimeRange(meeting.startHour, meeting.endHour)} (${(meeting.endHour - meeting.startHour).toInt()}h)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
