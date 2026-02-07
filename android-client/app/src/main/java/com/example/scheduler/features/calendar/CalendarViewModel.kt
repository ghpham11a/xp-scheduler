package com.example.scheduler.features.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class CalendarState(
    val meetings: List<Meeting> = emptyList(),
    val users: List<User> = emptyList(),
    val currentUserId: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: SchedulerRepository
) : ViewModel() {
    private val _state = MutableStateFlow(CalendarState())
    val state: StateFlow<CalendarState> = _state.asStateFlow()

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
                            meetings = data.meetings,
                            users = data.users,
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
