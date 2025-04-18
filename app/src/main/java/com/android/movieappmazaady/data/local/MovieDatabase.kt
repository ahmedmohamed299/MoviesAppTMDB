package com.android.movieappmazaady.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.android.movieappmazaady.data.model.Movie

@Database(
    entities = [Movie::class],
    version = 2,
    exportSchema = false
)
abstract class MovieDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
} 