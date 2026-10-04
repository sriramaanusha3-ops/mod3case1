package com.example.mod3case1.ui.documents

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mod3case1.R
import com.example.mod3case1.data.model.DocumentModel
import com.example.mod3case1.databinding.ItemDocumentBinding

class DocumentAdapter(
    private val onDetailsClick: (DocumentModel) -> Unit,
    private val onDeleteClick: (DocumentModel) -> Unit
) : ListAdapter<DocumentModel, DocumentAdapter.DocViewHolder>(DocDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocViewHolder {
        val binding = ItemDocumentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DocViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DocViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DocViewHolder(private val binding: ItemDocumentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(doc: DocumentModel) {
            binding.tvDocTitle.text = doc.filename
            binding.tvDocBadge.text = doc.fileType.uppercase()
            binding.tvDocMetrics.text = "${doc.pagesCount} pages  •  ${doc.chunksCount} chunks"
            binding.tvDocIndexedTime.text = "Indexed ${doc.indexedAt}"

            when (doc.status.uppercase()) {
                "READY" -> {
                    binding.tvDocStatus.text = "● READY"
                    binding.tvDocStatus.setBackgroundResource(R.drawable.bg_status_ready)
                    binding.tvDocStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.green_status))
                }
                "PROCESSING" -> {
                    binding.tvDocStatus.text = "⏳ INDEXING"
                    binding.tvDocStatus.setBackgroundResource(R.drawable.bg_status_processing)
                    binding.tvDocStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.yellow_status))
                }
                else -> {
                    binding.tvDocStatus.text = "⚠ ERROR"
                    binding.tvDocStatus.setBackgroundResource(R.drawable.bg_status_error)
                    binding.tvDocStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.red_status))
                }
            }

            binding.btnDocDetails.setOnClickListener { onDetailsClick(doc) }
            binding.root.setOnClickListener { onDetailsClick(doc) }
            binding.btnDeleteDoc.setOnClickListener { onDeleteClick(doc) }
        }
    }

    class DocDiffCallback : DiffUtil.ItemCallback<DocumentModel>() {
        override fun areItemsTheSame(oldItem: DocumentModel, newItem: DocumentModel): Boolean =
            oldItem.documentId == newItem.documentId

        override fun areContentsTheSame(oldItem: DocumentModel, newItem: DocumentModel): Boolean =
            oldItem == newItem
    }
}
