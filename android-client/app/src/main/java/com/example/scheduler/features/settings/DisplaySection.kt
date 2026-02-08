package com.example.scheduler.features.settings

import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun DisplaySettingsItem(
    showAllHours: Boolean,
    onShowAllHoursChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text("Show 24-hour time range") },
        supportingContent = { Text("Display full day instead of 6 AM - 10 PM") },
        trailingContent = {
            Switch(
                checked = showAllHours,
                onCheckedChange = onShowAllHoursChange
            )
        }
    )
}
