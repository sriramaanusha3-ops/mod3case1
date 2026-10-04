package com.example.mod3case1.ui.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mod3case1.data.model.ChatMessage
import com.example.mod3case1.data.model.SourceItem
import com.example.mod3case1.databinding.ItemChatAiBinding
import com.example.mod3case1.databinding.ItemChatUserBinding
import java.util.Locale

class ChatAdapter(
    private val onSourceClick: (SourceItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_USER = 1
        private const val TYPE_AI = 2
    }

    private var messages: List<ChatMessage> = emptyList()

    fun submitList(newList: List<ChatMessage>) {
        messages = newList
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) TYPE_USER else TYPE_AI
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_USER) {
            val binding = ItemChatUserBinding.inflate(inflater, parent, false)
            UserViewHolder(binding)
        } else {
            val binding = ItemChatAiBinding.inflate(inflater, parent, false)
            AiViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        if (holder is UserViewHolder) {
            holder.bind(msg)
        } else if (holder is AiViewHolder) {
            holder.bind(msg)
        }
    }

    override fun getItemCount(): Int = messages.size

    inner class UserViewHolder(private val binding: ItemChatUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: ChatMessage) {
            binding.tvUserMessage.text = msg.text
            binding.tvUserTimestamp.text = msg.timestamp
        }
    }

    inner class AiViewHolder(private val binding: ItemChatAiBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: ChatMessage) {
            binding.tvAiAnswer.text = msg.text
            binding.tvAiTimestamp.text = msg.timestamp

            // Retrieval quality details
            val info = msg.retrievalInfo
            if (info != null && info.chunksUsed > 0) {
                binding.tvRetrievalQuality.visibility = View.VISIBLE
                val topScore = String.format(Locale.getDefault(), "%.3f", info.topMatch)
                binding.tvRetrievalQuality.text = "RETRIEVAL QUALITY: Top Match $topScore  •  ${info.chunksUsed} Chunks Used"
            } else {
                binding.tvRetrievalQuality.visibility = View.GONE
            }

            // Sources
            if (msg.sources.isNotEmpty()) {
                binding.layoutSourcesContainer.visibility = View.VISIBLE
                binding.rvSources.layoutManager = LinearLayoutManager(binding.root.context)
                binding.rvSources.adapter = SourceChipAdapter(msg.sources, onSourceClick)
            } else {
                binding.layoutSourcesContainer.visibility = View.GONE
            }

            // Copy button
            binding.btnCopyAiResponse.setOnClickListener {
                val clipboard = binding.root.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("mod3case1 Answer", msg.text)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(binding.root.context, "Answer copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
