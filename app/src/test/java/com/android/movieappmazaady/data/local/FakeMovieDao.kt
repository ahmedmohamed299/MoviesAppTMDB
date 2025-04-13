package com.android.movieappmazaady.data.local

import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.movieappmazaady.data.model.Movie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Dao
class FakeMovieDao : MovieDao {
    private val movies = mutableListOf<Movie>()

    override fun getAllMovies(): PagingSource<Int, Movie> {
        return object : PagingSource<Int, Movie>() {
            override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
                val page = params.key ?: 1
                val pageSize = params.loadSize
                val start = (page - 1) * pageSize
                val end = minOf(start + pageSize, movies.size)
                
                return LoadResult.Page(
                    data = movies.subList(start, end),
                    prevKey = if (page > 1) page - 1 else null,
                    nextKey = if (end < movies.size) page + 1 else null
                )
            }

            override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
                return state.anchorPosition?.let { anchorPosition ->
                    val anchorPage = state.closestPageToPosition(anchorPosition)
                    anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
                }
            }
        }
    }

    override fun getFavoriteMovies(): Flow<List<Movie>> {
        return flowOf(movies.filter { it.isFavorite })
    }

    override suspend fun insertMovies(movies: List<Movie>) {
        this.movies.addAll(movies)
    }

    override suspend fun updateMovie(movie: Movie) {
        val index = movies.indexOfFirst { it.id == movie.id }
        if (index != -1) {
            movies[index] = movie
        }
    }

    override suspend fun getMovieById(movieId: Int): Movie? {
        return movies.find { it.id == movieId }
    }

    override suspend fun getMoviesByIds(movieIds: List<Int>): List<Movie> {
        return movies.filter { it.id in movieIds }
    }

    override suspend fun getMoviesByPage(page: Int, pageSize: Int): List<Movie> {
        val start = (page - 1) * pageSize
        val end = minOf(start + pageSize, movies.size)
        return movies.subList(start, end)
    }

    override suspend fun updateFavoriteStatus(movieId: Int, isFavorite: Boolean) {
        val movie = movies.find { it.id == movieId }
        movie?.let {
            val updatedMovie = it.copy(isFavorite = isFavorite)
            val index = movies.indexOf(it)
            movies[index] = updatedMovie
        }
    }
} 