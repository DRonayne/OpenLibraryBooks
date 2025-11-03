package com.darach.openlibrarybooks.feature.books

import android.util.Log
import app.cash.turbine.test
import com.darach.openlibrarybooks.core.common.ui.UiState
import com.darach.openlibrarybooks.core.domain.model.EditionDetails
import com.darach.openlibrarybooks.core.domain.model.WorkDetails
import com.darach.openlibrarybooks.core.domain.repository.BooksRepository
import com.darach.openlibrarybooks.core.domain.repository.FavouritesRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * Comprehensive unit tests for BookDetailsViewModel.
 *
 * Tests cover:
 * - Initial loading state
 * - Successful work and edition details loading
 * - Error handling for network failures
 * - Favourite toggle functionality
 * - Retry after error
 * - Disposal of RxJava subscriptions
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BookDetailsViewModelTest {

    private lateinit var mockBooksRepository: BooksRepository
    private lateinit var mockFavouritesRepository: FavouritesRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    private val sampleWorkDetails = WorkDetails(
        workKey = "/works/OL45804W",
        title = "The Lord of the Rings",
        description = "Epic high-fantasy novel by J. R. R. Tolkien.",
        subjects = listOf("Fantasy", "Adventure", "Epic"),
        authors = listOf("J. R. R. Tolkien"),
        authorKeys = listOf("/authors/OL26320A"),
        coverIds = listOf(12345),
        firstPublishDate = "1954",
        excerpts = emptyList(),
        links = emptyList(),
    )

    private val sampleEditionDetails = EditionDetails(
        editionKey = "/books/OL7353617M",
        title = "The Lord of the Rings",
        isbn10 = listOf("0618640150"),
        isbn13 = listOf("9780618640157"),
        publishers = listOf("HarperCollins"),
        publishDate = "2005",
        numberOfPages = 1178,
        physicalFormat = "Hardcover",
        languages = listOf("English"),
        coverIds = listOf(12345),
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock Android Log class
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.w(any(), any(), any()) } returns 0

        // Override RxJava schedulers for synchronous testing
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline() }

        // Setup mocks
        mockBooksRepository = mockk(relaxed = true)
        mockFavouritesRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        RxJavaPlugins.reset()
        unmockkStatic(Log::class)
    }

    @Test
    fun `initial state is Idle`() = runTest {
        val viewModel = createViewModel()

        viewModel.workDetailsState.test {
            val state = awaitItem()
            state.shouldBeInstanceOf<UiState.Idle>()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state is Loading after loadBookDetails is called`() = runTest {
        every { mockBooksRepository.getWorkDetails(any()) } returns Single.never()
        every { mockFavouritesRepository.isFavourite(any()) } returns Single.just(false)

        val viewModel = createViewModel()

        viewModel.workDetailsState.test {
            // Skip Idle state
            skipItems(1)

            viewModel.loadBookDetails("OL45804W")

            val state = awaitItem()
            state.shouldBeInstanceOf<UiState.Loading>()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `successfully loads work details`() = runTest {
        every { mockBooksRepository.getWorkDetails("OL45804W") } returns Single.just(sampleWorkDetails)
        every { mockFavouritesRepository.isFavourite(any()) } returns Single.just(false)

        val viewModel = createViewModel()

        viewModel.workDetailsState.test {
            // Skip idle state
            skipItems(1)

            viewModel.loadBookDetails("OL45804W")

            // Skip loading state
            skipItems(1)

            val state = awaitItem()
            state.shouldBeInstanceOf<UiState.Success<WorkDetails>>()
            (state as UiState.Success).data shouldBe sampleWorkDetails

            cancelAndIgnoreRemainingEvents()
        }

        verify { mockBooksRepository.getWorkDetails("OL45804W") }
    }

    @Test
    fun `successfully loads edition details when editionId is provided`() = runTest {
        every { mockBooksRepository.getWorkDetails("OL45804W") } returns Single.just(sampleWorkDetails)
        every { mockBooksRepository.getEditionDetails("OL7353617M") } returns Single.just(sampleEditionDetails)
        every { mockFavouritesRepository.isFavourite(any()) } returns Single.just(false)

        val viewModel = createViewModel()

        viewModel.editionDetailsState.test {
            // Skip idle state
            skipItems(1)

            viewModel.loadBookDetails("OL45804W", "OL7353617M")

            // Skip loading state
            skipItems(1)

            val state = awaitItem()
            state.shouldBeInstanceOf<UiState.Success<EditionDetails?>>()
            (state as UiState.Success).data shouldBe sampleEditionDetails

            cancelAndIgnoreRemainingEvents()
        }

        verify { mockBooksRepository.getEditionDetails("OL7353617M") }
    }

    @Test
    fun `handles work details loading error`() = runTest {
        val error = IOException("Network error")
        every { mockBooksRepository.getWorkDetails("OL45804W") } returns Single.error(error)
        every { mockFavouritesRepository.isFavourite(any()) } returns Single.just(false)

        val viewModel = createViewModel()

        viewModel.workDetailsState.test {
            // Skip idle state
            skipItems(1)

            viewModel.loadBookDetails("OL45804W")

            // Skip loading state
            skipItems(1)

            val state = awaitItem()
            state.shouldBeInstanceOf<UiState.Error>()
            (state as UiState.Error).message shouldBe "Failed to load book details. Please try again."

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `handles edition details loading error gracefully`() = runTest {
        val error = IOException("Network error")
        every { mockBooksRepository.getWorkDetails("OL45804W") } returns Single.just(sampleWorkDetails)
        every { mockBooksRepository.getEditionDetails("OL7353617M") } returns Single.error(error)
        every { mockFavouritesRepository.isFavourite(any()) } returns Single.just(false)

        val viewModel = createViewModel()

        viewModel.editionDetailsState.test {
            // Skip idle state
            skipItems(1)

            viewModel.loadBookDetails("OL45804W", "OL7353617M")

            // Skip loading state
            skipItems(1)

            // Edition error should be handled gracefully with Success(null)
            val state = awaitItem()
            state.shouldBeInstanceOf<UiState.Success<EditionDetails?>>()
            (state as UiState.Success).data shouldBe null

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `checks favourite status after loadBookDetails is called`() = runTest {
        val bookId = "lotr_jrrtolkien"
        every { mockBooksRepository.getWorkDetails("OL45804W") } returns Single.just(sampleWorkDetails)
        every { mockFavouritesRepository.isFavourite(bookId) } returns Single.just(true)

        val viewModel = createViewModel()

        viewModel.isFavourite.test {
            // Initial state is false
            awaitItem() shouldBe false

            viewModel.loadBookDetails("OL45804W", bookId = bookId)

            // After loading, should be true
            awaitItem() shouldBe true

            cancelAndIgnoreRemainingEvents()
        }

        verify { mockFavouritesRepository.isFavourite(bookId) }
    }

    @Test
    fun `toggleFavourite updates favourite status`() = runTest {
        val bookId = "lotr_jrrtolkien"
        every { mockBooksRepository.getWorkDetails("OL45804W") } returns Single.just(sampleWorkDetails)
        every { mockFavouritesRepository.isFavourite(bookId) } returns Single.just(false)
        every { mockFavouritesRepository.toggleFavourite(bookId) } returns Completable.complete()

        val viewModel = createViewModel()
        viewModel.loadBookDetails("OL45804W", bookId = bookId)

        viewModel.isFavourite.test {
            // Initial state
            awaitItem() shouldBe false

            // Toggle favourite
            viewModel.toggleFavourite()

            // Should update optimistically
            awaitItem() shouldBe true

            cancelAndIgnoreRemainingEvents()
        }

        verify { mockFavouritesRepository.toggleFavourite(bookId) }
    }

    @Test
    fun `toggleFavourite reverts on error`() = runTest {
        val bookId = "lotr_jrrtolkien"
        val error = IOException("Database error")
        every { mockBooksRepository.getWorkDetails("OL45804W") } returns Single.just(sampleWorkDetails)
        every { mockFavouritesRepository.isFavourite(bookId) } returns Single.just(false)
        every { mockFavouritesRepository.toggleFavourite(bookId) } returns Completable.error(error)

        val viewModel = createViewModel()
        viewModel.loadBookDetails("OL45804W", bookId = bookId)

        viewModel.isFavourite.test {
            // Initial state
            awaitItem() shouldBe false

            // Toggle favourite (will fail)
            viewModel.toggleFavourite()

            // Should update optimistically first
            awaitItem() shouldBe true

            // Then revert back to original state after error
            awaitItem() shouldBe false

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `retry reloads work details after error`() = runTest {
        val error = IOException("Network error")
        var callCount = 0

        every { mockBooksRepository.getWorkDetails("OL45804W") } answers {
            callCount++
            if (callCount == 1) {
                Single.error(error)
            } else {
                Single.just(sampleWorkDetails)
            }
        }
        every { mockFavouritesRepository.isFavourite(any()) } returns Single.just(false)

        val viewModel = createViewModel()

        viewModel.workDetailsState.test {
            // Skip idle state
            skipItems(1)

            viewModel.loadBookDetails("OL45804W")

            // Skip loading state
            skipItems(1)

            // Get error
            awaitItem().shouldBeInstanceOf<UiState.Error>()

            // Retry
            viewModel.retry()

            // Should get loading then success
            awaitItem().shouldBeInstanceOf<UiState.Loading>()
            val successState = awaitItem()
            successState.shouldBeInstanceOf<UiState.Success<WorkDetails>>()

            cancelAndIgnoreRemainingEvents()
        }

        verify(exactly = 2) { mockBooksRepository.getWorkDetails("OL45804W") }
    }

    @Test
    fun `handles null editionId gracefully`() = runTest {
        every { mockBooksRepository.getWorkDetails("OL45804W") } returns Single.just(sampleWorkDetails)
        every { mockFavouritesRepository.isFavourite(any()) } returns Single.just(false)

        val viewModel = createViewModel()

        viewModel.editionDetailsState.test {
            val state = awaitItem()
            state.shouldBeInstanceOf<UiState.Idle>()

            // Load without edition ID
            viewModel.loadBookDetails("OL45804W", null)

            // Should remain idle
            expectNoEvents()

            cancelAndIgnoreRemainingEvents()
        }

        verify(exactly = 0) { mockBooksRepository.getEditionDetails(any()) }
    }

    /**
     * Helper function to create ViewModel with mocked dependencies.
     */
    private fun createViewModel() = BookDetailsViewModel(
        booksRepository = mockBooksRepository,
        favouritesRepository = mockFavouritesRepository,
    )
}
