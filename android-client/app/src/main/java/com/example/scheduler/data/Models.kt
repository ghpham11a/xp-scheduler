package com.example.scheduler.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    val id: String,
    val name: String,
    val email: String,
    @Json(name = "avatarColor") val avatarColor: String
)

@JsonClass(generateAdapter = true)
data class TimeSlot(
    val date: String,       // ISO date (YYYY-MM-DD)
    val startHour: Double,  // 0-24, supports 0.5 increments for 30-min blocks
    val endHour: Double
)

@JsonClass(generateAdapter = true)
data class Availability(
    val userId: String,
    val slots: List<TimeSlot>
)

@JsonClass(generateAdapter = true)
data class Meeting(
    val id: String,
    val organizerId: String,
    val participantId: String,
    val date: String,       // ISO date (YYYY-MM-DD)
    val startHour: Double,
    val endHour: Double,
    val title: String
)

// Request body for creating a meeting (no id field)
@JsonClass(generateAdapter = true)
data class CreateMeetingRequest(
    val organizerId: String,
    val participantId: String,
    val date: String,
    val startHour: Double,
    val endHour: Double,
    val title: String
)

// Response for delete meeting
@JsonClass(generateAdapter = true)
data class DeleteMeetingResponse(
    val status: String,
    val id: String
)
