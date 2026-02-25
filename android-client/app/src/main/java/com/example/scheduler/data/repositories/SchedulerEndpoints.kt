package com.example.scheduler.data.repositories

import com.example.scheduler.data.model.Availability
import com.example.scheduler.data.model.CreateMeetingRequest
import com.example.scheduler.data.model.DeleteMeetingResponse
import com.example.scheduler.data.model.Meeting
import com.example.scheduler.data.model.TimeSlot
import com.example.scheduler.data.model.User
import retrofit2.http.*

interface SchedulerApi {
    // Users
    @GET("users")
    suspend fun getUsers(): List<User>

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): User

    // Availabilities
    @GET("availabilities")
    suspend fun getAvailabilities(): List<Availability>

    @GET("availabilities/{userId}")
    suspend fun getAvailability(@Path("userId") userId: String): Availability

    @PUT("availabilities/{userId}")
    suspend fun updateAvailability(
        @Path("userId") userId: String,
        @Body slots: List<TimeSlot>
    ): Availability

    // Meetings
    @GET("meetings")
    suspend fun getMeetings(): List<Meeting>

    @GET("meetings/{id}")
    suspend fun getMeeting(@Path("id") id: String): Meeting

    @POST("meetings")
    suspend fun createMeeting(@Body meeting: CreateMeetingRequest): Meeting

    @DELETE("meetings/{id}")
    suspend fun deleteMeeting(@Path("id") id: String): DeleteMeetingResponse
}
