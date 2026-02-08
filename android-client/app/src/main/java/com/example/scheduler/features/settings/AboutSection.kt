package com.example.scheduler.features.settings

import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun VersionListItem(version: String = "1.0.0") {
    ListItem(
        headlineContent = { Text("Version") },
        trailingContent = {
            Text(
                text = version,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}
