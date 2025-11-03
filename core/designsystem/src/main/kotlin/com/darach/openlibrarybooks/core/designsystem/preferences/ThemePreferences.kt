package com.darach.openlibrarybooks.core.designsystem.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Preferences keys for theme settings.
 */
private object ThemePreferencesKeys {
    val DARK_MODE = booleanPreferencesKey("dark_mode")
    val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
}

/**
 * Data class representing the current theme preferences.
 *
 * @property darkMode Whether dark mode is enabled. Null means follow system setting.
 * @property dynamicColor Whether dynamic colours (Material You) are enabled.
 */
data class ThemePreferencesData(val darkMode: Boolean? = null, val dynamicColor: Boolean = true)

/**
 * Repository for managing theme-related preferences using DataStore.
 *
 * Provides Kotlin Flow API for reading theme preferences and suspend functions for updating them.
 */
@Singleton
class ThemePreferencesRepository @Inject constructor(private val dataStore: DataStore<Preferences>) {

    /**
     * Flow of theme preferences. Emits whenever preferences change.
     */
    val themePreferences: Flow<ThemePreferencesData> = dataStore.data
        .catch { exception ->
            // If we encounter an error reading data, emit empty preferences
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            mapPreferences(preferences)
        }

    /**
     * Updates the dark mode preference.
     *
     * @param enabled True for dark mode, false for light mode, null to follow system.
     */
    suspend fun setDarkMode(enabled: Boolean?) {
        dataStore.edit { preferences ->
            if (enabled == null) {
                preferences.remove(ThemePreferencesKeys.DARK_MODE)
            } else {
                preferences[ThemePreferencesKeys.DARK_MODE] = enabled
            }
        }
    }

    /**
     * Updates the dynamic colour preference.
     *
     * @param enabled True to enable dynamic colours (Material You), false for static colours.
     */
    suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[ThemePreferencesKeys.DYNAMIC_COLOR] = enabled
        }
    }

    /**
     * Maps DataStore preferences to ThemePreferencesData.
     */
    private fun mapPreferences(preferences: Preferences): ThemePreferencesData {
        val darkMode = if (preferences.contains(ThemePreferencesKeys.DARK_MODE)) {
            preferences[ThemePreferencesKeys.DARK_MODE]
        } else {
            null // Follow system setting
        }

        val dynamicColor = preferences[ThemePreferencesKeys.DYNAMIC_COLOR] ?: true

        return ThemePreferencesData(
            darkMode = darkMode,
            dynamicColor = dynamicColor,
        )
    }
}
