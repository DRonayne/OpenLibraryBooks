package com.darach.openlibrarybooks.core.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.darach.openlibrarybooks.core.domain.model.FilterOptions
import com.darach.openlibrarybooks.core.domain.model.ReadingStatus
import com.darach.openlibrarybooks.core.domain.model.Settings
import com.darach.openlibrarybooks.core.domain.model.SortOption
import com.darach.openlibrarybooks.core.domain.repository.SettingsRepository
import com.darach.openlibrarybooks.core.network.api.OpenLibraryApi
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SettingsRepository using DataStore Preferences.
 *
 * Persists user settings locally and provides reactive access via Kotlin Flow.
 * Handles complex types (SortOption, FilterOptions) by serialising them to strings.
 * All DataStore operations are performed on IO dispatcher to avoid blocking.
 *
 * Helper methods are extracted to reduce cyclomatic complexity and improve
 * maintainability, which results in more functions than the default threshold.
 *
 * @property dataStore The DataStore instance for preferences
 * @property api The Open Library API for username validation
 */
@Singleton
@Suppress("TooManyFunctions")
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val api: OpenLibraryApi,
) : SettingsRepository {

    companion object {
        private const val TAG = "SettingsRepositoryImpl"

        // PreferencesKeys for all settings
        private val KEY_USERNAME = stringPreferencesKey("username")
        private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        private val KEY_DYNAMIC_THEME = booleanPreferencesKey("dynamic_theme")
        private val KEY_LAST_SYNC = longPreferencesKey("last_sync_timestamp")
        private val KEY_SORT_OPTION = stringPreferencesKey("sort_option")
        private val KEY_FILTER_READING_STATUSES = stringPreferencesKey("filter_reading_statuses")
        private val KEY_FILTER_IS_FAVORITE = stringPreferencesKey("filter_is_favorite")
        private val KEY_FILTER_SEARCH_QUERY = stringPreferencesKey("filter_search_query")
        private val KEY_FILTER_SUBJECTS = stringPreferencesKey("filter_subjects")
        private val KEY_FILTER_AUTHORS = stringPreferencesKey("filter_authors")
        private val KEY_FILTER_YEAR_FROM = stringPreferencesKey("filter_year_from")
        private val KEY_FILTER_YEAR_TO = stringPreferencesKey("filter_year_to")

        // Default values
        private const val DEFAULT_USERNAME = ""
        private const val DEFAULT_DARK_MODE = false
        private const val DEFAULT_DYNAMIC_THEME = true
        private const val DEFAULT_LAST_SYNC = 0L

        // Serialisation constants for sort options
        private const val SORT_TITLE_ASC = "title_asc"
        private const val SORT_TITLE_DESC = "title_desc"
        private const val SORT_AUTHOR_ASC = "author_asc"
        private const val SORT_AUTHOR_DESC = "author_desc"
        private const val SORT_YEAR_NEWEST = "year_newest"
        private const val SORT_YEAR_OLDEST = "year_oldest"
        private const val SORT_DATE_NEWEST = "date_newest"
        private const val SORT_DATE_OLDEST = "date_oldest"

        // Separator for list serialisation
        private const val LIST_SEPARATOR = ","
    }

    /**
     * Observe user settings from DataStore.
     *
     * Maps preferences to Settings domain model, handling deserialisation of
     * complex types. Provides default values for missing preferences and handles
     * IO errors gracefully by emitting defaults.
     *
     * @return Flow emitting the current settings
     */
    override fun getSettings(): Flow<Settings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            Settings(
                username = preferences[KEY_USERNAME] ?: DEFAULT_USERNAME,
                darkModeEnabled = preferences[KEY_DARK_MODE] ?: DEFAULT_DARK_MODE,
                dynamicThemeEnabled = preferences[KEY_DYNAMIC_THEME] ?: DEFAULT_DYNAMIC_THEME,
                lastSyncTimestamp = preferences[KEY_LAST_SYNC] ?: DEFAULT_LAST_SYNC,
                sortOption = deserializeSortOption(preferences[KEY_SORT_OPTION]),
                filterOptions = deserializeFilterOptions(preferences),
            )
        }

    /**
     * Update the Open Library username.
     *
     * @param username The new username
     * @return Completable that completes when the username is updated
     */
    override fun updateUsername(username: String): Completable = Completable.fromCallable {
        Log.i(TAG, "Updating username to: $username")
    }
        .andThen(
            Completable.fromAction {
                kotlinx.coroutines.runBlocking {
                    dataStore.edit { preferences ->
                        preferences[KEY_USERNAME] = username
                    }
                }
            },
        )
        .subscribeOn(Schedulers.io())
        .doOnComplete {
            Log.i(TAG, "Successfully updated username to: $username")
        }
        .doOnError { error ->
            Log.e(TAG, "Failed to update username", error)
        }

    /**
     * Validate that a username exists on Open Library.
     *
     * Makes an API call to the want-to-read shelf to verify the username.
     * A successful response (even with 0 books) indicates the username is valid.
     *
     * @param username The username to validate
     * @return Single emitting true if valid, false otherwise
     */
    override fun validateUsername(username: String): Single<Boolean> {
        Log.i(TAG, "Validating username: $username")
        return api.getReadingList(username, "want-to-read", page = 1)
            .subscribeOn(Schedulers.io())
            .map { true }
            .doOnSuccess {
                Log.i(TAG, "Username validation successful: $username")
            }
            .onErrorReturn { error ->
                Log.w(TAG, "Username validation failed for: $username", error)
                false
            }
    }

    /**
     * Toggle dark mode on or off.
     *
     * @param enabled True to enable dark mode, false to disable
     * @return Completable that completes when the setting is updated
     */
    override fun toggleDarkMode(enabled: Boolean): Completable = Completable.fromAction {
        Log.i(TAG, "Toggling dark mode: $enabled")
        kotlinx.coroutines.runBlocking {
            dataStore.edit { preferences ->
                preferences[KEY_DARK_MODE] = enabled
            }
        }
    }
        .subscribeOn(Schedulers.io())
        .doOnComplete {
            Log.i(TAG, "Successfully toggled dark mode: $enabled")
        }
        .doOnError { error ->
            Log.e(TAG, "Failed to toggle dark mode", error)
        }

    /**
     * Toggle dynamic theming (Material You) on or off.
     *
     * @param enabled True to enable dynamic theming, false to disable
     * @return Completable that completes when the setting is updated
     */
    override fun toggleDynamicTheme(enabled: Boolean): Completable = Completable.fromAction {
        Log.i(TAG, "Toggling dynamic theme: $enabled")
        kotlinx.coroutines.runBlocking {
            dataStore.edit { preferences ->
                preferences[KEY_DYNAMIC_THEME] = enabled
            }
        }
    }
        .subscribeOn(Schedulers.io())
        .doOnComplete {
            Log.i(TAG, "Successfully toggled dynamic theme: $enabled")
        }
        .doOnError { error ->
            Log.e(TAG, "Failed to toggle dynamic theme", error)
        }

    /**
     * Observe the last sync timestamp.
     *
     * @return Flow emitting the last sync timestamp
     */
    override fun getLastSyncTimestamp(): Flow<Long> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading last sync timestamp", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_LAST_SYNC] ?: DEFAULT_LAST_SYNC
        }

    /**
     * Update the last sync timestamp.
     *
     * @param timestamp The sync timestamp in milliseconds since epoch
     * @return Completable that completes when the timestamp is updated
     */
    override fun updateLastSyncTimestamp(timestamp: Long): Completable = Completable.fromAction {
        Log.i(TAG, "Updating last sync timestamp: $timestamp")
        kotlinx.coroutines.runBlocking {
            dataStore.edit { preferences ->
                preferences[KEY_LAST_SYNC] = timestamp
            }
        }
    }
        .subscribeOn(Schedulers.io())
        .doOnComplete {
            Log.i(TAG, "Successfully updated last sync timestamp: $timestamp")
        }
        .doOnError { error ->
            Log.e(TAG, "Failed to update last sync timestamp", error)
        }

    /**
     * Update the sort option for book lists.
     *
     * @param sortOption The new sort option
     * @return Completable that completes when the setting is updated
     */
    override fun updateSortOption(sortOption: SortOption): Completable = Completable.fromAction {
        Log.i(TAG, "Updating sort option: ${sortOption.displayName()}")
        kotlinx.coroutines.runBlocking {
            dataStore.edit { preferences ->
                preferences[KEY_SORT_OPTION] = serializeSortOption(sortOption)
            }
        }
    }
        .subscribeOn(Schedulers.io())
        .doOnComplete {
            Log.i(TAG, "Successfully updated sort option: ${sortOption.displayName()}")
        }
        .doOnError { error ->
            Log.e(TAG, "Failed to update sort option", error)
        }

    /**
     * Update the filter options for book lists.
     *
     * @param filterOptions The new filter options
     * @return Completable that completes when the setting is updated
     */
    override fun updateFilterOptions(filterOptions: FilterOptions): Completable = Completable.fromAction {
        Log.i(TAG, "Updating filter options")
        kotlinx.coroutines.runBlocking {
            dataStore.edit { preferences ->
                serializeFilterOptions(filterOptions, preferences)
            }
        }
    }
        .subscribeOn(Schedulers.io())
        .doOnComplete {
            Log.i(TAG, "Successfully updated filter options")
        }
        .doOnError { error ->
            Log.e(TAG, "Failed to update filter options", error)
        }

    /**
     * Clear all settings and reset to defaults.
     *
     * @return Completable that completes when all settings are cleared
     */
    override fun clearSettings(): Completable = Completable.fromAction {
        Log.i(TAG, "Clearing all settings")
        kotlinx.coroutines.runBlocking {
            dataStore.edit { preferences ->
                preferences.clear()
            }
        }
    }
        .subscribeOn(Schedulers.io())
        .doOnComplete {
            Log.i(TAG, "Successfully cleared all settings")
        }
        .doOnError { error ->
            Log.e(TAG, "Failed to clear settings", error)
        }

    /**
     * Serialise a SortOption to a string for storage.
     *
     * @param sortOption The sort option to serialise
     * @return Serialised string representation
     */
    private fun serializeSortOption(sortOption: SortOption): String = when (sortOption) {
        is SortOption.TitleAscending -> SORT_TITLE_ASC
        is SortOption.TitleDescending -> SORT_TITLE_DESC
        is SortOption.AuthorAscending -> SORT_AUTHOR_ASC
        is SortOption.AuthorDescending -> SORT_AUTHOR_DESC
        is SortOption.PublishYearNewest -> SORT_YEAR_NEWEST
        is SortOption.PublishYearOldest -> SORT_YEAR_OLDEST
        is SortOption.DateAddedNewest -> SORT_DATE_NEWEST
        is SortOption.DateAddedOldest -> SORT_DATE_OLDEST
    }

    /**
     * Deserialise a string to a SortOption.
     *
     * @param value The serialised string value
     * @return The corresponding SortOption, or default if invalid
     */
    private fun deserializeSortOption(value: String?): SortOption = when (value) {
        SORT_TITLE_ASC -> SortOption.TitleAscending
        SORT_TITLE_DESC -> SortOption.TitleDescending
        SORT_AUTHOR_ASC -> SortOption.AuthorAscending
        SORT_AUTHOR_DESC -> SortOption.AuthorDescending
        SORT_YEAR_NEWEST -> SortOption.PublishYearNewest
        SORT_YEAR_OLDEST -> SortOption.PublishYearOldest
        SORT_DATE_NEWEST -> SortOption.DateAddedNewest
        SORT_DATE_OLDEST -> SortOption.DateAddedOldest
        else -> SortOption.DateAddedNewest // Default
    }

    /**
     * Serialise FilterOptions to preferences.
     *
     * Stores each filter property as a separate preference key.
     * Lists are serialised as comma-separated strings.
     *
     * @param filterOptions The filter options to serialise
     * @param preferences The mutable preferences to update
     */
    private fun serializeFilterOptions(
        filterOptions: FilterOptions,
        preferences: androidx.datastore.preferences.core.MutablePreferences,
    ) {
        // Serialise reading statuses as comma-separated string
        val statusesStr = filterOptions.readingStatuses.joinToString(LIST_SEPARATOR) { it.name }
        preferences[KEY_FILTER_READING_STATUSES] = statusesStr

        // Serialise is favorite (nullable boolean)
        preferences[KEY_FILTER_IS_FAVORITE] = filterOptions.isFavorite?.toString() ?: ""

        // Search query
        preferences[KEY_FILTER_SEARCH_QUERY] = filterOptions.searchQuery ?: ""

        // Subjects as comma-separated string
        preferences[KEY_FILTER_SUBJECTS] = filterOptions.subjects.joinToString(LIST_SEPARATOR)

        // Authors as comma-separated string
        preferences[KEY_FILTER_AUTHORS] = filterOptions.authors.joinToString(LIST_SEPARATOR)

        // Year range
        preferences[KEY_FILTER_YEAR_FROM] = filterOptions.yearFrom?.toString() ?: ""
        preferences[KEY_FILTER_YEAR_TO] = filterOptions.yearTo?.toString() ?: ""
    }

    /**
     * Deserialise FilterOptions from preferences.
     *
     * Reads all filter-related preferences and constructs a FilterOptions instance.
     * Provides default values for missing or invalid data.
     *
     * @param preferences The preferences to read from
     * @return The deserialised FilterOptions
     */
    private fun deserializeFilterOptions(preferences: Preferences): FilterOptions = FilterOptions(
        readingStatuses = deserializeReadingStatuses(preferences[KEY_FILTER_READING_STATUSES]),
        isFavorite = deserializeNullableBoolean(preferences[KEY_FILTER_IS_FAVORITE]),
        searchQuery = preferences[KEY_FILTER_SEARCH_QUERY]?.takeIf { it.isNotBlank() },
        subjects = deserializeStringList(preferences[KEY_FILTER_SUBJECTS]),
        authors = deserializeStringList(preferences[KEY_FILTER_AUTHORS]),
        yearFrom = preferences[KEY_FILTER_YEAR_FROM]?.toIntOrNull(),
        yearTo = preferences[KEY_FILTER_YEAR_TO]?.toIntOrNull(),
    )

    /**
     * Deserialise reading statuses from a comma-separated string.
     *
     * @param statusesStr The comma-separated string of reading statuses
     * @return Set of reading statuses, or default if not set
     */
    private fun deserializeReadingStatuses(statusesStr: String?): Set<ReadingStatus> {
        // If never set (null), use default
        if (statusesStr == null) {
            return setOf(ReadingStatus.WantToRead)
        }

        // If explicitly set to empty, preserve empty set
        if (statusesStr.isBlank()) {
            return emptySet()
        }

        return statusesStr.split(LIST_SEPARATOR)
            .mapNotNull { status ->
                try {
                    ReadingStatus.valueOf(status)
                } catch (e: IllegalArgumentException) {
                    Log.w(TAG, "Invalid reading status in preferences: $status", e)
                    null
                }
            }
            .toSet()
    }

    /**
     * Deserialise a nullable boolean from a string.
     *
     * @param value The string value ("true", "false", or empty)
     * @return Boolean value, or null if empty/invalid
     */
    private fun deserializeNullableBoolean(value: String?): Boolean? = when (value) {
        "true" -> true
        "false" -> false
        else -> null
    }

    /**
     * Deserialise a comma-separated list of strings.
     *
     * @param listStr The comma-separated string
     * @return List of strings, or empty list if invalid
     */
    private fun deserializeStringList(listStr: String?): List<String> {
        if (listStr.isNullOrBlank()) {
            return emptyList()
        }
        return listStr.split(LIST_SEPARATOR)
    }
}
