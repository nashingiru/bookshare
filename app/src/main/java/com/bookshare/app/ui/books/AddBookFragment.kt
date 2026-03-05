package com.bookshare.app.ui.books

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bookshare.app.databinding.FragmentAddBookBinding
import com.bookshare.app.model.Book
import com.bookshare.app.model.BookGenre
import com.bookshare.app.model.Resource
import com.bookshare.app.utils.showSnackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

@AndroidEntryPoint
class AddBookFragment : Fragment() {

    private var _binding: FragmentAddBookBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BooksViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddBookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupGenreSpinner()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupGenreSpinner() {
        val genres = BookGenre.values().map { it.displayName }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, genres)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerGenre.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val author = binding.etAuthor.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            val isbn = binding.etIsbn.text.toString().trim()
            val genre = binding.spinnerGenre.selectedItem.toString()
            val isAvailable = binding.switchAvailable.isChecked

            if (title.isBlank() || author.isBlank()) {
                binding.root.showSnackbar("El título y autor son obligatorios")
                return@setOnClickListener
            }

            val book = Book(
                id = UUID.randomUUID().toString(),
                title = title,
                author = author,
                description = description,
                genre = genre,
                isbn = isbn,
                isAvailable = isAvailable,
                coverUrl = generateCoverUrl(isbn)
            )
            viewModel.addBook(book)
        }

        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSearchApi.setOnClickListener {
            val query = binding.etTitle.text.toString().trim()
            if (query.isNotBlank()) {
                viewModel.searchBooksFromApi(query)
            }
        }
    }

    private fun generateCoverUrl(isbn: String): String {
        return if (isbn.isNotBlank()) "https://covers.openlibrary.org/b/isbn/$isbn-M.jpg" else ""
    }

    private fun observeViewModel() {
        viewModel.addBookState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSave.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.root.showSnackbar("📚 Libro agregado exitosamente")
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    binding.root.showSnackbar(resource.message)
                }
            }
        }

        viewModel.apiSearchState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    resource.data.firstOrNull()?.let { book ->
                        binding.etTitle.setText(book.title)
                        binding.etAuthor.setText(book.author)
                        if (book.isbn.isNotBlank()) binding.etIsbn.setText(book.isbn)
                        binding.root.showSnackbar("Datos completados desde Open Library")
                    } ?: binding.root.showSnackbar("No se encontraron resultados")
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.root.showSnackbar("No se pudo buscar: ${resource.message}")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
