package com.android.movieappmazaady.data.paging

import android.content.Context
import androidx.paging.PagingSource
import com.android.movieappmazaady.data.local.FakeMovieDao
import com.android.movieappmazaady.data.remote.FakeMovieApi
import com.android.movieappmazaady.data.remote.MoviePagingSource
import com.android.movieappmazaady.util.FakeNetworkUtils
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertTrue

class MoviePagingSourceTest {
    @Mock
    private lateinit var mockContext: Context

    private lateinit var fakeApi: FakeMovieApi
    private lateinit var fakeDao: FakeMovieDao
    private lateinit var fakeNetworkUtils: FakeNetworkUtils
    private lateinit var moviePagingSource: MoviePagingSource

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        fakeApi = FakeMovieApi()
        fakeDao = FakeMovieDao()
        fakeNetworkUtils = FakeNetworkUtils(mockContext)
        moviePagingSource = MoviePagingSource(fakeApi, fakeDao, fakeNetworkUtils)
    }

    @Test
    fun `load returns success when network is available`() = runTest {
        // Given
        fakeNetworkUtils.setNetworkAvailable(true)

        // When
        val result = moviePagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = 1,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )

        // Then
        assertTrue(result is PagingSource.LoadResult.Page)
    }

    @Test
    fun `load returns error when network is unavailable`() = runTest {
        // Given
        fakeNetworkUtils.setNetworkAvailable(false)

        // When
        val result = moviePagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = 1,
                loadSize = 20,
                placeholdersEnabled = false
            )
        )

        // Then
        assertTrue(result is PagingSource.LoadResult.Error)
    }
} 
