package com.example.scheduler.features.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.scheduler.data.models.Meeting
import com.example.scheduler.utils.toIsoString
import java.time.LocalDate
import java.time.YearMonth

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
