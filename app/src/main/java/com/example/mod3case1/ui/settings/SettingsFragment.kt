package com.example.mod3case1.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.mod3case1.R
import com.example.mod3case1.data.api.ApiClient
import com.example.mod3case1.databinding.FragmentSettingsBinding
import com.example.mod3case1.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupInitialValues()
        setupListeners()
        observeViewModel()
    }

    private fun setupInitialValues() {
        binding.etBackendUrl.setText(ApiClient.getBaseUrl())
        binding.sliderTopK.value = viewModel.topK.value.toFloat()
        binding.sliderThreshold.value = viewModel.similarityThreshold.value.toFloat()

        binding.tvTopKValue.text = "${viewModel.topK.value} chunks"
        binding.tvThresholdValue.text = String.format(Locale.getDefault(), "%.2f", viewModel.similarityThreshold.value)
    }

    private fun setupListeners() {
        binding.btnTestBackend.setOnClickListener {
            val url = binding.etBackendUrl.text.toString().trim()
            if (url.isNotEmpty()) {
                ApiClient.updateBaseUrl(url)
                binding.tvTestStatus.visibility = View.VISIBLE
                binding.tvTestStatus.text = "Testing connection to $url..."
                binding.tvTestStatus.setTextColor(resources.getColor(R.color.yellow_status, null))

                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.checkBackendStatus()
                    if (viewModel.isBackendOnline.value) {
                        binding.tvTestStatus.text = "✓ Connected to FastAPI RAG backend!"
                        binding.tvTestStatus.setTextColor(resources.getColor(R.color.green_status, null))
                    } else {
                        binding.tvTestStatus.text = "⚠ Backend unreachable. Check host/port or start server."
                        binding.tvTestStatus.setTextColor(resources.getColor(R.color.red_status, null))
                    }
                }
            }
        }

        binding.sliderTopK.addOnChangeListener { _, value, _ ->
            val k = value.toInt()
            binding.tvTopKValue.text = "$k chunks"
            viewModel.setTopK(k)
        }

        binding.sliderThreshold.addOnChangeListener { _, value, _ ->
            binding.tvThresholdValue.text = String.format(Locale.getDefault(), "%.2f", value)
            viewModel.setSimilarityThreshold(value.toDouble())
        }

        binding.btnOpenHowItWorks.setOnClickListener {
            startActivity(Intent(requireContext(), HowItWorksActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isBackendOnline.collectLatest { online ->
                if (online) {
                    binding.tvTestStatus.visibility = View.VISIBLE
                    binding.tvTestStatus.text = "✓ FastAPI RAG Engine is Online"
                    binding.tvTestStatus.setTextColor(resources.getColor(R.color.green_status, null))
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
