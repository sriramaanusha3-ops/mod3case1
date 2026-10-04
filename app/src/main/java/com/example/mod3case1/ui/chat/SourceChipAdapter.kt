package com.example.mod3case1.ui.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mod3case1.data.model.SourceItem
import com.example.mod3case1.databinding.ItemSourceChipBinding
import java.util.Locale

class SourceChipAdapter(
    private val sources: List<SourceItem>,
    private val onSourceClick: (SourceItem) -> Unit
) : RecyclerView.Adapter<SourceChipAdapter.SourceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SourceViewHolder {
        val binding = ItemSourceChipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SourceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SourceViewHolder, position: Int) {
        holder.bind(sources[position], position)
    }

    override fun getItemCount(): Int = sources.size

    inner class SourceViewHolder(private val binding: ItemSourceChipBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(source: SourceItem, position: Int) {
            binding.tvSourceIndex.text = String.format(Locale.getDefault(), "%02d", position + 1)
            binding.tvSourceDocName.text = source.documentName
            val scorePct = String.format(Locale.getDefault(), "%.3f", source.score)
            binding.tvSourcePageSimilarity.text = "Page ${source.page ?: 1}  •  Similarity: $scorePct"

            binding.sourceItemContainer.setOnClickListener {
                onSourceClick(source)
            }
        }
    }
}
