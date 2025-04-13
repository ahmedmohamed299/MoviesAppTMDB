package com.android.movieappmazaady.presentation.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.android.movieappmazaady.R
import com.android.movieappmazaady.data.model.Movie
import com.android.movieappmazaady.databinding.FragmentDetailsBinding
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DetailsFragment : Fragment() {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetailsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        observeMovieDetails()
        
        // Get the movie ID from the navigation arguments
        val movieId = arguments?.getInt("movieId") ?: return
        viewModel.getMovieDetails(movieId)
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            setNavigationIcon(R.drawable.ic_back)
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun observeMovieDetails() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.movieDetails.collectLatest { state ->
                when (state) {
                    is DetailsViewModel.UiState.Loading -> showLoading()
                    is DetailsViewModel.UiState.Success -> showMovieDetails(state.movie)
                    is DetailsViewModel.UiState.Error -> showError(state.message)
                }
            }
        }
    }

    private fun showLoading() {
        binding.apply {
            progressBar.isVisible = true
            tvError.isVisible = false
        }
    }

    private fun showError(message: String) {
        binding.apply {
            progressBar.isVisible = false
            tvError.isVisible = true
            tvError.text = message
        }
    }

    private fun showMovieDetails(movie: Movie) {
        binding.apply {
            progressBar.isVisible = false
            tvError.isVisible = false

            // Set movie poster with crossfade
            Glide.with(requireContext())
                .load("https://image.tmdb.org/t/p/w500${movie.posterPath}")
                .placeholder(R.drawable.ic_movie_placeholder)
                .error(R.drawable.ic_movie_placeholder)
                .into(ivMoviePoster)

            // Set collapsing toolbar title
            collapsingToolbar.title = movie.title

            // Set movie details
            tvTitle.text = movie.title
            tvReleaseDate.text = movie.releaseDate
            tvRating.text = "${movie.voteAverage}/10"
            tvOverview.text = movie.overview


            // Set favorite button
            updateFavoriteIcon(movie.isFavorite)
            ivFavorite.setOnClickListener {
                viewModel.toggleFavorite(movie)
            }
        }
    }




    private fun updateFavoriteIcon(isFavorite: Boolean) {
        binding.ivFavorite.setImageResource(
            if (isFavorite) R.drawable.ic_favorite
            else R.drawable.ic_favorite_border
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 