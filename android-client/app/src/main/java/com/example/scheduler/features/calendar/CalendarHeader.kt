package com.example.scheduler.features.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarHeader(
    viewMode: CalendarViewMode,
    onViewModeChange: (CalendarViewMode) -> Unit,
    selectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    today: LocalDate
) {
    val isToday = selectedDate == today

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Title and view mode switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // View mode toggle
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = viewMode == CalendarViewMode.DAY,
                    onClick = { onViewModeChange(CalendarViewMode.DAY) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    icon = { Icon(Icons.Default.CalendarViewDay, null, Modifier.size(16.dp)) }
                ) {
                    Text("Day", fontSize = 12.sp)
                }
                SegmentedButton(
                    selected = viewMode == CalendarViewMode.MONTH,
                    onClick = { onViewModeChange(CalendarViewMode.MONTH) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon = { Icon(Icons.Default.ViewAgenda, null, Modifier.size(16.dp)) }
                ) {
                    Text("Month", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        onDateChange(
                            if (viewMode == CalendarViewMode.DAY)
                                selectedDate.minusDays(1)
                            else
                                selectedDate.minusMonths(1)
                        )
                    }
                ) {
                    Icon(Icons.Default.ChevronLeft, "Previous")
                }

                TextButton(
                    onClick = { onDateChange(today) },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (isToday)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            Color.Transparent
                    )
                ) {
                    Text("Today")
                }

                IconButton(
                    onClick = {
                        onDateChange(
                            if (viewMode == CalendarViewMode.DAY)
                                selectedDate.plusDays(1)
                            else
                                selectedDate.plusMonths(1)
                        )
                    }
                ) {
                    Icon(Icons.Default.ChevronRight, "Next")
                }
            }

            // Date label
            Text(
                text = if (viewMode == CalendarViewMode.DAY) {
                    "${selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())}, " +
                    "${selectedDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} " +
                    "${selectedDate.dayOfMonth}, ${selectedDate.year}"
                } else {
                    "${selectedDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${selectedDate.year}"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
        }

        HorizontalDivider(modifier = Modifier.padding(top = 16.dp))
    }
}
