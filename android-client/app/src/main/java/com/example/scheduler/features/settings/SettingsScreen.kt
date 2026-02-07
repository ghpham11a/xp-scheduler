package com.example.scheduler.features.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.scheduler.data.models.User
import com.example.scheduler.shared.components.UserAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentUserId: String,
    onUserChanged: (String) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    // Sync with parent when settings change
    LaunchedEffect(state.currentUserId) {
        if (state.currentUserId.isNotEmpty() && state.currentUserId != currentUserId) {
            onUserChanged(state.currentUserId)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        // Profile Section
        item {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        items(state.users) { user ->
            ListItem(
                headlineContent = { Text(user.name) },
                supportingContent = { Text(user.email) },
                leadingContent = {
                    UserAvatar(user = user, size = 40)
                },
                trailingContent = {
                    if (user.id == state.currentUserId) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                modifier = Modifier.clickable {
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
            Text(
                text = "Display",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        item {
            ListItem(
                headlineContent = { Text("Show 24-hour time range") },
                supportingContent = { Text("Display full day instead of 6 AM - 10 PM") },
                trailingContent = {
                    Switch(
                        checked = state.showAllHours,
                        onCheckedChange = { viewModel.setShowAllHours(it) }
                    )
                }
            )
        }

        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }

        // About Section
        item {
            Text(
                text = "About",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        item {
            ListItem(
                headlineContent = { Text("Version") },
                trailingContent = {
                    Text(
                        text = "1.0.0",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
    }
}
