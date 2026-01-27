package com.example.scheduler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scheduler.ui.navigation.MainScreen
import com.example.scheduler.ui.theme.SchedulerTheme
import com.example.scheduler.viewmodel.SchedulerViewModel
import com.example.scheduler.viewmodel.SchedulerViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SchedulerTheme {
                val viewModel: SchedulerViewModel = viewModel(
                    factory = SchedulerViewModelFactory(applicationContext)
                )
                val state by viewModel.state.collectAsState()

                MainScreen(
                    state = state,
                    onUserSelected = { userId ->
                        viewModel.setCurrentUser(userId)
                    },
                    onUpdateAvailability = { userId, slots ->
                        viewModel.setAvailability(userId, slots)
                    },
                    onScheduleMeeting = { organizerId, participantId, date, startHour, endHour, title ->
                        viewModel.addMeeting(
                            organizerId = organizerId,
                            participantId = participantId,
                            date = date,
                            startHour = startHour,
                            endHour = endHour,
                            title = title
                        )
                    },
                    onCancelMeeting = { meetingId ->
                        viewModel.cancelMeeting(meetingId)
                    },
                    getUserById = { userId ->
                        viewModel.getUserById(userId)
                    }
                )
            }
        }
    }
}
