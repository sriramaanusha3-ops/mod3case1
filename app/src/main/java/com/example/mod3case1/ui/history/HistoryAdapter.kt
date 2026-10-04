package com.example.mod3case1.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mod3case1.data.model.ChatMessage
import com.example.mod3case1.databinding.ItemHistoryBinding

class HistoryAdapter(
    private var historyList: List<ChatMessage> = emptyList(),
    private val onItemClick: (ChatMessage) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    fun submitList(newList: List<ChatMessage>) {
        historyList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(historyList[position])
    }

    override fun getItemCount(): Int = historyList.size

    inner class HistoryViewHolder(private val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatMessage) {
            val scopeText = if (item.documentScope.isNotEmpty()) item.documentScope else "All Documents"
            binding.tvHistoryScope.text = scopeText
            binding.tvHistoryTime.text = item.timestamp
            binding.tvHistoryQuestion.text = if (item.documentScope.startsWith("What") || item.documentScope.startsWith("Explain") || item.documentScope.startsWith("Summarize")) {
                item.documentScope.substringBefore(" [Scope:")
            } else {
                "Q: " + item.text.take(60) + "..."
            }
            binding.tvHistoryAnswerPreview.text = item.text
            binding.tvHistorySourcesSummary.text = "${item.sources.size} sources cited"

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }
}
