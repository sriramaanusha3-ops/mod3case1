package com.example.mod3case1.ui.chat

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mod3case1.R
import com.example.mod3case1.data.model.SourceItem
import com.example.mod3case1.databinding.FragmentChatBinding
import com.example.mod3case1.ui.source.SourceDetailActivity
import com.example.mod3case1.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(
            onSourceClick = { source: SourceItem ->
                val intent = Intent(requireContext(), SourceDetailActivity::class.java).apply {
                    putExtra("SOURCE_EXTRA", source)
                }
                startActivity(intent)
            }
        )
        val layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.rvChatMessages.layoutManager = layoutManager
        binding.rvChatMessages.adapter = chatAdapter
    }

    private fun setupListeners() {
        binding.btnSendChat.setOnClickListener {
            sendMessage()
        }

        binding.etChatInput.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }

        binding.etChatInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.btnClearChatInput.visibility = if (!s.isNullOrEmpty()) View.VISIBLE else View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnClearChatInput.setOnClickListener {
            binding.etChatInput.text.clear()
        }

        // Scope selector
        binding.btnScopeSelector.setOnClickListener {
            showScopeSelectionDialog()
        }

        // Suggested question chips
        binding.chipSuggested1.setOnClickListener { submitPrompt(binding.chipSuggested1.text.toString()) }
        binding.chipSuggested2.setOnClickListener { submitPrompt(binding.chipSuggested2.text.toString()) }
        binding.chipSuggested3.setOnClickListener { submitPrompt(binding.chipSuggested3.text.toString()) }
        binding.chipSuggested4.setOnClickListener { submitPrompt(binding.chipSuggested4.text.toString()) }
        binding.chipSuggested5.setOnClickListener { submitPrompt(binding.chipSuggested5.text.toString()) }
    }

    private fun sendMessage() {
        val q = binding.etChatInput.text.toString().trim()
        if (q.isNotEmpty()) {
            submitPrompt(q)
            binding.etChatInput.text.clear()
        }
    }

    fun submitPrompt(query: String) {
        viewModel.sendQuestion(query)
    }

    private fun showScopeSelectionDialog() {
        val docs = viewModel.documents.value
        val items = mutableListOf("All Documents")
        items.addAll(docs.map { it.filename })

        val currentSelected = viewModel.selectedScopeDoc.value
        val checkedItem = if (currentSelected == null) 0 else items.indexOf(currentSelected.filename).coerceAtLeast(0)

        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Document Scope")
            .setSingleChoiceItems(items.toTypedArray(), checkedItem) { dialog, which ->
                if (which == 0) {
                    viewModel.setSelectedScopeDoc(null)
                } else {
                    val selectedDoc = docs.getOrNull(which - 1)
                    viewModel.setSelectedScopeDoc(selectedDoc)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.chatMessages.collectLatest { messages ->
                if (messages.isEmpty() && !viewModel.isAiThinking.value) {
                    binding.rvChatMessages.visibility = View.GONE
                    binding.layoutSuggestedQuestions.visibility = View.VISIBLE
                } else {
                    binding.rvChatMessages.visibility = View.VISIBLE
                    binding.layoutSuggestedQuestions.visibility = View.GONE
                    chatAdapter.submitList(messages)
                    if (messages.isNotEmpty()) {
                        binding.rvChatMessages.smoothScrollToPosition(messages.size - 1)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isAiThinking.collectLatest { isThinking ->
                binding.cardAiThinking.visibility = if (isThinking) View.VISIBLE else View.GONE
                if (isThinking) {
                    binding.layoutSuggestedQuestions.visibility = View.GONE
                    binding.rvChatMessages.visibility = View.VISIBLE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.thinkingPhase.collectLatest { phase ->
                binding.tvThinkingPhase.text = phase
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedScopeDoc.collectLatest { scopeDoc ->
                binding.tvCurrentScope.text = scopeDoc?.filename ?: "All Documents"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
