package com.example.mod3case1.ui.documents

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mod3case1.data.model.DocumentModel
import com.example.mod3case1.databinding.FragmentDocumentsBinding
import com.example.mod3case1.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class DocumentsFragment : Fragment() {

    private var _binding: FragmentDocumentsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var docAdapter: DocumentAdapter

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { handleSelectedFile(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDocumentsBinding.inflate(inflater, container, false)
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
        binding.rvDocuments.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDocuments.adapter = docAdapter
    }

    private fun setupListeners() {
        binding.btnAddDocument.setOnClickListener {
            openFilePicker()
        }

        binding.btnEmptyAddDoc.setOnClickListener {
            openFilePicker()
        }

        binding.btnLoadSampleDocs.setOnClickListener {
            viewModel.loadSampleCorpus()
        }
    }

    private fun openFilePicker() {
        filePickerLauncher.launch(arrayOf("application/pdf", "text/plain", "application/msword", "*/*"))
    }

    private fun handleSelectedFile(uri: Uri) {
        try {
            var fileName = "document_${System.currentTimeMillis()}.pdf"
            val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        fileName = it.getString(nameIndex)
                    }
                }
            }

            val tempFile = File(requireContext().cacheDir, fileName)
            requireContext().contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            viewModel.uploadDocumentFile(tempFile)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to read file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.documents.collectLatest { docs ->
                if (docs.isEmpty() && !viewModel.isUploading.value) {
                    binding.rvDocuments.visibility = View.GONE
                    binding.layoutEmptyDocuments.visibility = View.VISIBLE
                } else {
                    binding.rvDocuments.visibility = View.VISIBLE
                    binding.layoutEmptyDocuments.visibility = View.GONE
                    docAdapter.submitList(docs)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isUploading.collectLatest { isUploading ->
                binding.cardProcessing.visibility = if (isUploading) View.VISIBLE else View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uploadStep.collectLatest { step ->
                updateStepUI(step)
            }
        }
    }

    private fun updateStepUI(step: Int) {
        when (step) {
            1 -> {
                binding.tvStep1Status.text = "⏳"
                binding.tvStep2Status.text = "• • •"
                binding.tvStep3Status.text = "• • •"
                binding.tvStep4Status.text = "• • •"
            }
            2 -> {
                binding.tvStep1Status.text = "✓"
                binding.tvStep2Status.text = "⏳"
                binding.tvStep3Status.text = "• • •"
                binding.tvStep4Status.text = "• • •"
            }
            3 -> {
                binding.tvStep1Status.text = "✓"
                binding.tvStep2Status.text = "✓"
                binding.tvStep3Status.text = "⏳"
                binding.tvStep4Status.text = "• • •"
            }
            4 -> {
                binding.tvStep1Status.text = "✓"
                binding.tvStep2Status.text = "✓"
                binding.tvStep3Status.text = "✓"
                binding.tvStep4Status.text = "✓"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
