package com.example.scheduler.data.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TimeSlot(
    val date: String,       // ISO date (YYYY-MM-DD)
    val startHour: Double,  // 0-24, supports 0.5 increments for 30-min blocks
    val endHour: Double
)
