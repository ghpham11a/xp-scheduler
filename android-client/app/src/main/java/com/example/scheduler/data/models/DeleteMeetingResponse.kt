package com.example.scheduler.data.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeleteMeetingResponse(
    val status: String,
    val id: String
)
