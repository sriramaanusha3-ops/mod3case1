package com.example.mod3case1.ui.documents

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mod3case1.data.model.ChunkModel
import com.example.mod3case1.databinding.ItemChunkBinding

class ChunkAdapter(
    private var chunks: List<ChunkModel> = emptyList()
) : RecyclerView.Adapter<ChunkAdapter.ChunkViewHolder>() {

    fun submitList(newList: List<ChunkModel>) {
        chunks = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChunkViewHolder {
        val binding = ItemChunkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChunkViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChunkViewHolder, position: Int) {
        holder.bind(chunks[position], position)
    }

    override fun getItemCount(): Int = chunks.size

    inner class ChunkViewHolder(private val binding: ItemChunkBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chunk: ChunkModel, pos: Int) {
            binding.tvChunkId.text = "Chunk #${String.format("%02d", pos + 1)} (${chunk.chunkId})"
            binding.tvChunkPage.text = "Page ${chunk.page ?: 1}"
            binding.tvChunkText.text = chunk.text
        }
    }
}
