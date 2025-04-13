package com.android.movieappmazaady.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.android.movieappmazaady.data.model.Movie
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Query("SELECT * FROM movies")
    fun getAllMovies(): PagingSource<Int, Movie>

    @Query("SELECT * FROM movies WHERE isFavorite = 1")
    fun getFavoriteMovies(): Flow<List<Movie>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<Movie>)

    @Update
    suspend fun updateMovie(movie: Movie)

    @Query("SELECT * FROM movies WHERE id = :movieId")
    suspend fun getMovieById(movieId: Int): Movie?

    @Query("SELECT * FROM movies WHERE id IN (:movieIds)")
    suspend fun getMoviesByIds(movieIds: List<Int>): List<Movie>

    @Query("SELECT * FROM movies LIMIT :pageSize OFFSET (:page - 1) * :pageSize")
    suspend fun getMoviesByPage(page: Int, pageSize: Int): List<Movie>

    @Query("UPDATE movies SET isFavorite = :isFavorite WHERE id = :movieId")
    suspend fun updateFavoriteStatus(movieId: Int, isFavorite: Boolean)

    @Transaction
    suspend fun insertOrUpdateMovies(newMovies: List<Movie>) {
        // Get existing movies that are favorites
        val existingMovieIds = newMovies.map { it.id }
        val existingMovies = getMoviesByIds(existingMovieIds)
        val favoriteMovieIds = existingMovies.filter { it.isFavorite }.map { it.id }

        // Insert all new movies
        insertMovies(newMovies)

        // Restore favorite status for previously favorited movies
        favoriteMovieIds.forEach { movieId ->
            updateFavoriteStatus(movieId, true)
        }
    }
} 