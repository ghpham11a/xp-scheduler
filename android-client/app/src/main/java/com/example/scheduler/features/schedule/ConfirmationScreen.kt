package com.example.scheduler.features.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.scheduler.data.models.User
import com.example.scheduler.shared.components.UserAvatar
import com.example.scheduler.utils.*

@Composable
fun ConfirmationScreen(
    participant: User,
    duration: MeetingDuration,
    selectedDate: String,
    selectedStartHour: Double,
    meetingTitle: String,
    use24HourFormat: Boolean = false,
    onTitleChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit
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
                            "${formatTimeRange(selectedStartHour, endHour, use24HourFormat)} (${duration.displayName})",
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                onClick = onBack
            ) {
                Text("Back")
            }

            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                enabled = meetingTitle.isNotBlank()
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Schedule Meeting")
            }
        }
    }
}
