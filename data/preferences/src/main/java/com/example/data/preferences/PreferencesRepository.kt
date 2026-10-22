package com.example.data.preferences

import kotlinx.coroutines.flow.Flow

// :data:preferences 模块中
interface PreferencesRepository {
    val isDarkMode: Flow<Boolean>

    suspend fun setDarkMode(enabled: Boolean)
}
