package com.example.scheduler.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scheduler.data.models.Availability
import com.example.scheduler.data.models.Meeting
import com.example.scheduler.data.models.User
import com.example.scheduler.data.repositories.SchedulerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Root state container for app-wide shared state.
 * Each screen has its own ViewModel for screen-specific operations.
 */
data class SchedulerState(
    val currentUserId: String = "",
    val users: List<User> = emptyList(),
    val availabilities: List<Availability> = emptyList(),
    val meetings: List<Meeting> = emptyList(),
    val use24HourFormat: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * Root ViewModel that manages shared app state.
 * Provides currentUserId and users list to child screens.
 * Individual screens use their own ViewModels for screen-specific operations.
 */
@HiltViewModel
class SchedulerViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val repository: SchedulerRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SchedulerState())
    val state: StateFlow<SchedulerState> = _state.asStateFlow()

    private val CURRENT_USER_KEY = stringPreferencesKey("current_user_id")
    private val USE_24_HOUR_FORMAT_KEY = stringPreferencesKey("use_24_hour_format")

    init {
        loadPersistedSettings()
        fetchData()
    }

    private fun loadPersistedSettings() {
        viewModelScope.launch {
            dataStore.data.first().let { prefs ->
                val userId = prefs[CURRENT_USER_KEY]
                val use24HourFormat = prefs[USE_24_HOUR_FORMAT_KEY]?.toBoolean() ?: false
                _state.update {
                    it.copy(
                        currentUserId = userId ?: "",
                        use24HourFormat = use24HourFormat
                    )
                }
            }
        }
    }

    fun setUse24HourFormat(use24Hour: Boolean) {
        _state.update { it.copy(use24HourFormat = use24Hour) }
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[USE_24_HOUR_FORMAT_KEY] = use24Hour.toString()
            }
        }
    }

    fun fetchData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.fetchAllData()
                .onSuccess { data ->
                    _state.update { current ->
                        val currentUserId = if (current.currentUserId.isEmpty() && data.users.isNotEmpty()) {
                            data.users.first().id
                        } else {
                            current.currentUserId
                        }

                        current.copy(
                            users = data.users,
                            availabilities = data.availabilities,
                            meetings = data.meetings,
                            currentUserId = currentUserId,
                            isLoading = false
                        )
                    }

                    // Persist the current user ID
                    if (_state.value.currentUserId.isNotEmpty()) {
                        persistCurrentUserId(_state.value.currentUserId)
                    }
                }
                .onFailure { e ->
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
        dataStore.edit { prefs ->
            prefs[CURRENT_USER_KEY] = userId
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
