package com.example.scheduler.features.availability

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scheduler.data.models.Availability
import com.example.scheduler.data.models.TimeSlot
import com.example.scheduler.data.models.User
import com.example.scheduler.data.repositories.SchedulerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AvailabilityState(
    val currentUser: User? = null,
    val currentUserId: String = "",
    val availabilities: List<Availability> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class AvailabilityViewModel @Inject constructor(
    private val repository: SchedulerRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AvailabilityState())
    val state: StateFlow<AvailabilityState> = _state.asStateFlow()

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.fetchAllData()
                .onSuccess { data ->
                    val currentUserId = _state.value.currentUserId
                    _state.update {
                        it.copy(
                            availabilities = data.availabilities,
                            currentUser = data.users.find { user -> user.id == currentUserId },
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
        _state.update { current ->
            current.copy(currentUserId = userId)
        }
    }

    fun setCurrentUser(user: User?) {
        _state.update { it.copy(currentUser = user) }
    }

    fun getCurrentUserAvailability(): List<TimeSlot> {
        val userId = _state.value.currentUserId
        return _state.value.availabilities.find { it.userId == userId }?.slots ?: emptyList()
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

            repository.updateAvailability(userId, slots)
                .onFailure {
                    // On error, refetch data
                    fetchData()
                }
        }
    }
}
