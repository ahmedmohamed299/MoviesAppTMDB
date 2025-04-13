package com.android.movieappmazaady.data.remote

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.android.movieappmazaady.data.local.MovieDao
import com.android.movieappmazaady.data.model.Movie
import com.android.movieappmazaady.util.NetworkUtils
import retrofit2.HttpException
import java.io.IOException

class MoviePagingSource(
    private val api: MovieApi,
    private val dao: MovieDao,
    private val networkUtils: NetworkUtils
) : PagingSource<Int, Movie>() {

    companion object {
        const val API_KEY = "ad7b6dbe2cd8c3b637d59a7ac1677960"
        const val PAGE_SIZE = 20
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val page = params.key ?: 1
        
        return try {
            if (!networkUtils.isNetworkAvailable()) {
                // If no network, try to load from database
                val dbMovies = dao.getMoviesByPage(page, PAGE_SIZE)
                if (dbMovies.isNotEmpty()) {
                    return LoadResult.Page(
                        data = dbMovies,
                        prevKey = if (page == 1) null else page - 1,
                        nextKey = page + 1
                    )
                }
                throw IOException("No internet connection")
            }

            // Try to load from API
            val response = api.getPopularMovies(page, API_KEY)
            
            // Save new movies to database while preserving favorites
            dao.insertOrUpdateMovies(response.results)
            
            // Load the updated data from the database to get correct favorite status
            val dbMovies = dao.getMoviesByPage(page, PAGE_SIZE)
            
            LoadResult.Page(
                data = dbMovies,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.results.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            // If API call fails, try to load from database
            try {
                val dbMovies = dao.getMoviesByPage(page, PAGE_SIZE)
                if (dbMovies.isNotEmpty()) {
                    return LoadResult.Page(
                        data = dbMovies,
                        prevKey = if (page == 1) null else page - 1,
                        nextKey = page + 1
                    )
                }
                LoadResult.Error(e)
            } catch (dbError: Exception) {
                LoadResult.Error(dbError)
            }
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
} 