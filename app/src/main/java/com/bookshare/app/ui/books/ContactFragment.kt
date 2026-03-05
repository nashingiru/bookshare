package com.bookshare.app.ui.books

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bookshare.app.databinding.FragmentContactBinding
import com.bookshare.app.utils.showSnackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContactFragment : Fragment() {

    private var _binding: FragmentContactBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSendRequest.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val message = binding.etMessage.text.toString().trim()

            if (name.isBlank() || message.isBlank()) {
                binding.root.showSnackbar("Por favor completa todos los campos")
                return@setOnClickListener
            }
            // Simulate sending
            binding.root.showSnackbar("✅ Solicitud de intercambio enviada a $name")
            findNavController().navigateUp()
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
