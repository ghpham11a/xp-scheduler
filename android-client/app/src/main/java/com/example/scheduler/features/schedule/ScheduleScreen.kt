package com.example.scheduler.features.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.scheduler.data.models.User
import com.example.scheduler.utils.MeetingDuration

@Composable
fun ScheduleScreen(
    currentUserId: String,
    use24HourFormat: Boolean = false,
    viewModel: ScheduleViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    // Update ViewModel with current user ID when it changes
    LaunchedEffect(currentUserId) {
        viewModel.setCurrentUserId(currentUserId)
    }

    var currentStep by remember { mutableStateOf(ScheduleStep.SELECT_PARTICIPANT) }
    var selectedParticipant by remember { mutableStateOf<User?>(null) }
    var selectedDuration by remember { mutableStateOf<MeetingDuration?>(null) }
    var selectedSlot by remember { mutableStateOf<Pair<String, Double>?>(null) } // date, startHour
    var meetingTitle by remember { mutableStateOf("") }

    val otherUsers = state.users.filter { it.id != currentUserId }
    val currentUserMeetings = state.meetings.filter {
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
        when (currentStep) {
            ScheduleStep.SELECT_PARTICIPANT -> ParticipantSelection(
                otherUsers = otherUsers,
                availabilities = state.availabilities,
                onSelect = { user ->
                    selectedParticipant = user
                    currentStep = ScheduleStep.SELECT_TIME
                }
            )

            ScheduleStep.SELECT_TIME -> TimeSlotSelection(
                currentUserId = currentUserId,
                participant = selectedParticipant!!,
                availabilities = state.availabilities,
                meetings = state.meetings,
                use24HourFormat = use24HourFormat,
                onSelect = { date, startHour, duration ->
                    selectedSlot = date to startHour
                    selectedDuration = duration
                    currentStep = ScheduleStep.CONFIRM
                },
                onBack = { currentStep = ScheduleStep.SELECT_PARTICIPANT }
            )

            ScheduleStep.CONFIRM -> ConfirmationScreen(
                participant = selectedParticipant!!,
                duration = selectedDuration!!,
                selectedDate = selectedSlot!!.first,
                selectedStartHour = selectedSlot!!.second,
                meetingTitle = meetingTitle,
                use24HourFormat = use24HourFormat,
                onTitleChange = { meetingTitle = it },
                onConfirm = {
                    viewModel.addMeeting(
                        organizerId = currentUserId,
                        participantId = selectedParticipant!!.id,
                        date = selectedSlot!!.first,
                        startHour = selectedSlot!!.second,
                        endHour = selectedSlot!!.second + selectedDuration!!.hours,
                        title = meetingTitle
                    )
                    resetWizard()
                },
                onBack = { currentStep = ScheduleStep.SELECT_TIME }
            )
        }

        // Meeting list (always visible at bottom)
        if (currentStep == ScheduleStep.SELECT_PARTICIPANT && currentUserMeetings.isNotEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            MeetingListSection(
                meetings = currentUserMeetings,
                currentUserId = currentUserId,
                getUserById = { viewModel.getUserById(it) },
                use24HourFormat = use24HourFormat,
                onCancelMeeting = { viewModel.cancelMeeting(it) }
            )
        }
    }
}
