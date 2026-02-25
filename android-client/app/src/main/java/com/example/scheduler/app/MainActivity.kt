package com.example.scheduler.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.scheduler.core.navigation.MainScreen
import com.example.scheduler.core.navigation.MainViewModel
import com.example.scheduler.shared.theme.SchedulerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SchedulerTheme {
                val viewModel: MainViewModel = hiltViewModel()
                val state by viewModel.state.collectAsState()
                val currentUserId by viewModel.currentUserId.collectAsState()
                val use24HourFormat by viewModel.use24HourFormat.collectAsState()

                MainScreen(
                    users = state.users,
                    currentUserId = currentUserId,
                    use24HourFormat = use24HourFormat,
                    isLoading = state.isLoading,
                    error = state.error,
                    onUserSelected = viewModel::setCurrentUser,
                    onUse24HourFormatChanged = viewModel::setUse24HourFormat,
                    onRetry = viewModel::retry
                )
            }
        }
    }
}
