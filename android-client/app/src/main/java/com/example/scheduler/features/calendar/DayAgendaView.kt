package com.example.scheduler.features.calendar

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.scheduler.data.models.Meeting
import com.example.scheduler.data.models.User

@Composable
fun DayAgendaView(
    meetings: List<Meeting>,
    getUserById: (String) -> User?,
    use24HourFormat: Boolean = false,
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
                    use24HourFormat = use24HourFormat,
                    onClick = { onMeetingClick(meeting) }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    }
}
