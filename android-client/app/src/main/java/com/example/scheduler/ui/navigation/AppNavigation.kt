package com.example.scheduler.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.scheduler.ui.components.Header
import com.example.scheduler.ui.screens.AvailabilityScreen
import com.example.scheduler.ui.screens.CalendarScreen
import com.example.scheduler.ui.screens.ScheduleScreen
import com.example.scheduler.ui.screens.SettingsScreen
import com.example.scheduler.viewmodel.SchedulerState

enum class AppScreen(
    val title: String,
    val icon: ImageVector
) {
    CALENDAR("Calendar", Icons.Default.CalendarMonth),
    AVAILABILITY("Availability", Icons.Default.EventAvailable),
    SCHEDULE("Schedule", Icons.Default.Add),
    SETTINGS("Settings", Icons.Default.Settings)
}

@Composable
fun MainScreen(
    state: SchedulerState,
    onUserSelected: (String) -> Unit,
    onUpdateAvailability: (String, List<com.example.scheduler.data.TimeSlot>) -> Unit,
    onScheduleMeeting: (
        organizerId: String,
        participantId: String,
        date: String,
        startHour: Double,
        endHour: Double,
        title: String
    ) -> Unit,
    onCancelMeeting: (String) -> Unit,
    getUserById: (String) -> com.example.scheduler.data.User?
) {
    var currentScreen by remember { mutableStateOf(AppScreen.CALENDAR) }
    var showAllHours by remember { mutableStateOf(false) }

    val currentUser = state.users.find { it.id == state.currentUserId }
    val currentUserAvailability = state.availabilities.find { it.userId == state.currentUserId }

    Scaffold(
        topBar = {
            Header(currentUser = currentUser)
        },
        bottomBar = {
            NavigationBar {
                AppScreen.entries.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentScreen == screen,
                        onClick = { currentScreen = screen }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    LoadingScreen()
                }
                state.error != null -> {
                    ErrorScreen(
                        error = state.error,
                        onRetry = { /* Will be handled by ViewModel */ }
                    )
                }
                else -> {
                    when (currentScreen) {
                        AppScreen.CALENDAR -> CalendarScreen(
                            currentUserId = state.currentUserId,
                            users = state.users,
                            meetings = state.meetings,
                            showAllHours = showAllHours,
                            onCancelMeeting = onCancelMeeting,
                            getUserById = getUserById
                        )
                        AppScreen.AVAILABILITY -> AvailabilityScreen(
                            currentUser = currentUser,
                            availabilitySlots = currentUserAvailability?.slots ?: emptyList(),
                            onUpdateAvailability = { slots ->
                                onUpdateAvailability(state.currentUserId, slots)
                            }
                        )
                        AppScreen.SCHEDULE -> ScheduleScreen(
                            currentUserId = state.currentUserId,
                            users = state.users,
                            availabilities = state.availabilities,
                            meetings = state.meetings,
                            onScheduleMeeting = onScheduleMeeting,
                            onCancelMeeting = onCancelMeeting,
                            getUserById = getUserById
                        )
                        AppScreen.SETTINGS -> SettingsScreen(
                            currentUser = currentUser,
                            users = state.users,
                            showAllHours = showAllHours,
                            onUserSelected = onUserSelected,
                            onShowAllHoursChanged = { showAllHours = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text("Loading...")
        }
    }
}

@Composable
private fun ErrorScreen(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}
