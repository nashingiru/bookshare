package com.bookshare.app.ui.books

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bookshare.app.R
import com.bookshare.app.databinding.FragmentBookDetailBinding
import com.bookshare.app.model.Book
import com.bookshare.app.model.Resource
import com.bookshare.app.utils.showSnackbar
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookDetailFragment : Fragment() {

    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BooksViewModel by viewModels()
    private val args: BookDetailFragmentArgs by navArgs()
    private var currentBook: Book? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBookDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setupClickListeners()
    }

    private fun observeViewModel() {
        viewModel.selectedBook.observe(viewLifecycleOwner) { book ->
            book?.let {
                currentBook = it
                bindBookData(it)
            }
        }

        viewModel.deleteBookState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    binding.root.showSnackbar("Libro eliminado")
                    findNavController().navigateUp()
                }
                is Resource.Error -> binding.root.showSnackbar(resource.message)
                else -> {}
            }
        }
    }

    private fun bindBookData(book: Book) {
        binding.apply {
            tvTitle.text = book.title
            tvAuthor.text = "por ${book.author}"
            tvDescription.text = book.description.ifBlank { "Sin descripción disponible." }
            tvGenre.text = book.genre
            tvOwner.text = "Compartido por: ${book.ownerName}"
            tvIsbn.text = if (book.isbn.isNotBlank()) "ISBN: ${book.isbn}" else ""
            tvPages.text = if (book.pageCount > 0) "${book.pageCount} páginas" else ""

            val availText = if (book.isAvailable) "✅ Disponible para intercambio" else "❌ No disponible"
            tvAvailability.text = availText

            Glide.with(requireContext())
                .load(book.coverUrl.ifEmpty { null })
                .placeholder(R.drawable.ic_book_placeholder)
                .error(R.drawable.ic_book_placeholder)
                .fallback(R.drawable.ic_book_placeholder)
                .into(ivCover)

            // Show delete button only for owner
            val isOwner = book.ownerUid == viewModel.getCurrentUserId()
            btnDelete.visibility = if (isOwner) View.VISIBLE else View.GONE
        }
    }

    private fun setupClickListeners() {
        binding.btnDelete.setOnClickListener {
            currentBook?.let { book ->
                viewModel.deleteBook(book.id)
            }
        }

        binding.btnContact.setOnClickListener {
            findNavController().navigate(R.id.action_bookDetailFragment_to_contactFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
