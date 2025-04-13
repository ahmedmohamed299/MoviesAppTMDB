package com.android.movieappmazaady.data.repository

import androidx.paging.PagingData
import com.android.movieappmazaady.data.model.Movie
import com.android.movieappmazaady.util.ErrorType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flowOf

class FakeMovieRepository : MovieRepository {
    private val favoriteStatusFlow = MutableSharedFlow<Int>()
    private val errorFlow = MutableSharedFlow<ErrorType>()
    
    private val movies = mutableListOf(
        Movie(
            id = 1,
            title = "Test Movie 1",
            overview = "Test Overview 1",
            posterPath = "/test1.jpg",
            releaseDate = "2024-01-01",
            voteAverage = 8.5,
            isFavorite = false,
            genreIds = listOf(6,4,5,1)
        ),
        Movie(
            id = 2,
            title = "Test Movie 2",
            overview = "Test Overview 2",
            posterPath = "/test2.jpg",
            releaseDate = "2024-01-02",
            voteAverage = 7.5,
            isFavorite = true,
            genreIds = listOf(6,4,5,1)
        )
    )

    override fun getPopularMovies(): Flow<PagingData<Movie>> {
        return flowOf(PagingData.from(movies))
    }

    override suspend fun getMovieDetails(movieId: Int): Movie {
        TODO("Not yet implemented")
    }

    override suspend fun toggleFavorite(movie: Movie) {
        val movie = movies.find { it.id == movie.id }
        movie?.let {
            val updatedMovie = it.copy(isFavorite = !it.isFavorite)
            val index = movies.indexOf(it)
            movies[index] = updatedMovie
            favoriteStatusFlow.emit(movie.id)
        }
    }

    override fun getFavoriteMovies(): Flow<List<Movie>> {
        return flowOf(movies.filter { it.isFavorite })
    }

    override suspend fun refreshMovies() {
        TODO("Not yet implemented")
    }



    override val favoriteStatusChanged: SharedFlow<Int> = favoriteStatusFlow
    override val error: SharedFlow<ErrorType> = errorFlow
} 