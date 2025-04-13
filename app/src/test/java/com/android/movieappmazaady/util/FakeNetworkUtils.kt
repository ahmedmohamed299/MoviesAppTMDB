package com.android.movieappmazaady.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class FakeNetworkUtils @Inject constructor(
    @ApplicationContext private val context: Context
) : NetworkUtils(context) {
    private var isNetworkAvailable = true

    fun setNetworkAvailable(available: Boolean) {
        isNetworkAvailable = available
    }

    override fun isNetworkAvailable(): Boolean {
        return isNetworkAvailable
    }
} 