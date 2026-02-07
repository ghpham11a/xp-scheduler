package com.example.scheduler.data.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Availability(
    val userId: String,
    val slots: List<TimeSlot>
)
