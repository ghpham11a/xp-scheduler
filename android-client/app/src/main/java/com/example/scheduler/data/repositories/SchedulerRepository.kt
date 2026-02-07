package com.example.scheduler.data.repositories

import com.example.scheduler.data.networking.SchedulerApi
import com.example.scheduler.data.models.Availability
import com.example.scheduler.data.models.CreateMeetingRequest
import com.example.scheduler.data.models.Meeting
import com.example.scheduler.data.models.TimeSlot
import com.example.scheduler.data.models.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SchedulerRepository @Inject constructor(
    private val api: SchedulerApi
) {
    // Users
    suspend fun getUsers(): Result<List<User>> {
        return runCatching { api.getUsers() }
    }

    suspend fun getUser(id: String): Result<User> {
        return runCatching { api.getUser(id) }
    }

    // Availabilities
    suspend fun getAvailabilities(): Result<List<Availability>> {
        return runCatching { api.getAvailabilities() }
    }

    suspend fun getAvailability(userId: String): Result<Availability> {
        return runCatching { api.getAvailability(userId) }
    }

    suspend fun updateAvailability(userId: String, slots: List<TimeSlot>): Result<Availability> {
        return runCatching { api.updateAvailability(userId, slots) }
    }

    // Meetings
    suspend fun getMeetings(): Result<List<Meeting>> {
        return runCatching { api.getMeetings() }
    }

    suspend fun getMeeting(id: String): Result<Meeting> {
        return runCatching { api.getMeeting(id) }
    }

    suspend fun createMeeting(
        organizerId: String,
        participantId: String,
        date: String,
        startHour: Double,
        endHour: Double,
        title: String
    ): Result<Meeting> {
        return runCatching {
            val request = CreateMeetingRequest(
                organizerId = organizerId,
                participantId = participantId,
                date = date,
                startHour = startHour,
                endHour = endHour,
                title = title
            )
            api.createMeeting(request)
        }
    }

    suspend fun deleteMeeting(id: String): Result<Unit> {
        return runCatching {
            api.deleteMeeting(id)
            Unit
        }
    }

    // Fetch all data at once
    suspend fun fetchAllData(): Result<SchedulerData> {
        return runCatching {
            val users = api.getUsers()
            val availabilities = api.getAvailabilities()
            val meetings = api.getMeetings()
            SchedulerData(users, availabilities, meetings)
        }
    }
}

data class SchedulerData(
    val users: List<User>,
    val availabilities: List<Availability>,
    val meetings: List<Meeting>
)
