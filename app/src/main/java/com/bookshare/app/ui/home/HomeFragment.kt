package com.bookshare.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookshare.app.R
import com.bookshare.app.databinding.FragmentHomeBinding
import com.bookshare.app.model.Resource
import com.bookshare.app.ui.books.BookAdapter
import com.bookshare.app.ui.books.BooksViewModel
import com.bookshare.app.utils.showSnackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BooksViewModel by viewModels()
    private lateinit var bookAdapter: BookAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchView()
        setupSwipeRefresh()
        observeViewModel()

        binding.fabAddBook.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addBookFragment)
        }
    }

    private fun setupRecyclerView() {
        bookAdapter = BookAdapter(
            onBookClick = { book ->
                viewModel.selectBook(book)
                val action = HomeFragmentDirections.actionHomeFragmentToBookDetailFragment(book.id)
                findNavController().navigate(action)
            }
        )
        binding.recyclerView.apply {
            adapter = bookAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.setSearchQuery(it) }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText ?: "")
                return true
            }
        })
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshBooks()
        }
    }

    private fun observeViewModel() {
        viewModel.searchResults.observe(viewLifecycleOwner) { books ->
            bookAdapter.submitList(books)
            binding.tvEmptyState.visibility = if (books.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.refreshState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> binding.swipeRefresh.isRefreshing = true
                is Resource.Success -> binding.swipeRefresh.isRefreshing = false
                is Resource.Error -> {
                    binding.swipeRefresh.isRefreshing = false
                    binding.root.showSnackbar(resource.message)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
