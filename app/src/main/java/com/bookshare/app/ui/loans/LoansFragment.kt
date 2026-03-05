package com.bookshare.app.ui.loans

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bookshare.app.databinding.FragmentLoansBinding
import com.bookshare.app.model.Resource
import com.bookshare.app.utils.showSnackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoansFragment : Fragment() {

    private var _binding: FragmentLoansBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoansViewModel by viewModels()

    private lateinit var receivedAdapter: LoanRequestAdapter
    private lateinit var sentAdapter: LoanRequestAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoansBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        setupTabs()
        observeViewModel()
    }

    private fun setupAdapters() {
        receivedAdapter = LoanRequestAdapter(
            isOwnerView = true,
            onAccept = { request ->
                viewModel.acceptRequest(request)
            },
            onReject = { request ->
                viewModel.rejectRequest(request)
            },
            onMarkReturned = { request ->
                viewModel.markAsReturned(request)
            }
        )

        sentAdapter = LoanRequestAdapter(isOwnerView = false)

        binding.rvReceived.apply {
            adapter = receivedAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        binding.rvSent.apply {
            adapter = sentAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupTabs() {
        // Mostrar Recibidas por defecto
        showReceivedTab()

        binding.btnTabReceived.setOnClickListener { showReceivedTab() }
        binding.btnTabSent.setOnClickListener { showSentTab() }
    }

    private fun showReceivedTab() {
        binding.rvReceived.visibility = View.VISIBLE
        binding.rvSent.visibility = View.GONE
        binding.tvEmptySent.visibility = View.GONE
        binding.btnTabReceived.isSelected = true
        binding.btnTabSent.isSelected = false
        checkEmptyReceived()
    }

    private fun showSentTab() {
        binding.rvReceived.visibility = View.GONE
        binding.rvSent.visibility = View.VISIBLE
        binding.tvEmptyReceived.visibility = View.GONE
        binding.btnTabSent.isSelected = true
        binding.btnTabReceived.isSelected = false
        checkEmptySent()
    }

    private fun checkEmptyReceived() {
        val isEmpty = receivedAdapter.currentList.isEmpty()
        binding.tvEmptyReceived.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    private fun checkEmptySent() {
        val isEmpty = sentAdapter.currentList.isEmpty()
        binding.tvEmptySent.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    private fun observeViewModel() {
        viewModel.receivedRequests.observe(viewLifecycleOwner) { requests ->
            receivedAdapter.submitList(requests)
            if (binding.rvReceived.visibility == View.VISIBLE) checkEmptyReceived()
            // Badge en el tab
            val pending = requests.count { it.status == "pending" }
            binding.btnTabReceived.text = if (pending > 0)
                "Recibidas ($pending)" else "Recibidas"
        }

        viewModel.myRequests.observe(viewLifecycleOwner) { requests ->
            sentAdapter.submitList(requests)
            if (binding.rvSent.visibility == View.VISIBLE) checkEmptySent()
        }

        viewModel.actionState.observe(viewLifecycleOwner) { resource ->
            resource?.let {
                when (it) {
                    is Resource.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.root.showSnackbar("✅ Acción realizada correctamente")
                        viewModel.syncRequests()
                        viewModel.resetActionState()
                    }
                    is Resource.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.root.showSnackbar("Error: ${it.message}")
                        viewModel.resetActionState()
                    }
                    is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
