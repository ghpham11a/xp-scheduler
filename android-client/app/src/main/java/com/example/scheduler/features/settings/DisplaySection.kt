package com.example.scheduler.features.settings

import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun DisplaySettingsItem(
    use24HourFormat: Boolean,
    onUse24HourFormatChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text("Use 24-hour format") },
        supportingContent = { Text("Display times as 14:00 instead of 2:00 PM") },
        trailingContent = {
            Switch(
                checked = use24HourFormat,
                onCheckedChange = onUse24HourFormatChange
            )
        }
    )
}
