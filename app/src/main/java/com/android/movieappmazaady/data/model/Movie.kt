package com.android.movieappmazaady.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.android.movieappmazaady.data.local.Converters
import com.google.gson.annotations.SerializedName

@Entity(tableName = "movies")
@TypeConverters(Converters::class)
data class Movie(
    @PrimaryKey
    val id: Int,
    val title: String,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("release_date")
    val releaseDate: String,
    val overview: String,
    @SerializedName("vote_average")
    val voteAverage: Double,
    @SerializedName("genre_ids")
    val genreIds: List<Int>,
    var isFavorite: Boolean = false,
    val runtime: Int = 0
) 