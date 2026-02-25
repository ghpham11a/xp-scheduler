package com.example.scheduler.shared.state

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val CURRENT_USER_KEY = stringPreferencesKey("current_user_id")
    private val USE_24_HOUR_FORMAT_KEY = stringPreferencesKey("use_24_hour_format")

    val currentUserId: Flow<String> = dataStore.data.map { prefs ->
        prefs[CURRENT_USER_KEY] ?: ""
    }

    val use24HourFormat: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[USE_24_HOUR_FORMAT_KEY]?.toBoolean() ?: false
    }

    suspend fun setCurrentUserId(userId: String) {
        dataStore.edit { prefs ->
            prefs[CURRENT_USER_KEY] = userId
        }
    }

    suspend fun setUse24HourFormat(use24Hour: Boolean) {
        dataStore.edit { prefs ->
            prefs[USE_24_HOUR_FORMAT_KEY] = use24Hour.toString()
        }
    }
}
