package com.example.mod3case1.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.mod3case1.R
import com.example.mod3case1.databinding.ActivityMainBinding
import com.example.mod3case1.ui.chat.ChatFragment
import com.example.mod3case1.ui.documents.DocumentsFragment
import com.example.mod3case1.ui.history.HistoryFragment
import com.example.mod3case1.ui.home.HomeFragment
import com.example.mod3case1.ui.settings.SettingsFragment
import com.example.mod3case1.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private val homeFragment = HomeFragment()
    private val documentsFragment = DocumentsFragment()
    private val chatFragment = ChatFragment()
    private val historyFragment = HistoryFragment()
    private val settingsFragment = SettingsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            loadFragment(homeFragment)
        }

        setupBottomNavigation()
        observeViewModel()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(homeFragment)
                    true
                }
                R.id.nav_documents -> {
                    loadFragment(documentsFragment)
                    true
                }
                R.id.nav_ask -> {
                    loadFragment(chatFragment)
                    true
                }
                R.id.nav_history -> {
                    loadFragment(historyFragment)
                    true
                }
                R.id.nav_settings -> {
                    loadFragment(settingsFragment)
                    true
                }
                else -> false
            }
        }
    }

    fun navigateToTab(tabId: Int) {
        binding.bottomNavigation.selectedItemId = tabId
    }

    fun navigateToChatTab() {
        navigateToTab(R.id.nav_ask)
    }

    fun navigateToChatWithQuery(query: String) {
        navigateToTab(R.id.nav_ask)
        chatFragment.submitPrompt(query)
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.isBackendOnline.collectLatest { online ->
                if (online) {
                    binding.tvEngineStatus.text = getString(R.string.rag_engine_online)
                    binding.tvEngineStatus.setBackgroundResource(R.drawable.bg_status_ready)
                    binding.tvEngineStatus.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.green_status))
                } else {
                    binding.tvEngineStatus.text = getString(R.string.rag_engine_offline)
                    binding.tvEngineStatus.setBackgroundResource(R.drawable.bg_status_error)
                    binding.tvEngineStatus.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.red_status))
                }
            }
        }

        lifecycleScope.launch {
            viewModel.toastEvent.collectLatest { msg ->
                Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
