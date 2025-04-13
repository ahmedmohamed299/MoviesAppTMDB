package com.android.movieappmazaady.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.movieappmazaady.R
import com.android.movieappmazaady.data.model.Movie
import com.android.movieappmazaady.databinding.ItemMovieBinding
import com.bumptech.glide.Glide

class MovieAdapter(
    private val onMovieClick: (Movie) -> Unit,
    private val onFavoriteClick: (Movie) -> Unit
) : PagingDataAdapter<Movie, MovieAdapter.MovieViewHolder>(MovieDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemMovieBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        getItem(position)?.let { movie ->
            holder.bind(movie)
        }
    }

    inner class MovieViewHolder(
        private val binding: ItemMovieBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: Movie) {
            binding.apply {
                tvTitle.text = movie.title
                tvReleaseDate.text = movie.releaseDate

                Glide.with(root.context)
                    .load("https://image.tmdb.org/t/p/w500${movie.posterPath}")
                    .placeholder(R.drawable.ic_movie_placeholder)
                    .error(R.drawable.ic_movie_placeholder)
                    .into(ivPoster)

                // Set the initial favorite icon state
                updateFavoriteIcon(movie.isFavorite)

                root.setOnClickListener {
                    onMovieClick(movie)
                }

                ivFavorite.setOnClickListener {
                    // Update the UI immediately
                    val newFavoriteState = !movie.isFavorite
                    updateFavoriteIcon(newFavoriteState)
                    
                    // Create a new movie instance with updated favorite status
                    val updatedMovie = movie.copy(isFavorite = newFavoriteState)
                    
                    // Notify the repository to update the database
                    onFavoriteClick(movie)
                }
            }
        }

        private fun updateFavoriteIcon(isFavorite: Boolean) {
            binding.ivFavorite.setImageResource(
                if (isFavorite) R.drawable.ic_favorite
                else R.drawable.ic_favorite_border
            )
        }
    }

    class MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem.id == newItem.id && 
                   oldItem.isFavorite == newItem.isFavorite &&
                   oldItem.title == newItem.title &&
                   oldItem.releaseDate == newItem.releaseDate
        }
    }
} 