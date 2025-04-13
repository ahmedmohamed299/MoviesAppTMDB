package com.android.movieappmazaady.presentation.home

import android.content.Context
import android.net.ConnectivityManager
import androidx.paging.PagingData
import com.android.movieappmazaady.data.local.FakeMovieDao
import com.android.movieappmazaady.data.model.Movie
import com.android.movieappmazaady.data.remote.FakeMovieApi
import com.android.movieappmazaady.data.repository.MovieRepositoryImpl
import com.android.movieappmazaady.util.FakeNetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import kotlin.test.assertTrue

class HomeViewModelTest {
    @Mock
    private lateinit var mockContext: Context

    private lateinit var fakeApi: FakeMovieApi
    private lateinit var fakeDao: FakeMovieDao
    private lateinit var fakeNetworkUtils: FakeNetworkUtils
    private lateinit var movieRepository: MovieRepositoryImpl
    private lateinit var homeViewModel: HomeViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this)
        
        // Setup mock context
        Mockito.`when`(mockContext.getSystemService(Context.CONNECTIVITY_SERVICE))
            .thenReturn(Mockito.mock(ConnectivityManager::class.java))
        
        Dispatchers.setMain(testDispatcher)
        
        fakeApi = FakeMovieApi()
        fakeDao = FakeMovieDao()
        fakeNetworkUtils = FakeNetworkUtils(mockContext)
        movieRepository = MovieRepositoryImpl(fakeApi, fakeDao, fakeNetworkUtils)
        homeViewModel = HomeViewModel(movieRepository)
    }

    @Before
    fun setupTestData() = runTest {
        // Initialize test data
        val testMovies = listOf(
            Movie(
                id = 1,
                title = "Test Movie 1",
                overview = "Test Overview 1",
                posterPath = "/test1.jpg",
                releaseDate = "2024-01-01",
                voteAverage = 8.5,
                isFavorite = true,
                genreIds = listOf(6,4,5,1)
            ),
            Movie(
                id = 2,
                title = "Test Movie 2",
                overview = "Test Overview 2",
                posterPath = "/test2.jpg",
                releaseDate = "2024-01-02",
                voteAverage = 7.5,
                isFavorite = false,
                genreIds = listOf(6,4,5,1)
            )
        )
        
        // Insert test data into the fake DAO
        fakeDao.insertMovies(testMovies)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getPopularMovies returns paging data`() = runTest {
        // When
        val result = homeViewModel.moviesList.first()

        // Then
        assertTrue(result is PagingData<*>)
    }

    @Test
    fun `getFavoriteMovies returns list of movies`() = runTest {
        // When
        val result = homeViewModel.getFavoriteMovies().first()

        // Then
        assertTrue(result is List<*>)
        assertTrue(result.isNotEmpty())
        assertTrue(result.all { it is Movie })
    }

    @Test
    fun `toggleFavorite updates movie`() = runTest {
        // Given
        val movie = Movie(
            id = 1,
            title = "Test Movie",
            overview = "Test Overview",
            posterPath = "/test.jpg",
            releaseDate = "2024-01-01",
            voteAverage = 8.5,
            isFavorite = true,
            genreIds = listOf(6,4,5,1)
        )

        // When
        homeViewModel.toggleFavorite(movie)

        // Then
        val updatedMovie = fakeDao.getMovieById(1)
        assertTrue(updatedMovie?.isFavorite == true)
    }
} 