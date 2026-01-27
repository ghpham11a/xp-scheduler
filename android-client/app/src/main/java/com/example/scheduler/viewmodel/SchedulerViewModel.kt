package com.example.scheduler.viewmodel

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.scheduler.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// DataStore extension
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "scheduler_prefs")

// State
data class SchedulerState(
    val currentUserId: String = "",
    val users: List<User> = emptyList(),
    val availabilities: List<Availability> = emptyList(),
    val meetings: List<Meeting> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class SchedulerViewModel(private val context: Context) : ViewModel() {
    private val _state = MutableStateFlow(SchedulerState())
    val state: StateFlow<SchedulerState> = _state.asStateFlow()

    private val CURRENT_USER_KEY = stringPreferencesKey("current_user_id")

    init {
        loadPersistedUserId()
        fetchData()
    }

    private fun loadPersistedUserId() {
        viewModelScope.launch {
            context.dataStore.data.first().let { prefs ->
                val userId = prefs[CURRENT_USER_KEY]
                if (userId != null) {
                    _state.update { it.copy(currentUserId = userId) }
                }
            }
        }
    }

    fun fetchData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val users = ApiClient.api.getUsers()
                val availabilities = ApiClient.api.getAvailabilities()
                val meetings = ApiClient.api.getMeetings()

                _state.update { current ->
                    val currentUserId = if (current.currentUserId.isEmpty() && users.isNotEmpty()) {
                        users.first().id
                    } else {
                        current.currentUserId
                    }

                    current.copy(
                        users = users,
                        availabilities = availabilities,
                        meetings = meetings,
                        currentUserId = currentUserId,
                        isLoading = false
                    )
                }

                // Persist the current user ID
                if (_state.value.currentUserId.isNotEmpty()) {
                    persistCurrentUserId(_state.value.currentUserId)
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    fun setCurrentUser(userId: String) {
        _state.update { it.copy(currentUserId = userId) }
        viewModelScope.launch {
            persistCurrentUserId(userId)
        }
    }

    private suspend fun persistCurrentUserId(userId: String) {
        context.dataStore.edit { prefs ->
            prefs[CURRENT_USER_KEY] = userId
        }
    }

    fun setAvailability(userId: String, slots: List<TimeSlot>) {
        viewModelScope.launch {
            // Optimistic update
            val newAvailability = Availability(userId, slots)
            _state.update { current ->
                val updatedAvailabilities = current.availabilities.map {
                    if (it.userId == userId) newAvailability else it
                }.let { list ->
                    if (list.none { it.userId == userId }) {
                        list + newAvailability
                    } else list
                }
                current.copy(availabilities = updatedAvailabilities)
            }

            try {
                ApiClient.api.updateAvailability(userId, slots)
            } catch (e: Exception) {
                // On error, refetch data
                fetchData()
            }
        }
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
            try {
                val request = CreateMeetingRequest(
                    organizerId = organizerId,
                    participantId = participantId,
                    date = date,
                    startHour = startHour,
                    endHour = endHour,
                    title = title
                )
                val meeting = ApiClient.api.createMeeting(request)

                _state.update { current ->
                    current.copy(meetings = current.meetings + meeting)
                }
            } catch (e: Exception) {
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

            try {
                ApiClient.api.deleteMeeting(meetingId)
            } catch (e: Exception) {
                // On error, refetch data
                fetchData()
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    // Helper functions to get data for current user
    fun getCurrentUser(): User? {
        return _state.value.users.find { it.id == _state.value.currentUserId }
    }

    fun getCurrentUserAvailability(): Availability? {
        return _state.value.availabilities.find { it.userId == _state.value.currentUserId }
    }

    fun getUserAvailability(userId: String): Availability? {
        return _state.value.availabilities.find { it.userId == userId }
    }

    fun getCurrentUserMeetings(): List<Meeting> {
        val userId = _state.value.currentUserId
        return _state.value.meetings.filter {
            it.organizerId == userId || it.participantId == userId
        }
    }

    fun getUserById(userId: String): User? {
        return _state.value.users.find { it.id == userId }
    }
}

class SchedulerViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SchedulerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SchedulerViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
