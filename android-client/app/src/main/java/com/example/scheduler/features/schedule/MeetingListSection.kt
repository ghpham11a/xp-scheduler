package com.example.scheduler.features.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.scheduler.data.models.Meeting
import com.example.scheduler.data.models.User
import com.example.scheduler.shared.components.UserAvatar
import com.example.scheduler.utils.*

@Composable
fun MeetingListSection(
    meetings: List<Meeting>,
    currentUserId: String,
    getUserById: (String) -> User?,
    use24HourFormat: Boolean = false,
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
                                text = "${formatDateRelative(meeting.date.toLocalDate())} • ${formatTimeRange(meeting.startHour, meeting.endHour, use24HourFormat)}",
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
