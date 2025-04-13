package com.android.movieappmazaady.data.repository

import androidx.paging.PagingData
import com.android.movieappmazaady.data.model.Movie
import com.android.movieappmazaady.util.ErrorType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface MovieRepository {
    fun getPopularMovies(): Flow<PagingData<Movie>>
    suspend fun getMovieDetails(movieId: Int): Movie
    suspend fun toggleFavorite(movie: Movie)
    fun getFavoriteMovies(): Flow<List<Movie>>
    suspend fun refreshMovies()
    val favoriteStatusChanged: SharedFlow<Int>
    val error: SharedFlow<ErrorType>
} 