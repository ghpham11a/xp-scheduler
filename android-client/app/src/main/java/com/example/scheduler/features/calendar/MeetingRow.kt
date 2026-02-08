package com.example.scheduler.features.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.scheduler.data.models.Meeting
import com.example.scheduler.data.models.User
import com.example.scheduler.shared.components.UserAvatar
import com.example.scheduler.utils.formatHour

@Composable
fun MeetingRow(
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
