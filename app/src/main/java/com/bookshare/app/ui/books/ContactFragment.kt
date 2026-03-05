package com.bookshare.app.ui.books

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bookshare.app.databinding.FragmentContactBinding
import com.bookshare.app.model.Book
import com.bookshare.app.model.Resource
import com.bookshare.app.ui.loans.LoansViewModel
import com.bookshare.app.utils.showSnackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContactFragment : Fragment() {

    private var _binding: FragmentContactBinding? = null
    private val binding get() = _binding!!
    
    private val loansViewModel: LoansViewModel by viewModels()
    private val booksViewModel: BooksViewModel by viewModels()
    private val args: ContactFragmentArgs by navArgs()
    
    private var targetBook: Book? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load the book to have the full data for the request
        booksViewModel.loadBook(args.bookId)
        
        observeViewModels()
        setupClickListeners()
    }

    private fun observeViewModels() {
        booksViewModel.selectedBook.observe(viewLifecycleOwner) { book ->
            targetBook = book
            book?.let {
                binding.tilMessage.hint = "Mensaje para ${it.ownerName}..."
            }
        }

        loansViewModel.sendState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.btnSendRequest.isEnabled = false
                    binding.btnSendRequest.text = "Enviando..."
                }
                is Resource.Success -> {
                    binding.root.showSnackbar("✅ Solicitud enviada correctamente")
                    loansViewModel.resetSendState()
                    findNavController().popBackStack()
                }
                is Resource.Error -> {
                    binding.btnSendRequest.isEnabled = true
                    binding.btnSendRequest.text = "Enviar Solicitud"
                    binding.root.showSnackbar(resource.message)
                }
                else -> {}
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSendRequest.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()

            if (message.isBlank()) {
                binding.root.showSnackbar("Por favor escribe un mensaje")
                return@setOnClickListener
            }

            targetBook?.let { book ->
                loansViewModel.sendRequest(book, message)
            } ?: run {
                binding.root.showSnackbar("Error: No se pudo cargar la información del libro")
            }
        }

        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
