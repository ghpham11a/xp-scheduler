package com.example.scheduler.features.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.scheduler.data.models.Meeting
import com.example.scheduler.data.models.User
import com.example.scheduler.utils.toLocalDate
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

@Composable
fun MonthAgendaView(
    meetingsByDate: Map<String, List<Meeting>>,
    today: LocalDate,
    getUserById: (String) -> User?,
    use24HourFormat: Boolean = false,
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
                            use24HourFormat = use24HourFormat,
                            onClick = { onMeetingClick(meeting) }
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                }
            }
        }
    }
}
