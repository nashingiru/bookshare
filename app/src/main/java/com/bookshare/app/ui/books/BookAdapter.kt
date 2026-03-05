package com.bookshare.app.ui.books

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bookshare.app.R
import com.bookshare.app.databinding.ItemBookBinding
import com.bookshare.app.model.Book
import com.bumptech.glide.Glide

class BookAdapter(
    private val onBookClick: (Book) -> Unit,
    private val onDeleteClick: ((Book) -> Unit)? = null,
    private val currentUserId: String? = null
) : ListAdapter<Book, BookAdapter.BookViewHolder>(BookDiffCallback()) {

    inner class BookViewHolder(private val binding: ItemBookBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(book: Book) {
            binding.apply {
                tvTitle.text = book.title
                tvAuthor.text = book.author
                tvOwner.text = "Compartido por: ${book.ownerName}"
                tvGenre.text = book.genre

                val availText = if (book.isAvailable) "Disponible" else "No disponible"
                tvAvailability.text = availText
                tvAvailability.setTextColor(
                    root.context.getColor(
                        if (book.isAvailable) R.color.green_available else R.color.red_unavailable
                    )
                )

                Glide.with(root.context)
                    .load(book.coverUrl.ifEmpty { null })
                    .placeholder(R.drawable.ic_book_placeholder)
                    .error(R.drawable.ic_book_placeholder)
                    .fallback(R.drawable.ic_book_placeholder)
                    .centerCrop()
                    .into(ivCover)

                root.setOnClickListener { onBookClick(book) }

                // Show delete only for owner
                if (onDeleteClick != null && book.ownerUid == currentUserId) {
                    btnDelete.visibility = android.view.View.VISIBLE
                    btnDelete.setOnClickListener { onDeleteClick.invoke(book) }
                } else {
                    btnDelete.visibility = android.view.View.GONE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BookDiffCallback : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Book, newItem: Book) = oldItem == newItem
    }
}
