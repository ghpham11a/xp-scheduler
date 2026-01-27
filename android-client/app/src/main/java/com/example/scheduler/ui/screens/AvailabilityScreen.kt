package com.example.scheduler.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scheduler.data.TimeSlot
import com.example.scheduler.data.User
import com.example.scheduler.ui.components.UserAvatar
import com.example.scheduler.utils.*
import java.time.LocalDate

@Composable
fun AvailabilityScreen(
    currentUser: User?,
    availabilitySlots: List<TimeSlot>,
    onUpdateAvailability: (List<TimeSlot>) -> Unit,
    modifier: Modifier = Modifier
) {
    val next14Days = remember { getNextDays(14) }
    var localSlots by remember(availabilitySlots) { mutableStateOf(availabilitySlots) }

    val totalHours = getTotalAvailableHours(localSlots)
    val daysWithAvailability = getDaysWithAvailability(localSlots)

    Column(modifier = modifier.fillMaxSize()) {
        // Header with user info
        AvailabilityHeader(
            currentUser = currentUser,
            totalHours = totalHours,
            daysWithAvailability = daysWithAvailability
        )

        // Legend
        AvailabilityLegend()

        // Calendar picker
        LazyRow(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(next14Days) { date ->
                DayColumn(
                    date = date,
                    slots = localSlots.filter { it.date == date.toIsoString() },
                    onSlotsChanged = { newSlots ->
                        localSlots = localSlots.filter { it.date != date.toIsoString() } + newSlots
                        onUpdateAvailability(mergeTimeSlots(localSlots))
                    }
                )
            }
        }
    }
}

@Composable
private fun AvailabilityHeader(
    currentUser: User?,
    totalHours: Double,
    daysWithAvailability: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            currentUser?.let {
                UserAvatar(user = it, size = 48)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentUser?.name ?: "Unknown User",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Set your availability for the next 2 weeks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${totalHours.toInt()}h",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "$daysWithAvailability days",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AvailabilityLegend() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
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

@Composable
private fun DayColumn(
    date: LocalDate,
    slots: List<TimeSlot>,
    onSlotsChanged: (List<TimeSlot>) -> Unit
) {
    val today = LocalDate.now()
    val isToday = date == today
    val dateStr = date.toIsoString()

    // Generate all half-hour blocks from 6am to 10pm
    val timeBlocks = remember { generateHours(6, 22) }

    // Calculate hours available for this day
    val dayHours = slots.sumOf { it.endHour - it.startHour }

    Column(
        modifier = Modifier
            .width(80.dp)
            .border(
                width = if (isToday) 2.dp else 1.dp,
                color = if (isToday) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
    ) {
        // Day header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isToday) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = SHORT_DAYS[date.dayOfWeek.value % 7],
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (dayHours > 0) {
                Text(
                    text = "${dayHours.toInt()}h",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Quick action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PresetButton("9-5") {
                onSlotsChanged(listOf(TimeSlot(dateStr, 9.0, 17.0)))
            }
            PresetButton("AM") {
                onSlotsChanged(listOf(TimeSlot(dateStr, 6.0, 12.0)))
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PresetButton("PM") {
                onSlotsChanged(listOf(TimeSlot(dateStr, 12.0, 18.0)))
            }
            PresetButton("Eve") {
                onSlotsChanged(listOf(TimeSlot(dateStr, 18.0, 22.0)))
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = { onSlotsChanged(emptyList()) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Clear,
                    contentDescription = "Clear",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        HorizontalDivider()

        // Time blocks
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(4.dp)
        ) {
            timeBlocks.forEach { hour ->
                val isAvailable = isHourInSlots(dateStr, hour, slots)

                TimeBlock(
                    hour = hour,
                    isAvailable = isAvailable,
                    onToggle = {
                        val newSlots = if (isAvailable) {
                            // Remove this half-hour block
                            removeTimeBlock(slots, dateStr, hour)
                        } else {
                            // Add this half-hour block
                            addTimeBlock(slots, dateStr, hour)
                        }
                        onSlotsChanged(mergeTimeSlots(newSlots))
                    }
                )
            }
        }
    }
}

@Composable
private fun PresetButton(
    label: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.height(28.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun TimeBlock(
    hour: Double,
    isAvailable: Boolean,
    onToggle: () -> Unit
) {
    val isHalfHour = hour % 1 != 0.0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isHalfHour) 20.dp else 24.dp)
            .padding(vertical = 1.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(
                if (isAvailable) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onToggle),
        contentAlignment = Alignment.Center
    ) {
        if (!isHalfHour) {
            Text(
                text = formatHour(hour).replace(" ", "\n"),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 8.sp,
                color = if (isAvailable) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 8.sp
            )
        }
    }
}

// Helper function to add a 30-min time block
private fun addTimeBlock(
    currentSlots: List<TimeSlot>,
    date: String,
    hour: Double
): List<TimeSlot> {
    return currentSlots + TimeSlot(date, hour, hour + 0.5)
}

// Helper function to remove a 30-min time block
private fun removeTimeBlock(
    currentSlots: List<TimeSlot>,
    date: String,
    hour: Double
): List<TimeSlot> {
    val result = mutableListOf<TimeSlot>()

    for (slot in currentSlots) {
        if (slot.date != date) {
            result.add(slot)
            continue
        }

        // Check if this hour is within this slot
        if (hour >= slot.startHour && hour < slot.endHour) {
            // Split the slot if needed
            if (hour > slot.startHour) {
                result.add(TimeSlot(date, slot.startHour, hour))
            }
            if (hour + 0.5 < slot.endHour) {
                result.add(TimeSlot(date, hour + 0.5, slot.endHour))
            }
        } else {
            result.add(slot)
        }
    }

    return result
}
