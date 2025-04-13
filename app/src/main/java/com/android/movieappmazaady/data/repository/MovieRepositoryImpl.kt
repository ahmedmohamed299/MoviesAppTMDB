package com.android.movieappmazaady.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.android.movieappmazaady.data.local.MovieDao
import com.android.movieappmazaady.data.model.Movie
import com.android.movieappmazaady.data.remote.MovieApi
import com.android.movieappmazaady.data.remote.MoviePagingSource
import com.android.movieappmazaady.util.ErrorType
import com.android.movieappmazaady.util.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class MovieRepositoryImpl @Inject constructor(
    private val api: MovieApi,
    private val dao: MovieDao,
    private val networkUtils: NetworkUtils
) : MovieRepository {

    private val _favoriteStatusChanged = MutableSharedFlow<Int>()
    override val favoriteStatusChanged: SharedFlow<Int> = _favoriteStatusChanged.asSharedFlow()

    private val _error = MutableSharedFlow<ErrorType>()
    override val error: SharedFlow<ErrorType> = _error.asSharedFlow()

    override fun getPopularMovies(): Flow<PagingData<Movie>> {
        if (!networkUtils.isNetworkAvailable()) {
            CoroutineScope(Dispatchers.IO).launch {
                _error.emit(ErrorType.NoInternet)
            }
        }
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 20
            ),
            pagingSourceFactory = {
                MoviePagingSource(api, dao, networkUtils)
            }
        ).flow
    }

    override suspend fun getMovieDetails(movieId: Int): Movie {
        try {
            return dao.getMovieById(movieId) ?: throw Exception("Movie not found")
        } catch (e: Exception) {
            _error.emit(ErrorType.UnknownError(e.message ?: "Unknown error occurred"))
            throw e
        }
    }

    override suspend fun toggleFavorite(movie: Movie) {
        try {
            // Update the favorite status in the database
            dao.updateFavoriteStatus(movie.id, !movie.isFavorite)
            
            // Emit the change notification
            _favoriteStatusChanged.emit(movie.id)
            
            if (networkUtils.isNetworkAvailable()) {
                // Immediately refresh the affected movie to ensure consistency
                val response = api.getPopularMovies(1, MoviePagingSource.API_KEY)
                val updatedMovies = response.results.map { apiMovie ->
                    if (apiMovie.id == movie.id) {
                        apiMovie.copy(isFavorite = !movie.isFavorite)
                    } else {
                        apiMovie
                    }
                }
                dao.insertOrUpdateMovies(updatedMovies)
            }
        } catch (e: Exception) {
            // If there's an error, try to at least update the local database
            dao.updateFavoriteStatus(movie.id, !movie.isFavorite)
            _favoriteStatusChanged.emit(movie.id)
            _error.emit(ErrorType.UnknownError(e.message ?: "Error updating favorite status"))
        }
    }

    override fun getFavoriteMovies(): Flow<List<Movie>> {
        return dao.getFavoriteMovies().map { movies ->
            if (movies.isEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    _error.emit(ErrorType.EmptyData)
                }
            }
            movies
        }
    }

    override suspend fun refreshMovies() {
        try {
            if (!networkUtils.isNetworkAvailable()) {
                _error.emit(ErrorType.NoInternet)
                return
            }
            
            // Load first page from API
            val response = api.getPopularMovies(1, MoviePagingSource.API_KEY)
            if (response.results.isEmpty()) {
                _error.emit(ErrorType.EmptyData)
                return
            }
            
            // Update database while preserving favorites
            dao.insertOrUpdateMovies(response.results)
        } catch (e: Exception) {
            _error.emit(ErrorType.UnknownError(e.message ?: "Error refreshing movies"))
            throw e
        }
    }
} 