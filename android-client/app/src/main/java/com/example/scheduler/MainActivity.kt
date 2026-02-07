package com.example.scheduler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.scheduler.core.navigation.MainScreen
import com.example.scheduler.core.designsystem.theme.SchedulerTheme
import com.example.scheduler.viewmodel.SchedulerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SchedulerTheme {
                val viewModel: SchedulerViewModel = hiltViewModel()
                val state by viewModel.state.collectAsState()

                MainScreen(
                    state = state,
                    onUserSelected = { userId ->
                        viewModel.setCurrentUser(userId)
                    }
                )
            }
        }
    }
}
