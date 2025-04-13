package com.android.movieappmazaady.data.repository

import androidx.paging.PagingData
import com.android.movieappmazaady.data.local.MovieDao
import com.android.movieappmazaady.data.model.Movie
import com.android.movieappmazaady.data.remote.MovieApi
import com.android.movieappmazaady.data.remote.MovieResponse
import com.android.movieappmazaady.util.NetworkUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import kotlin.test.assertTrue

class MovieRepositoryTest {

    @Mock
    private lateinit var movieApi: MovieApi

    @Mock
    private lateinit var movieDao: MovieDao

    @Mock
    private lateinit var networkUtils: NetworkUtils

    private lateinit var movieRepository: MovieRepositoryImpl

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        movieRepository = MovieRepositoryImpl(movieApi, movieDao, networkUtils)
    }

    @Test
    fun `getPopularMovies returns paging data when successful`() = runBlocking {
        // Given
        val movies = listOf(
            Movie(
                id = 1,
                title = "Test Movie",
                overview = "Test Overview",
                posterPath = "/test.jpg",
                releaseDate = "2024-01-01",
                voteAverage = 8.5,
                isFavorite = false,
                genreIds = listOf(6,4,5,1)
            )
        )
        val response = MovieResponse(
            page = 1,
            results = movies,
            total_pages = 10
        )

        `when`(networkUtils.isNetworkAvailable()).thenReturn(true)
        `when`(movieApi.getPopularMovies(1, "YOUR_API_KEY")).thenReturn(response)

        // When
        val result = movieRepository.getPopularMovies().first()

        // Then
        assert(result is PagingData<*>)
    }

    @Test
    fun `getFavoriteMovies returns paging data`() = runBlocking {
        // Given
        val movies = listOf(
            Movie(
                id = 1,
                title = "Test Movie",
                overview = "Test Overview",
                posterPath = "/test.jpg",
                releaseDate = "2024-01-01",
                voteAverage = 8.5,
                isFavorite = true,
                genreIds = listOf(6,4,5,1)
            )
        )

        `when`(movieDao.getFavoriteMovies()).thenReturn(flowOf(movies))

        // When
        val result = movieRepository.getFavoriteMovies().first()

        // Then
        assertTrue (result[0].isFavorite)
    }
} 