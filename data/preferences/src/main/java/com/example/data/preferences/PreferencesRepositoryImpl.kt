package com.example.data.preferences

import androidx.datastore.core.DataStore
import com.example.data.preferences.proto.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class PreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<UserPreferences>
) : PreferencesRepository {
    override val isDarkMode: Flow<Boolean> = dataStore.data.map { it.isDarkMode }

    override suspend fun setDarkMode(enabled: Boolean) {
        dataStore.updateData { prefs ->
            prefs.toBuilder().setIsDarkMode(enabled).build()
        }
    }
}