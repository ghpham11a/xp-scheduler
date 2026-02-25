package com.example.scheduler.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppScreen(
    val title: String,
    val icon: ImageVector
) {
    CALENDAR("Calendar", Icons.Default.CalendarMonth),
    AVAILABILITY("Availability", Icons.Default.EventAvailable),
    SCHEDULE("Schedule", Icons.Default.Add),
    SETTINGS("Settings", Icons.Default.Settings)
}
