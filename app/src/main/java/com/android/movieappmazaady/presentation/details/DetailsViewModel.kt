package com.android.movieappmazaady.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.movieappmazaady.data.model.Movie
import com.android.movieappmazaady.data.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    private val _movieDetails = MutableStateFlow<UiState>(UiState.Loading)
    val movieDetails: StateFlow<UiState> = _movieDetails.asStateFlow()

    fun getMovieDetails(movieId: Int) {
        viewModelScope.launch {
            _movieDetails.value = UiState.Loading
            try {
                val movie = repository.getMovieDetails(movieId)
                _movieDetails.value = UiState.Success(movie)
            } catch (e: Exception) {
                _movieDetails.value = UiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun toggleFavorite(movie: Movie) {
        viewModelScope.launch {
            try {
                repository.toggleFavorite(movie)
                // Refresh the movie details to update the UI
                getMovieDetails(movie.id)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    sealed class UiState {
        data object Loading : UiState()
        data class Success(val movie: Movie) : UiState()
        data class Error(val message: String) : UiState()
    }
} 