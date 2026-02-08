package com.example.scheduler.features.availability

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.scheduler.data.models.TimeSlot
import com.example.scheduler.data.models.User
import com.example.scheduler.utils.*
import kotlinx.coroutines.launch

@Composable
fun AvailabilityScreen(
    currentUserId: String,
    currentUser: User?,
    viewModel: AvailabilityViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    // Update ViewModel with current user info when it changes
    LaunchedEffect(currentUserId, currentUser) {
        viewModel.setCurrentUserId(currentUserId)
        viewModel.setCurrentUser(currentUser)
    }

    val availabilitySlots = viewModel.getCurrentUserAvailability()

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
                viewModel.setAvailability(currentUserId, mergeTimeSlots(localSlots))
            },
            onClear = {
                val dateStr = currentDay.toIsoString()
                localSlots = localSlots.filter { it.date != dateStr }
                viewModel.setAvailability(currentUserId, mergeTimeSlots(localSlots))
            }
        )

        // Vertical time blocks
        VerticalTimeBlocks(
            date = currentDay,
            slots = currentDaySlots,
            onSlotsChanged = { newSlots ->
                val dateStr = currentDay.toIsoString()
                localSlots = localSlots.filter { it.date != dateStr } + newSlots
                viewModel.setAvailability(currentUserId, mergeTimeSlots(localSlots))
            }
        )
    }
}
