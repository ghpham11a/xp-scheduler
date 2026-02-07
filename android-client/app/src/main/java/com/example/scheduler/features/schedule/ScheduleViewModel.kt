package com.example.scheduler.features.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scheduler.data.models.Availability
import com.example.scheduler.data.models.Meeting
import com.example.scheduler.data.models.User
import com.example.scheduler.data.repositories.SchedulerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScheduleState(
    val users: List<User> = emptyList(),
    val availabilities: List<Availability> = emptyList(),
    val meetings: List<Meeting> = emptyList(),
    val currentUserId: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val repository: SchedulerRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ScheduleState())
    val state: StateFlow<ScheduleState> = _state.asStateFlow()

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.fetchAllData()
                .onSuccess { data ->
                    _state.update {
                        it.copy(
                            users = data.users,
                            availabilities = data.availabilities,
                            meetings = data.meetings,
                            isLoading = false
                        )
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(isLoading = false, error = e.message ?: "Unknown error")
                    }
                }
        }
    }

    fun setCurrentUserId(userId: String) {
        _state.update { it.copy(currentUserId = userId) }
    }

    fun addMeeting(
        organizerId: String,
        participantId: String,
        date: String,
        startHour: Double,
        endHour: Double,
        title: String
    ) {
        viewModelScope.launch {
            repository.createMeeting(
                organizerId = organizerId,
                participantId = participantId,
                date = date,
                startHour = startHour,
                endHour = endHour,
                title = title
            )
                .onSuccess { meeting ->
                    _state.update { current ->
                        current.copy(meetings = current.meetings + meeting)
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(error = e.message) }
                }
        }
    }

    fun cancelMeeting(meetingId: String) {
        viewModelScope.launch {
            // Optimistic update
            _state.update { current ->
                current.copy(meetings = current.meetings.filter { it.id != meetingId })
            }

            repository.deleteMeeting(meetingId)
                .onFailure {
                    // On error, refetch data
                    fetchData()
                }
        }
    }

    fun getUserById(userId: String): User? {
        return _state.value.users.find { it.id == userId }
    }

    fun getCurrentUserMeetings(): List<Meeting> {
        val userId = _state.value.currentUserId
        return _state.value.meetings.filter {
            it.organizerId == userId || it.participantId == userId
        }
    }
}
