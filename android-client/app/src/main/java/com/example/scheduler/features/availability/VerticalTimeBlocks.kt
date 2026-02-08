package com.example.scheduler.features.availability

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.scheduler.data.models.TimeSlot
import com.example.scheduler.utils.*
import java.time.LocalDate

@Composable
fun VerticalTimeBlocks(
    date: LocalDate,
    slots: List<TimeSlot>,
    use24HourFormat: Boolean = false,
    onSlotsChanged: (List<TimeSlot>) -> Unit
) {
    val dateStr = date.toIsoString()
    // Full 24 hours in 30-min increments
    val timeBlocks = remember { generateHours(0, 24) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            timeBlocks.forEach { hour ->
                val isAvailable = isHourInSlots(dateStr, hour, slots)
                val isHourMark = hour % 1 == 0.0

                TimeBlockRow(
                    hour = hour,
                    isAvailable = isAvailable,
                    isHourMark = isHourMark,
                    use24HourFormat = use24HourFormat,
                    onToggle = {
                        val newSlots = if (isAvailable) {
                            removeTimeBlock(slots, dateStr, hour)
                        } else {
                            addTimeBlock(slots, dateStr, hour)
                        }
                        onSlotsChanged(mergeTimeSlots(newSlots))
                    }
                )

                if (hour < 23.5) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }

    // Legend at bottom
    TimeBlocksLegend()
}

@Composable
fun TimeBlockRow(
    hour: Double,
    isAvailable: Boolean,
    isHourMark: Boolean,
    use24HourFormat: Boolean = false,
    onToggle: () -> Unit
) {
    val backgroundColor = if (isAvailable) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }

    val contentColor = if (isAvailable) {
        MaterialTheme.colorScheme.onPrimary
    } else if (isHourMark) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatHour(hour, use24HourFormat),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isHourMark) FontWeight.Medium else FontWeight.Normal,
            color = contentColor,
            modifier = Modifier.width(72.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(24.dp)
                .border(
                    width = 2.dp,
                    color = if (isAvailable) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                )
                .background(
                    if (isAvailable) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                    else Color.Transparent,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isAvailable) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun TimeBlocksLegend() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(4.dp)
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Available", style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.width(24.dp))

        Box(
            modifier = Modifier
                .size(16.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(4.dp)
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Unavailable", style = MaterialTheme.typography.bodySmall)
    }
}
