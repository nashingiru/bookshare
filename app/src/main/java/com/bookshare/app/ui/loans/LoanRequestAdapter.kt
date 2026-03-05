package com.bookshare.app.ui.loans

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bookshare.app.R
import com.bookshare.app.databinding.ItemLoanRequestBinding
import com.bookshare.app.model.LoanRequest
import com.bookshare.app.model.LoanStatus
import com.bumptech.glide.Glide

class LoanRequestAdapter(
    private val isOwnerView: Boolean, // true = veo solicitudes recibidas, false = veo mis solicitudes
    private val onAccept: ((LoanRequest) -> Unit)? = null,
    private val onReject: ((LoanRequest) -> Unit)? = null,
    private val onMarkReturned: ((LoanRequest) -> Unit)? = null
) : ListAdapter<LoanRequest, LoanRequestAdapter.LoanViewHolder>(DiffCallback()) {

    inner class LoanViewHolder(private val binding: ItemLoanRequestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(request: LoanRequest) {
            binding.apply {
                tvBookTitle.text = request.bookTitle
                tvMessage.text = "\"${request.message}\""

                if (isOwnerView) {
                    tvPersonLabel.text = "Solicitado por:"
                    tvPersonName.text = request.requesterName
                } else {
                    tvPersonLabel.text = "Dueño del libro:"
                    tvPersonName.text = request.ownerName
                }

                Glide.with(root.context)
                    .load(request.bookCoverUrl)
                    .placeholder(R.drawable.ic_book_placeholder)
                    .error(R.drawable.ic_book_placeholder)
                    .centerCrop()
                    .into(ivBookCover)

                // Estado visual
                val statusEnum = LoanStatus.values().find { it.value == request.status }
                    ?: LoanStatus.PENDING
                tvStatus.text = statusEnum.displayName
                val (bgColor, textColor) = when (statusEnum) {
                    LoanStatus.PENDING  -> R.color.status_pending_bg to R.color.status_pending_text
                    LoanStatus.ACCEPTED -> R.color.status_accepted_bg to R.color.status_accepted_text
                    LoanStatus.REJECTED -> R.color.status_rejected_bg to R.color.status_rejected_text
                    LoanStatus.RETURNED -> R.color.status_returned_bg to R.color.status_returned_text
                }
                tvStatus.setBackgroundColor(ContextCompat.getColor(root.context, bgColor))
                tvStatus.setTextColor(ContextCompat.getColor(root.context, textColor))

                // Botones: solo visibles para el dueño y si está pendiente
                val isPending = request.status == LoanStatus.PENDING.value
                val isAccepted = request.status == LoanStatus.ACCEPTED.value

                btnAccept.visibility = if (isOwnerView && isPending) View.VISIBLE else View.GONE
                btnReject.visibility = if (isOwnerView && isPending) View.VISIBLE else View.GONE
                btnMarkReturned.visibility = if (isOwnerView && isAccepted) View.VISIBLE else View.GONE

                btnAccept.setOnClickListener { onAccept?.invoke(request) }
                btnReject.setOnClickListener { onReject?.invoke(request) }
                btnMarkReturned.setOnClickListener { onMarkReturned?.invoke(request) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoanViewHolder {
        val binding = ItemLoanRequestBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LoanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LoanViewHolder, position: Int) =
        holder.bind(getItem(position))

    class DiffCallback : DiffUtil.ItemCallback<LoanRequest>() {
        override fun areItemsTheSame(a: LoanRequest, b: LoanRequest) = a.id == b.id
        override fun areContentsTheSame(a: LoanRequest, b: LoanRequest) = a == b
    }
}
