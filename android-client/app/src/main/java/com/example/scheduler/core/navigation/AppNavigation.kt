package com.example.scheduler.core.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.scheduler.data.models.User
import com.example.scheduler.shared.components.Header
import com.example.scheduler.features.availability.AvailabilityScreen
import com.example.scheduler.features.calendar.CalendarScreen
import com.example.scheduler.features.schedule.ScheduleScreen
import com.example.scheduler.features.settings.SettingsScreen
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
    onUserSelected: (String) -> Unit
) {
    var currentScreen by remember { mutableStateOf(AppScreen.CALENDAR) }

    val currentUser = state.users.find { it.id == state.currentUserId }

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
                            currentUserId = state.currentUserId
                        )
                        AppScreen.AVAILABILITY -> AvailabilityScreen(
                            currentUserId = state.currentUserId,
                            currentUser = currentUser
                        )
                        AppScreen.SCHEDULE -> ScheduleScreen(
                            currentUserId = state.currentUserId
                        )
                        AppScreen.SETTINGS -> SettingsScreen(
                            currentUserId = state.currentUserId,
                            onUserChanged = onUserSelected
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
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
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
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
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
