package com.example.scheduler.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateMeetingRequest(
    val organizerId: String,
    val participantId: String,
    val date: String,
    val startHour: Double,
    val endHour: Double,
    val title: String
)
