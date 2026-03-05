package com.bookshare.app.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookshare.app.R
import com.bookshare.app.databinding.FragmentProfileBinding
import com.bookshare.app.ui.books.BookAdapter
import com.bookshare.app.ui.books.BooksViewModel
import com.bookshare.app.ui.auth.AuthViewModel
import com.bookshare.app.utils.showSnackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val booksViewModel: BooksViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var myBooksAdapter: BookAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        myBooksAdapter = BookAdapter(
            onBookClick = { book ->
                booksViewModel.selectBook(book)
                findNavController().navigate(R.id.action_profileFragment_to_bookDetailFragment)
            },
            onDeleteClick = { book ->
                booksViewModel.deleteBook(book.id)
            },
            currentUserId = booksViewModel.getCurrentUserId()
        )
        binding.rvMyBooks.apply {
            adapter = myBooksAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {
        booksViewModel.myBooks.observe(viewLifecycleOwner) { books ->
            myBooksAdapter.submitList(books)
            binding.tvBooksCount.text = "${books.size} libros compartidos"
            binding.tvEmptyBooks.visibility = if (books.isEmpty()) View.VISIBLE else View.GONE
        }

        booksViewModel.deleteBookState.observe(viewLifecycleOwner) { resource ->
            // Handle delete
        }
    }

    private fun setupClickListeners() {
        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }

        binding.btnAddBook.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_addBookFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
