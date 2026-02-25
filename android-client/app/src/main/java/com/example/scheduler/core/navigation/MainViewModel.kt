package com.example.scheduler.core.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scheduler.data.model.User
import com.example.scheduler.data.repositories.SchedulerRepository
import com.example.scheduler.shared.state.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: SchedulerRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> = _state.asStateFlow()

    val currentUserId: StateFlow<String> = appPreferences.currentUserId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val use24HourFormat: StateFlow<Boolean> = appPreferences.use24HourFormat
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getUsers()
                .onSuccess { users ->
                    _state.update { current ->
                        current.copy(users = users, isLoading = false)
                    }
                    // Set default user if none selected
                    if (currentUserId.value.isEmpty() && users.isNotEmpty()) {
                        setCurrentUser(users.first().id)
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
        viewModelScope.launch {
            appPreferences.setCurrentUserId(userId)
        }
    }

    fun setUse24HourFormat(use24Hour: Boolean) {
        viewModelScope.launch {
            appPreferences.setUse24HourFormat(use24Hour)
        }
    }

    fun retry() {
        fetchUsers()
    }
}
