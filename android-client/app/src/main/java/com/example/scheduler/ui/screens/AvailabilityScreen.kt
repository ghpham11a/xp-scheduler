package com.example.scheduler.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.scheduler.data.TimeSlot
import com.example.scheduler.data.User
import com.example.scheduler.ui.components.UserAvatar
import com.example.scheduler.utils.*
import kotlinx.coroutines.launch
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
    var selectedDayIndex by remember { mutableStateOf(0) }

    val totalHours = getTotalAvailableHours(localSlots)
    val daysWithAvailability = getDaysWithAvailability(localSlots)

    val currentDay = next14Days[selectedDayIndex]
    val currentDaySlots = localSlots.filter { it.date == currentDay.toIsoString() }
    val currentDayHours = getTotalAvailableHours(currentDaySlots)

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize()) {
        // Header with user info
        AvailabilityHeader(
            currentUser = currentUser,
            totalHours = totalHours,
            daysWithAvailability = daysWithAvailability
        )

        // Day selector pills - horizontal scroll
        DaySelectorRow(
            days = next14Days,
            selectedIndex = selectedDayIndex,
            slots = localSlots,
            listState = listState,
            onDaySelected = { index ->
                selectedDayIndex = index
                coroutineScope.launch {
                    listState.animateScrollToItem(maxOf(0, index - 2))
                }
            }
        )

        // Current day header with navigation
        DayNavigationHeader(
            currentDay = currentDay,
            currentDayHours = currentDayHours,
            canGoPrev = selectedDayIndex > 0,
            canGoNext = selectedDayIndex < next14Days.size - 1,
            onPrevDay = {
                if (selectedDayIndex > 0) {
                    selectedDayIndex--
                    coroutineScope.launch {
                        listState.animateScrollToItem(maxOf(0, selectedDayIndex - 2))
                    }
                }
            },
            onNextDay = {
                if (selectedDayIndex < next14Days.size - 1) {
                    selectedDayIndex++
                    coroutineScope.launch {
                        listState.animateScrollToItem(maxOf(0, selectedDayIndex - 2))
                    }
                }
            }
        )

        // Quick action presets
        QuickActionButtons(
            onPresetSelected = { preset ->
                val dateStr = currentDay.toIsoString()
                localSlots = localSlots.filter { it.date != dateStr } +
                        TimeSlot(dateStr, preset.startHour, preset.endHour)
                onUpdateAvailability(mergeTimeSlots(localSlots))
            },
            onClear = {
                val dateStr = currentDay.toIsoString()
                localSlots = localSlots.filter { it.date != dateStr }
                onUpdateAvailability(mergeTimeSlots(localSlots))
            }
        )

        // Vertical time blocks
        VerticalTimeBlocks(
            date = currentDay,
            slots = currentDaySlots,
            onSlotsChanged = { newSlots ->
                val dateStr = currentDay.toIsoString()
                localSlots = localSlots.filter { it.date != dateStr } + newSlots
                onUpdateAvailability(mergeTimeSlots(localSlots))
            }
        )
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
private fun DaySelectorRow(
    days: List<LocalDate>,
    selectedIndex: Int,
    slots: List<TimeSlot>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onDaySelected: (Int) -> Unit
) {
    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(days) { index, date ->
            val isSelected = index == selectedIndex
            val isToday = date == LocalDate.now()
            val dayHours = getTotalAvailableHours(slots.filter { it.date == date.toIsoString() })

            DayPill(
                date = date,
                isSelected = isSelected,
                isToday = isToday,
                hasAvailability = dayHours > 0,
                onClick = { onDaySelected(index) }
            )
        }
    }
}

@Composable
private fun DayPill(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    hasAvailability: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = SHORT_DAYS[date.dayOfWeek.value % 7],
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = contentColor
        )
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
        if (hasAvailability) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(6.dp)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.primary,
                        CircleShape
                    )
            )
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DayNavigationHeader(
    currentDay: LocalDate,
    currentDayHours: Double,
    canGoPrev: Boolean,
    canGoNext: Boolean,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onPrevDay,
            enabled = canGoPrev
        ) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous day",
                modifier = Modifier.size(28.dp)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = DAYS[currentDay.dayOfWeek.value % 7],
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${formatDateRelative(currentDay)} • ${
                    if (currentDayHours > 0) "${currentDayHours.toInt()}h selected"
                    else "No availability"
                }",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(
            onClick = onNextDay,
            enabled = canGoNext
        ) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next day",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun QuickActionButtons(
    onPresetSelected: (AvailabilityPreset) -> Unit,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // All Day button highlighted
        AssistChip(
            onClick = { onPresetSelected(AvailabilityPreset.FULL_DAY) },
            label = { Text("All Day") },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )

        AssistChip(
            onClick = { onPresetSelected(AvailabilityPreset.BUSINESS) },
            label = { Text("9-5") }
        )

        AssistChip(
            onClick = { onPresetSelected(AvailabilityPreset.MORNING) },
            label = { Text("Morning") }
        )

        AssistChip(
            onClick = { onPresetSelected(AvailabilityPreset.AFTERNOON) },
            label = { Text("Afternoon") }
        )

        AssistChip(
            onClick = { onPresetSelected(AvailabilityPreset.EVENING) },
            label = { Text("Evening") }
        )

        AssistChip(
            onClick = onClear,
            label = { Text("Clear") },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                labelColor = MaterialTheme.colorScheme.onErrorContainer
            )
        )
    }
}

@Composable
private fun VerticalTimeBlocks(
    date: LocalDate,
    slots: List<TimeSlot>,
    onSlotsChanged: (List<TimeSlot>) -> Unit
) {
    val dateStr = date.toIsoString()
    // Full 24 hours in 30-min increments
    val timeBlocks = remember { generateHours(0, 24) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .weight(1f)
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

@Composable
private fun TimeBlockRow(
    hour: Double,
    isAvailable: Boolean,
    isHourMark: Boolean,
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
            text = formatHour(hour),
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
