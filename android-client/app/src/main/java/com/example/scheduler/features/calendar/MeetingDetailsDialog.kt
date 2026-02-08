package com.example.scheduler.features.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.scheduler.data.models.Meeting
import com.example.scheduler.data.models.User
import com.example.scheduler.shared.components.UserAvatar
import com.example.scheduler.utils.formatDateFull
import com.example.scheduler.utils.formatTimeRange
import com.example.scheduler.utils.toLocalDate

@Composable
fun MeetingDetailsDialog(
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
