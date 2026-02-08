package com.example.scheduler.features.availability

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.scheduler.utils.AvailabilityPreset

@Composable
fun QuickActionButtons(
    onPresetSelected: (AvailabilityPreset) -> Unit,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // All Day button highlighted
        AssistChip(
            onClick = { onPresetSelected(AvailabilityPreset.FULL_DAY) },
            label = { Text("All Day") },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )

        AssistChip(
            onClick = { onPresetSelected(AvailabilityPreset.BUSINESS) },
            label = { Text("9-5") }
        )

        AssistChip(
            onClick = { onPresetSelected(AvailabilityPreset.MORNING) },
            label = { Text("Morning") }
        )

        AssistChip(
            onClick = { onPresetSelected(AvailabilityPreset.AFTERNOON) },
            label = { Text("Afternoon") }
        )

        AssistChip(
            onClick = { onPresetSelected(AvailabilityPreset.EVENING) },
            label = { Text("Evening") }
        )

        AssistChip(
            onClick = onClear,
            label = { Text("Clear") },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                labelColor = MaterialTheme.colorScheme.onErrorContainer
            )
        )
    }
}
