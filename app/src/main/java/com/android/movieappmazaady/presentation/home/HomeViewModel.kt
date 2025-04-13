package com.android.movieappmazaady.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.android.movieappmazaady.data.model.Movie
import com.android.movieappmazaady.data.repository.MovieRepository
import com.android.movieappmazaady.util.ErrorType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.io.IOException
import retrofit2.HttpException

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _moviesList = MutableStateFlow<PagingData<Movie>?>(null)
    val moviesList: StateFlow<PagingData<Movie>?> = _moviesList.asStateFlow()

    private val _error = MutableStateFlow<ErrorType?>(null)
    val error: StateFlow<ErrorType?> = _error.asStateFlow()

    init {
        getMovies()
        observeFavoriteChanges()
        observeErrors()
    }

    private fun observeErrors() {
        viewModelScope.launch {
            repository.error.collect { error ->
                _error.value = error
            }
        }
    }

    private fun observeFavoriteChanges() {
        viewModelScope.launch {
            repository.favoriteStatusChanged.collect {
                _moviesList.value?.let {
                    getMovies()
                }
            }
        }
    }

    fun getMovies() {
        viewModelScope.launch {
            try {
                repository.getPopularMovies()
                    .cachedIn(viewModelScope)
                    .catch { e -> 
                        _error.value = ErrorType.UnknownError(e.message ?: "Unknown error occurred")
                    }
                    .collect {
                        _moviesList.value = it
                    }
            } catch (e: Exception) {
                _error.value = ErrorType.UnknownError(e.message ?: "Error loading movies")
            }
        }
    }

    fun toggleFavorite(movie: Movie) {
        viewModelScope.launch {
            try {
                repository.toggleFavorite(movie)
            } catch (e: Exception) {
                _error.value = ErrorType.UnknownError(e.message ?: "Error updating favorite status")
            }
        }
    }

    fun refreshMovies() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.refreshMovies()
                getMovies()
            } catch (e: Exception) {
                _error.value = ErrorType.UnknownError(e.message ?: "Error refreshing movies")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun getFavoriteMovies(): Flow<List<Movie>> {
        return repository.getFavoriteMovies()
    }

    fun clearError() {
        _error.value = null
    }

    fun handlePagingError(throwable: Throwable) {
        _error.value = when (throwable) {
            is IOException -> ErrorType.Network
            is HttpException -> ErrorType.ApiError(
                throwable.code(),
                throwable.message()
            )
            else -> ErrorType.UnknownError(throwable.message ?: "Unknown error occurred")
        }
    }
} 