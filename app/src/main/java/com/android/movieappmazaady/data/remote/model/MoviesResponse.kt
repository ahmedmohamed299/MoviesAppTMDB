package com.android.movieappmazaady.data.remote.model

import com.android.movieappmazaady.data.model.Movie
import com.google.gson.annotations.SerializedName

data class MoviesResponse(
    val results: List<Movie>,
    val page: Int,
    val total_pages: Int
) 