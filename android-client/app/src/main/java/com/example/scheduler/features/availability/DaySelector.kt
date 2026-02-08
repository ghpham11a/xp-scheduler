package com.example.scheduler.features.availability

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.scheduler.data.models.TimeSlot
import com.example.scheduler.utils.SHORT_DAYS
import com.example.scheduler.utils.getTotalAvailableHours
import com.example.scheduler.utils.toIsoString
import java.time.LocalDate

@Composable
fun DaySelectorRow(
    days: List<LocalDate>,
    selectedIndex: Int,
    slots: List<TimeSlot>,
    listState: LazyListState,
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
fun DayPill(
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
