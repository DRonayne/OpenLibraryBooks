package com.darach.openlibrarybooks.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.darach.openlibrarybooks.core.domain.model.FilterOptions
import com.darach.openlibrarybooks.core.domain.model.ReadingStatus
import com.darach.openlibrarybooks.core.domain.model.SortOption
import com.darach.openlibrarybooks.core.network.api.OpenLibraryApi
import com.darach.openlibrarybooks.core.network.dto.ReadingListResponseDto
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Unit tests for SettingsRepositoryImpl.
 *
 * Tests cover:
 * - DataStore persistence and retrieval
 * - Username validation via API
 * - Theme settings (dark mode, dynamic theme)
 * - Sort and filter options serialisation
 * - Settings reset/clear functionality
 * - Default values and error handling
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsRepositoryImplTest {

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    private lateinit var repository: SettingsRepositoryImpl
    private lateinit var testDataStore: DataStore<Preferences>
    private lateinit var mockApi: OpenLibraryApi

    @Before
    fun setup() {
        // Override RxJava schedulers for synchronous testing
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline() }

        // Create test DataStore with temporary file
        val testFile = tmpFolder.newFile("test_datastore.preferences_pb")
        testDataStore = PreferenceDataStoreFactory.create {
            testFile
        }

        mockApi = mockk()
        repository = SettingsRepositoryImpl(testDataStore, mockApi)
    }

    @After
    fun tearDown() {
        RxJavaPlugins.reset()
    }

    @Test
    fun `getSettings returns default values when no preferences are set`() = runTest {
        // When
        val settings = repository.getSettings().first()

        // Then
        assert(settings.username == "")
        assert(!settings.darkModeEnabled)
        assert(settings.dynamicThemeEnabled)
        assert(settings.lastSyncTimestamp == 0L)
        assert(settings.sortOption is SortOption.DateAddedNewest)
        assert(settings.filterOptions.readingStatuses == setOf(ReadingStatus.WantToRead))
    }

    @Test
    fun `updateUsername persists username to DataStore`() = runTest {
        // Given
        val username = "testuser"

        // When
        repository.updateUsername(username).test().await()

        // Then
        val settings = repository.getSettings().first()
        assert(settings.username == username)
    }

    @Test
    fun `validateUsername returns true for valid username`() = runTest {
        // Given
        val username = "validuser"
        val mockResponse = ReadingListResponseDto(page = 1, readingLogEntries = emptyList())
        every { mockApi.getReadingList(username, "want-to-read", 1) } returns Single.just(mockResponse)

        // When
        val testObserver = repository.validateUsername(username).test()

        // Then
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        assert(testObserver.values().first() == true)
    }

    @Test
    fun `validateUsername returns false for invalid username`() = runTest {
        // Given
        val username = "invaliduser"
        every { mockApi.getReadingList(username, "want-to-read", 1) } returns
            Single.error(Exception("User not found"))

        // When
        val testObserver = repository.validateUsername(username).test()

        // Then
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        assert(testObserver.values().first() == false)
    }

    @Test
    fun `toggleDarkMode persists dark mode setting`() = runTest {
        // When
        repository.toggleDarkMode(true).test().await()

        // Then
        val settings = repository.getSettings().first()
        assert(settings.darkModeEnabled)

        // When disabled
        repository.toggleDarkMode(false).test().await()

        // Then
        val updatedSettings = repository.getSettings().first()
        assert(!updatedSettings.darkModeEnabled)
    }

    @Test
    fun `toggleDynamicTheme persists dynamic theme setting`() = runTest {
        // When
        repository.toggleDynamicTheme(false).test().await()

        // Then
        val settings = repository.getSettings().first()
        assert(!settings.dynamicThemeEnabled)

        // When enabled
        repository.toggleDynamicTheme(true).test().await()

        // Then
        val updatedSettings = repository.getSettings().first()
        assert(updatedSettings.dynamicThemeEnabled)
    }

    @Test
    fun `updateLastSyncTimestamp persists timestamp`() = runTest {
        // Given
        val timestamp = System.currentTimeMillis()

        // When
        repository.updateLastSyncTimestamp(timestamp).test().await()

        // Then
        val syncTimestamp = repository.getLastSyncTimestamp().first()
        assert(syncTimestamp == timestamp)
    }

    @Test
    fun `getLastSyncTimestamp returns 0L when not set`() = runTest {
        // When
        val timestamp = repository.getLastSyncTimestamp().first()

        // Then
        assert(timestamp == 0L)
    }

    @Test
    fun `updateSortOption persists sort option`() = runTest {
        // Given
        val sortOption = SortOption.TitleAscending

        // When
        repository.updateSortOption(sortOption).test().await()

        // Then
        val settings = repository.getSettings().first()
        assert(settings.sortOption is SortOption.TitleAscending)
    }

    @Test
    fun `updateSortOption handles all sort option types`() = runTest {
        // Test each sort option type
        val sortOptions = listOf(
            SortOption.TitleAscending,
            SortOption.TitleDescending,
            SortOption.AuthorAscending,
            SortOption.AuthorDescending,
            SortOption.PublishYearNewest,
            SortOption.PublishYearOldest,
            SortOption.DateAddedNewest,
            SortOption.DateAddedOldest,
        )

        sortOptions.forEach { sortOption ->
            // When
            repository.updateSortOption(sortOption).test().await()

            // Then
            val settings = repository.getSettings().first()
            assert(settings.sortOption::class == sortOption::class)
        }
    }

    @Test
    fun `updateFilterOptions persists filter options`() = runTest {
        // Given
        val filterOptions = FilterOptions(
            readingStatuses = setOf(ReadingStatus.CurrentlyReading, ReadingStatus.AlreadyRead),
            isFavorite = true,
            searchQuery = "test query",
            subjects = listOf("Fiction", "Adventure"),
            authors = listOf("Author A", "Author B"),
            yearFrom = 2000,
            yearTo = 2020,
        )

        // When
        repository.updateFilterOptions(filterOptions).test().await()

        // Then
        val settings = repository.getSettings().first()
        assert(
            settings.filterOptions.readingStatuses == setOf(ReadingStatus.CurrentlyReading, ReadingStatus.AlreadyRead),
        )
        assert(settings.filterOptions.isFavorite == true)
        assert(settings.filterOptions.searchQuery == "test query")
        assert(settings.filterOptions.subjects == listOf("Fiction", "Adventure"))
        assert(settings.filterOptions.authors == listOf("Author A", "Author B"))
        assert(settings.filterOptions.yearFrom == 2000)
        assert(settings.filterOptions.yearTo == 2020)
    }

    @Test
    fun `updateFilterOptions handles empty filter options`() = runTest {
        // Given
        val emptyFilterOptions = FilterOptions(
            readingStatuses = emptySet(),
            isFavorite = null,
            searchQuery = null,
            subjects = emptyList(),
            authors = emptyList(),
            yearFrom = null,
            yearTo = null,
        )

        // When
        repository.updateFilterOptions(emptyFilterOptions).test().await()

        // Then
        val settings = repository.getSettings().first()
        assert(settings.filterOptions.isEmpty())
    }

    @Test
    fun `updateFilterOptions handles null filter values`() = runTest {
        // Given
        val filterOptions = FilterOptions(
            readingStatuses = emptySet(),
            isFavorite = null,
            searchQuery = null,
            subjects = emptyList(),
            authors = emptyList(),
            yearFrom = null,
            yearTo = null,
        )

        // When
        repository.updateFilterOptions(filterOptions).test().await()

        // Then
        val settings = repository.getSettings().first()
        assert(settings.filterOptions.isFavorite == null)
        assert(settings.filterOptions.searchQuery == null)
        assert(settings.filterOptions.yearFrom == null)
        assert(settings.filterOptions.yearTo == null)
    }

    @Test
    fun `clearSettings removes all stored preferences`() = runTest {
        // Given - Set multiple preferences
        repository.updateUsername("testuser").test().await()
        repository.toggleDarkMode(true).test().await()
        repository.updateSortOption(SortOption.TitleAscending).test().await()

        // When
        repository.clearSettings().test().await()

        // Then - All settings should be reset to defaults
        val settings = repository.getSettings().first()
        assert(settings.username == "")
        assert(!settings.darkModeEnabled)
        assert(settings.dynamicThemeEnabled)
        assert(settings.lastSyncTimestamp == 0L)
        assert(settings.sortOption is SortOption.DateAddedNewest)
    }

    @Test
    fun `multiple settings updates are persisted correctly`() = runTest {
        // Given
        val username = "user123"
        val sortOption = SortOption.AuthorDescending
        val filterOptions = FilterOptions(
            readingStatuses = setOf(ReadingStatus.WantToRead),
            isFavorite = true,
        )

        // When
        repository.updateUsername(username).test().await()
        repository.toggleDarkMode(true).test().await()
        repository.toggleDynamicTheme(false).test().await()
        repository.updateSortOption(sortOption).test().await()
        repository.updateFilterOptions(filterOptions).test().await()

        // Then
        val settings = repository.getSettings().first()
        assert(settings.username == username)
        assert(settings.darkModeEnabled)
        assert(!settings.dynamicThemeEnabled)
        assert(settings.sortOption is SortOption.AuthorDescending)
        assert(settings.filterOptions.isFavorite == true)
    }

    @Test
    fun `settings persist across repository instances`() = runTest {
        // Given - Set settings with first repository instance
        val username = "persistentuser"
        repository.updateUsername(username).test().await()

        // When - Create new repository instance with same DataStore
        val newRepository = SettingsRepositoryImpl(testDataStore, mockApi)

        // Then - Settings should still be available
        val settings = newRepository.getSettings().first()
        assert(settings.username == username)
    }
}
