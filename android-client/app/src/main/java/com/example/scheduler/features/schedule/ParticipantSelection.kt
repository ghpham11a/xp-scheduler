package com.example.scheduler.features.schedule

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.scheduler.data.models.Availability
import com.example.scheduler.data.models.User
import com.example.scheduler.shared.components.UserAvatar

@Composable
fun ParticipantSelection(
    otherUsers: List<User>,
    availabilities: List<Availability>,
    onSelect: (User) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Schedule a Meeting",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Select who you want to meet with",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(otherUsers) { user ->
                val userAvailability = availabilities.find { it.userId == user.id }
                val hasAvailability = (userAvailability?.slots?.isNotEmpty()) == true

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = hasAvailability) { onSelect(user) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (hasAvailability)
                            MaterialTheme.colorScheme.surface
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        UserAvatar(user = user, size = 48)

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (hasAvailability) {
                                    val hours = userAvailability?.slots?.sumOf { it.endHour - it.startHour } ?: 0.0
                                    "${hours.toInt()}h available"
                                } else "No availability set",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (hasAvailability)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error
                            )
                        }

                        if (hasAvailability) {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
