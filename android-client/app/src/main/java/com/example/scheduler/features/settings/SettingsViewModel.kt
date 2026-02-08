package com.example.scheduler.features.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scheduler.data.models.User
import com.example.scheduler.data.repositories.SchedulerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val users: List<User> = emptyList(),
    val currentUserId: String = "",
    val currentUser: User? = null,
    val use24HourFormat: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val repository: SchedulerRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val CURRENT_USER_KEY = stringPreferencesKey("current_user_id")
    private val USE_24_HOUR_FORMAT_KEY = stringPreferencesKey("use_24_hour_format")

    init {
        loadPersistedSettings()
        fetchData()
    }

    private fun loadPersistedSettings() {
        viewModelScope.launch {
            dataStore.data.first().let { prefs ->
                val userId = prefs[CURRENT_USER_KEY] ?: ""
                val use24HourFormat = prefs[USE_24_HOUR_FORMAT_KEY]?.toBoolean() ?: false
                _state.update {
                    it.copy(currentUserId = userId, use24HourFormat = use24HourFormat)
                }
            }
        }
    }

    fun fetchData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.fetchAllData()
                .onSuccess { data ->
                    val currentUserId = _state.value.currentUserId.ifEmpty {
                        data.users.firstOrNull()?.id ?: ""
                    }
                    _state.update {
                        it.copy(
                            users = data.users,
                            currentUserId = currentUserId,
                            currentUser = data.users.find { user -> user.id == currentUserId },
                            isLoading = false
                        )
                    }
                    // Persist the current user ID if it was empty
                    if (_state.value.currentUserId.isNotEmpty()) {
                        persistCurrentUserId(_state.value.currentUserId)
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(isLoading = false, error = e.message ?: "Unknown error")
                    }
                }
        }
    }

    fun setCurrentUser(userId: String) {
        _state.update { current ->
            current.copy(
                currentUserId = userId,
                currentUser = current.users.find { it.id == userId }
            )
        }
        viewModelScope.launch {
            persistCurrentUserId(userId)
        }
    }

    private suspend fun persistCurrentUserId(userId: String) {
        dataStore.edit { prefs ->
            prefs[CURRENT_USER_KEY] = userId
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
}
