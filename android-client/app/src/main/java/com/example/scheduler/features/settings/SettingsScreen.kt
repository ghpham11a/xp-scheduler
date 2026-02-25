package com.example.scheduler.features.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.scheduler.features.settings.components.DisplaySettingsItem
import com.example.scheduler.features.settings.components.ProfileListItem
import com.example.scheduler.features.settings.components.SectionHeader
import com.example.scheduler.features.settings.components.VersionListItem

@Composable
fun SettingsScreen(
    currentUserId: String,
    onUserChanged: (String) -> Unit,
    onUse24HourFormatChanged: (Boolean) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val selectedUserId by viewModel.currentUserId.collectAsState()
    val use24HourFormat by viewModel.use24HourFormat.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        // Profile Section
        item {
            SectionHeader(title = "Profile")
        }

        items(state.users) { user ->
            ProfileListItem(
                user = user,
                isSelected = user.id == selectedUserId,
                onSelect = {
                    viewModel.setCurrentUser(user.id)
                    onUserChanged(user.id)
                }
            )
        }

        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }

        // Display Section
        item {
            SectionHeader(title = "Display")
        }

        item {
            DisplaySettingsItem(
                use24HourFormat = use24HourFormat,
                onUse24HourFormatChange = {
                    viewModel.setUse24HourFormat(it)
                    onUse24HourFormatChanged(it)
                }
            )
        }

        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }

        // About Section
        item {
            SectionHeader(title = "About")
        }

        item {
            VersionListItem()
        }
    }
}
