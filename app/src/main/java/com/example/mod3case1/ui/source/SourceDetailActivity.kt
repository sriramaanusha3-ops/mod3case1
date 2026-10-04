package com.example.mod3case1.ui.source

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mod3case1.data.model.SourceItem
import com.example.mod3case1.databinding.ActivitySourceDetailBinding
import java.util.Locale

class SourceDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySourceDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySourceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val source = intent.getSerializableExtra("SOURCE_EXTRA") as? SourceItem
        if (source == null) {
            finish()
            return
        }

        setupViews(source)
    }

    private fun setupViews(source: SourceItem) {
        binding.btnBackSourceDetail.setOnClickListener { finish() }

        binding.tvSourceInspectDocName.text = source.documentName
        binding.tvSourceInspectPage.text = "Page ${source.page ?: 1}"

        val matchPercent = String.format(Locale.getDefault(), "%.1f%%", source.score * 100)
        binding.tvSourceInspectMatchScore.text = matchPercent
        binding.tvSourceInspectChunkId.text = source.chunkId

        binding.tvSourceInspectPassage.text = "\"${source.text.trim()}\""

        binding.tvWhyThisSource.text = "This passage was retrieved because its embedding vector achieved a semantic similarity score of ${String.format(Locale.getDefault(), "%.3f", source.score)} with your query. The RAG retriever selected it as high-confidence ground truth to formulate the factual response."
    }
}
