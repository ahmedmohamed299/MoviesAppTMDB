package com.android.movieappmazaady.presentation.home

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.movieappmazaady.R
import com.android.movieappmazaady.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var movieAdapter: MovieAdapter
    private var isGridLayout = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Restore layout state from saved instance state
        isGridLayout = savedInstanceState?.getBoolean(KEY_IS_GRID_LAYOUT, true) ?: true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupLayoutToggle()
        setupSwipeRefresh()
        setupErrorHandling()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        movieAdapter = MovieAdapter(
            onMovieClick = { movie ->
                val action = HomeFragmentDirections.actionHomeToDetails(movie.id)
                findNavController().navigate(action)
            },
            onFavoriteClick = { movie ->
                viewModel.toggleFavorite(movie)
            }
        )

        binding.rvMovies.adapter = movieAdapter
        setLayoutManager(isGridLayout)
    }

    private fun setupLayoutToggle() {
        binding.fabLayout.setOnClickListener {
            isGridLayout = !isGridLayout
            setLayoutManager(isGridLayout)
            binding.fabLayout.setImageResource(if (isGridLayout) R.drawable.ic_grid else R.drawable.ic_list)
        }
    }

    private fun setLayoutManager(isGrid: Boolean) {
        val currentLayoutManager = binding.rvMovies.layoutManager
        val firstVisibleItemPosition = (currentLayoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition() ?: 0
        val firstVisibleView = currentLayoutManager?.findViewByPosition(firstVisibleItemPosition)
        val offset = firstVisibleView?.top ?: 0

        binding.rvMovies.layoutManager = if (isGrid) {
            LinearLayoutManager(requireContext())
        } else {
            GridLayoutManager(requireContext(), 2)
        }

        // Set the scroll position after the layout manager is set
        binding.rvMovies.post {
            (binding.rvMovies.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(firstVisibleItemPosition, offset)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_IS_GRID_LAYOUT, isGridLayout)
    }

    companion object {
        private const val KEY_IS_GRID_LAYOUT = "is_grid_layout"
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            if (isNetworkAvailable()) {
                viewModel.refreshMovies()
                viewModel.getMovies()
            } else {
                Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show()
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun setupErrorHandling() {
        binding.errorView.setOnRetryClickListener {
            viewModel.refreshMovies()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.moviesList.collectLatest { pagingData ->
                val currentPosition = (binding.rvMovies.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition() ?: 0
                val currentView = binding.rvMovies.layoutManager?.findViewByPosition(currentPosition)
                val currentOffset = currentView?.top ?: 0

                movieAdapter.submitData(pagingData ?: PagingData.empty())

                // Restore scroll position after data is submitted
                binding.rvMovies.post {
                    (binding.rvMovies.layoutManager as? LinearLayoutManager)?.let { layoutManager ->
                        layoutManager.scrollToPositionWithOffset(currentPosition, currentOffset)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isRefreshing.collectLatest { isRefreshing ->
                binding.swipeRefreshLayout.isRefreshing = isRefreshing
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collectLatest { error ->
                error?.let {
                    binding.errorView.apply {
                        isVisible = true
                        showError(it)
                    }
                    binding.rvMovies.isVisible = false
                } ?: run {
                    binding.errorView.isVisible = false
                    binding.rvMovies.isVisible = true
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            movieAdapter.loadStateFlow.collectLatest { loadState ->
                binding.progressBar.isVisible = loadState.refresh is LoadState.Loading
                
                val errorState = loadState.refresh as? LoadState.Error
                    ?: loadState.append as? LoadState.Error
                    ?: loadState.prepend as? LoadState.Error
                
                errorState?.let {
                    viewModel.handlePagingError(it.error)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 