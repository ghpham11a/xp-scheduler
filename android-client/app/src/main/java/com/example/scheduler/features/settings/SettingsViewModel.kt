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
    val showAllHours: Boolean = false,
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
    private val SHOW_ALL_HOURS_KEY = stringPreferencesKey("show_all_hours")

    init {
        loadPersistedSettings()
        fetchData()
    }

    private fun loadPersistedSettings() {
        viewModelScope.launch {
            dataStore.data.first().let { prefs ->
                val userId = prefs[CURRENT_USER_KEY] ?: ""
                val showAllHours = prefs[SHOW_ALL_HOURS_KEY]?.toBoolean() ?: false
                _state.update {
                    it.copy(currentUserId = userId, showAllHours = showAllHours)
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

    fun setShowAllHours(show: Boolean) {
        _state.update { it.copy(showAllHours = show) }
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[SHOW_ALL_HOURS_KEY] = show.toString()
            }
        }
    }
}
