package com.example.scheduler.data.models

import com.squareup.moshi.JsonClass

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
