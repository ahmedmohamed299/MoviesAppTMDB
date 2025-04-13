package com.android.movieappmazaady.data.remote

import com.android.movieappmazaady.data.model.Movie


class FakeMovieApi : MovieApi {
    private val movies = listOf(
        Movie(
            id = 1,
            title = "Test Movie 1",
            overview = "Test Overview 1",
            posterPath = "/test1.jpg",
            releaseDate = "2024-01-01",
            voteAverage = 8.5,
            isFavorite = false,
            genreIds = listOf(6, 4, 5, 1)
        ),
        Movie(
            id = 2,
            title = "Test Movie 2",
            overview = "Test Overview 2",
            posterPath = "/test2.jpg",
            releaseDate = "2024-01-02",
            voteAverage = 7.5,
            isFavorite = true,
            genreIds = listOf(6, 4, 5, 1)
        )
    )

    override suspend fun getPopularMovies(page: Int, apiKey: String): MovieResponse {
        return MovieResponse(
            page = page,
            results = movies,
            total_pages = 1
        )
    }

    override suspend fun getMovieDetails(movieId: Int, apiKey: String): MovieDetailsResponse {
        return MovieDetailsResponse(
            id = 1,
            title = "",
            overview = "",
            runtime = 1,
            genres = listOf(Genre(1, "")),
            vote_average = 1.0
        )
    }
} 