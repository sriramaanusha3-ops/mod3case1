package com.example.mod3case1.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mod3case1.databinding.ActivityHowItWorksBinding

class HowItWorksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHowItWorksBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHowItWorksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBackHowItWorks.setOnClickListener {
            finish()
        }
    }
}
