package com.example.mod3case1.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mod3case1.R
import com.example.mod3case1.databinding.FragmentHomeBinding
import com.example.mod3case1.ui.MainActivity
import com.example.mod3case1.ui.documents.DocumentAdapter
import com.example.mod3case1.ui.documents.DocumentDetailActivity
import com.example.mod3case1.ui.settings.HowItWorksActivity
import com.example.mod3case1.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var docAdapter: DocumentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        docAdapter = DocumentAdapter(
            onDetailsClick = { doc ->
                val intent = Intent(requireContext(), DocumentDetailActivity::class.java).apply {
                    putExtra("DOCUMENT_EXTRA", doc)
                }
                startActivity(intent)
            },
            onDeleteClick = { doc ->
                viewModel.deleteDocument(doc)
            }
        )
        binding.rvRecentDocs.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecentDocs.adapter = docAdapter
    }

    private fun setupListeners() {
        binding.btnQuickSend.setOnClickListener {
            val q = binding.etQuickAsk.text.toString().trim()
            if (q.isNotEmpty()) {
                (activity as? MainActivity)?.navigateToChatWithQuery(q)
                binding.etQuickAsk.text.clear()
            }
        }

        binding.btnViewAllDocs.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(R.id.nav_documents)
        }

        binding.btnHomeAddDoc.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(R.id.nav_documents)
        }

        binding.cardHowItWorks.setOnClickListener {
            startActivity(Intent(requireContext(), HowItWorksActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.documents.collectLatest { docs ->
                binding.tvStatDocs.text = docs.size.toString()
                binding.tvStatChunks.text = docs.sumOf { it.chunksCount }.toString()

                if (docs.isEmpty()) {
                    binding.rvRecentDocs.visibility = View.GONE
                    binding.layoutHomeEmptyDocs.visibility = View.VISIBLE
                } else {
                    binding.rvRecentDocs.visibility = View.VISIBLE
                    binding.layoutHomeEmptyDocs.visibility = View.GONE
                    docAdapter.submitList(docs.take(3))
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.historyList.collectLatest { history ->
                binding.tvStatQueries.text = history.size.toString()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
