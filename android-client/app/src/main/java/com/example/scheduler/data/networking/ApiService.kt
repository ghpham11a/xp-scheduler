package com.example.scheduler.data.networking

import com.example.scheduler.data.models.Availability
import com.example.scheduler.data.models.CreateMeetingRequest
import com.example.scheduler.data.models.DeleteMeetingResponse
import com.example.scheduler.data.models.Meeting
import com.example.scheduler.data.models.TimeSlot
import com.example.scheduler.data.models.User
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

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

object ApiClient {
    // Use 10.0.2.2 for Android emulator to access host machine's localhost
    private const val BASE_URL = "http://10.0.2.2:6969/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val api: SchedulerApi = retrofit.create(SchedulerApi::class.java)
}
