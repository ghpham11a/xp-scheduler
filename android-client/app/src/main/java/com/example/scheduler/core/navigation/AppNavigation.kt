package com.example.scheduler.core.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.scheduler.data.model.User
import com.example.scheduler.shared.components.Header
import com.example.scheduler.features.availability.AvailabilityScreen
import com.example.scheduler.features.calendar.CalendarScreen
import com.example.scheduler.features.schedule.ScheduleScreen
import com.example.scheduler.features.settings.SettingsScreen

@Composable
fun MainScreen(
    users: List<User>,
    currentUserId: String,
    use24HourFormat: Boolean,
    isLoading: Boolean,
    error: String?,
    onUserSelected: (String) -> Unit,
    onUse24HourFormatChanged: (Boolean) -> Unit,
    onRetry: () -> Unit
) {
    var currentScreen by remember { mutableStateOf(AppScreen.CALENDAR) }

    val currentUser = users.find { it.id == currentUserId }

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
                isLoading -> {
                    LoadingScreen()
                }
                error != null -> {
                    ErrorScreen(
                        error = error,
                        onRetry = onRetry
                    )
                }
                else -> {
                    when (currentScreen) {
                        AppScreen.CALENDAR -> CalendarScreen(
                            currentUserId = currentUserId,
                            use24HourFormat = use24HourFormat
                        )
                        AppScreen.AVAILABILITY -> AvailabilityScreen(
                            currentUserId = currentUserId,
                            currentUser = currentUser,
                            use24HourFormat = use24HourFormat
                        )
                        AppScreen.SCHEDULE -> ScheduleScreen(
                            currentUserId = currentUserId,
                            use24HourFormat = use24HourFormat
                        )
                        AppScreen.SETTINGS -> SettingsScreen(
                            currentUserId = currentUserId,
                            onUserChanged = onUserSelected,
                            onUse24HourFormatChanged = onUse24HourFormatChanged
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
