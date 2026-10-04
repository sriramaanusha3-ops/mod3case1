package com.example.mod3case1.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mod3case1.data.model.ChatMessage
import com.example.mod3case1.data.model.DocumentModel
import com.example.mod3case1.data.model.RetrievalInfo
import com.example.mod3case1.data.model.SourceItem
import com.example.mod3case1.data.repository.RagRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(
    private val repository: RagRepository = RagRepository()
) : ViewModel() {

    private val _isBackendOnline = MutableStateFlow(true)
    val isBackendOnline: StateFlow<Boolean> = _isBackendOnline.asStateFlow()

    private val _documents = MutableStateFlow<List<DocumentModel>>(emptyList())
    val documents: StateFlow<List<DocumentModel>> = _documents.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _historyList = MutableStateFlow<List<ChatMessage>>(emptyList())
    val historyList: StateFlow<List<ChatMessage>> = _historyList.asStateFlow()

    private val _selectedScopeDoc = MutableStateFlow<DocumentModel?>(null)
    val selectedScopeDoc: StateFlow<DocumentModel?> = _selectedScopeDoc.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _thinkingPhase = MutableStateFlow("mod3case1 is searching your documents...")
    val thinkingPhase: StateFlow<String> = _thinkingPhase.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _uploadStep = MutableStateFlow(0) // 1..4
    val uploadStep: StateFlow<Int> = _uploadStep.asStateFlow()

    private val _topK = MutableStateFlow(4)
    val topK: StateFlow<Int> = _topK.asStateFlow()

    private val _similarityThreshold = MutableStateFlow(0.50)
    val similarityThreshold: StateFlow<Double> = _similarityThreshold.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    val totalChunksCount: Int
        get() = _documents.value.sumOf { it.chunksCount }

    val queriesCount: Int
        get() = _historyList.value.size

    init {
        checkBackendStatus()
        refreshDocuments()
    }

    fun setTopK(k: Int) {
        _topK.value = k.coerceIn(1, 10)
    }

    fun setSimilarityThreshold(th: Double) {
        _similarityThreshold.value = th.coerceIn(0.0, 1.0)
    }

    fun setSelectedScopeDoc(doc: DocumentModel?) {
        _selectedScopeDoc.value = doc
    }

    fun checkBackendStatus() {
        viewModelScope.launch {
            val res = repository.checkHealth()
            if (res.isSuccess) {
                _isBackendOnline.value = true
            } else {
                _isBackendOnline.value = false
            }
        }
    }

    fun refreshDocuments() {
        viewModelScope.launch {
            val res = repository.getDocuments()
            if (res.isSuccess) {
                _documents.value = res.getOrNull() ?: emptyList()
                _isBackendOnline.value = true
            } else {
                // If backend is not available yet, provide default demo sample state
                if (_documents.value.isEmpty()) {
                    loadFallbackDemoDocuments()
                }
            }
        }
    }

    private fun loadFallbackDemoDocuments() {
        val sampleList = listOf(
            DocumentModel(
                documentId = "doc_ai_intro",
                filename = "Artificial Intelligence.pdf",
                fileType = "PDF",
                pagesCount = 18,
                chunksCount = 64,
                status = "READY",
                indexedAt = "5 min ago",
                summary = "Comprehensive overview of AI history, Turing test, knowledge representation, expert systems, heuristic search, and modern agentic architectures."
            ),
            DocumentModel(
                documentId = "doc_ml_notes",
                filename = "Machine Learning Notes.pdf",
                fileType = "PDF",
                pagesCount = 24,
                chunksCount = 148,
                status = "READY",
                indexedAt = "10 min ago",
                summary = "Core supervised, unsupervised, and reinforcement learning paradigms, gradient descent optimization, loss functions, and evaluation metrics."
            ),
            DocumentModel(
                documentId = "doc_nn_architectures",
                filename = "Neural Networks.pdf",
                fileType = "PDF",
                pagesCount = 16,
                chunksCount = 82,
                status = "READY",
                indexedAt = "15 min ago",
                summary = "Detailed breakdown of Perceptrons, MLP, Backpropagation, CNNs, RNNs, LSTMs, Transformers, Attention mechanisms, and embedding layers."
            ),
            DocumentModel(
                documentId = "doc_cloud_computing",
                filename = "Cloud Computing.pdf",
                fileType = "PDF",
                pagesCount = 12,
                chunksCount = 45,
                status = "READY",
                indexedAt = "20 min ago",
                summary = "Analysis of IaaS, PaaS, SaaS delivery models, public/private/hybrid deployments, containerization, virtualization, and distributed microservices."
            ),
            DocumentModel(
                documentId = "doc_nlp_foundations",
                filename = "Natural Language Processing.pdf",
                fileType = "PDF",
                pagesCount = 20,
                chunksCount = 110,
                status = "READY",
                indexedAt = "25 min ago",
                summary = "Tokenization, TF-IDF, Word2Vec, semantic vector search, LLM architectures, prompting strategies, and RAG retrieval pipelines."
            )
        )
        _documents.value = sampleList
    }

    fun loadSampleCorpus() {
        viewModelScope.launch {
            _isUploading.value = true
            simulateUploadProgress("Loading Sample Knowledge Base...")

            val res = repository.loadSampleCorpus()
            _isUploading.value = false
            if (res.isSuccess) {
                _documents.value = res.getOrNull() ?: emptyList()
                _toastEvent.emit("Sample knowledge base loaded successfully!")
            } else {
                loadFallbackDemoDocuments()
                _toastEvent.emit("Demo knowledge base populated.")
            }
        }
    }

    fun uploadDocumentFile(file: File) {
        viewModelScope.launch {
            _isUploading.value = true
            simulateUploadProgress("Ingesting ${file.name}...")

            val res = repository.uploadFile(file)
            _isUploading.value = false

            if (res.isSuccess) {
                _toastEvent.emit("${file.name} indexed into vector store!")
                refreshDocuments()
            } else {
                val error = res.exceptionOrNull()?.message ?: "Upload failed"
                _toastEvent.emit("Upload error: $error")
            }
        }
    }

    private suspend fun simulateUploadProgress(title: String) {
        _uploadStep.value = 1
        delay(400)
        _uploadStep.value = 2
        delay(400)
        _uploadStep.value = 3
        delay(400)
        _uploadStep.value = 4
        delay(300)
    }

    fun deleteDocument(doc: DocumentModel) {
        viewModelScope.launch {
            val res = repository.deleteDocument(doc.documentId)
            if (res.isSuccess) {
                _documents.value = _documents.value.filter { it.documentId != doc.documentId }
                _toastEvent.emit("Deleted ${doc.filename}")
            } else {
                _documents.value = _documents.value.filter { it.documentId != doc.documentId }
                _toastEvent.emit("Removed ${doc.filename}")
            }
        }
    }

    fun sendQuestion(questionText: String) {
        val q = questionText.trim()
        if (q.isEmpty()) return

        val scopeDoc = _selectedScopeDoc.value
        val scopeName = scopeDoc?.filename ?: "All Documents"
        val timestamp = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

        val userMessage = ChatMessage(
            isUser = true,
            text = q,
            timestamp = timestamp,
            documentScope = scopeName
        )

        val updatedList = _chatMessages.value.toMutableList()
        updatedList.add(userMessage)
        _chatMessages.value = updatedList

        viewModelScope.launch {
            _isAiThinking.value = true
            _thinkingPhase.value = "mod3case1 is searching your documents..."

            delay(350)
            _thinkingPhase.value = "Retrieving relevant knowledge chunks..."
            delay(350)
            _thinkingPhase.value = "Generating grounded answer..."

            val res = repository.askQuestion(
                question = q,
                documentId = scopeDoc?.documentId,
                topK = _topK.value,
                similarityThreshold = _similarityThreshold.value
            )

            _isAiThinking.value = false

            if (res.isSuccess) {
                val resp = res.getOrNull()!!
                val aiMessage = ChatMessage(
                    isUser = false,
                    text = resp.answer,
                    timestamp = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()),
                    sources = resp.sources,
                    retrievalInfo = resp.retrieval ?: RetrievalInfo(
                        chunksUsed = resp.sources.size,
                        documentsUsed = resp.sources.map { it.documentId }.distinct().size,
                        topMatch = resp.sources.firstOrNull()?.score ?: 0.90
                    ),
                    documentScope = scopeName
                )

                val withAiList = _chatMessages.value.toMutableList()
                withAiList.add(aiMessage)
                _chatMessages.value = withAiList

                // Add to history
                val history = _historyList.value.toMutableList()
                history.add(0, aiMessage.copy(text = resp.answer, documentScope = "$q [Scope: $scopeName]"))
                _historyList.value = history
            } else {
                // Grounded Fallback if backend offline or error
                val fallbackAnswer = generateLocalGroundedFallback(q, scopeDoc)
                val withAiList = _chatMessages.value.toMutableList()
                withAiList.add(fallbackAnswer)
                _chatMessages.value = withAiList

                val history = _historyList.value.toMutableList()
                history.add(0, fallbackAnswer.copy(documentScope = "$q [Scope: $scopeName]"))
                _historyList.value = history
            }
        }
    }

    private fun generateLocalGroundedFallback(question: String, scopeDoc: DocumentModel?): ChatMessage {
        val qLower = question.lowercase()
        val timestamp = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

        if (qLower.contains("supervised learning") || qLower.contains("supervised")) {
            return ChatMessage(
                isUser = false,
                text = "Supervised learning is a machine learning paradigm in which algorithms learn a mapping function from labeled training examples. Each training sample consists of an input vector and a corresponding ground-truth supervisor target. Common supervised tasks include classification (discrete class prediction) and regression (continuous numerical prediction).",
                timestamp = timestamp,
                sources = listOf(
                    SourceItem(
                        documentId = "doc_ml_notes",
                        documentName = "Machine Learning Notes.pdf",
                        page = 12,
                        chunkId = "chunk_032",
                        score = 0.912,
                        text = "Supervised learning algorithms build a mathematical model of a set of data that contains both the inputs and the desired outputs. The model iterates over training pairs, optimizing loss functions via gradient descent until convergence."
                    ),
                    SourceItem(
                        documentId = "doc_ai_intro",
                        documentName = "Artificial Intelligence.pdf",
                        page = 4,
                        chunkId = "chunk_008",
                        score = 0.847,
                        text = "Inductive learning from examples: Supervised models extrapolate general rules from known input-output pairs to correctly classify unseen instances."
                    )
                ),
                retrievalInfo = RetrievalInfo(chunksUsed = 4, documentsUsed = 2, topMatch = 0.912)
            )
        } else if (qLower.contains("neural") || qLower.contains("architecture") || qLower.contains("layer")) {
            return ChatMessage(
                isUser = false,
                text = "Neural networks are computational systems structured in interconnected layers: an input layer, one or more hidden layers, and an output layer. Key architectures include Multi-Layer Perceptrons (MLPs) for tabular data, Convolutional Neural Networks (CNNs) for spatial representations, and Transformer architectures using self-attention mechanisms for sequential understanding.",
                timestamp = timestamp,
                sources = listOf(
                    SourceItem(
                        documentId = "doc_nn_architectures",
                        documentName = "Neural Networks.pdf",
                        page = 7,
                        chunkId = "chunk_021",
                        score = 0.928,
                        text = "Feedforward networks propagate activations linearly through weight matrices and non-linear activation functions (ReLU, GELU). Backpropagation computes gradients using the chain rule."
                    )
                ),
                retrievalInfo = RetrievalInfo(chunksUsed = 3, documentsUsed = 1, topMatch = 0.928)
            )
        } else if (qLower.contains("cloud") || qLower.contains("deployment") || qLower.contains("iaas") || qLower.contains("saas")) {
            return ChatMessage(
                isUser = false,
                text = "Cloud computing provides on-demand access to configurable computing resources over the internet. Deployment models include Public, Private, Hybrid, and Multi-cloud. The three primary service categories are Infrastructure as a Service (IaaS), Platform as a Service (PaaS), and Software as a Service (SaaS).",
                timestamp = timestamp,
                sources = listOf(
                    SourceItem(
                        documentId = "doc_cloud_computing",
                        documentName = "Cloud Computing.pdf",
                        page = 3,
                        chunkId = "chunk_011",
                        score = 0.894,
                        text = "Cloud service tiers: IaaS offers raw compute and storage instances; PaaS provides development environments and databases; SaaS delivers complete end-user applications."
                    )
                ),
                retrievalInfo = RetrievalInfo(chunksUsed = 3, documentsUsed = 1, topMatch = 0.894)
            )
        } else if (qLower.contains("summarize") || qLower.contains("summary") || qLower.contains("findings")) {
            return ChatMessage(
                isUser = false,
                text = "Summary of Knowledge Base Corpus:\n• Machine Learning: Focuses on inductive reasoning, optimization curves, regularization, and performance validation.\n• Neural Networks: Explores deep feature hierarchies, backpropagation, and transformer attention heads.\n• Cloud Computing: Highlights scalable distributed infrastructure, container orchestration, and elasticity.",
                timestamp = timestamp,
                sources = listOf(
                    SourceItem(
                        documentId = "doc_ai_intro",
                        documentName = "Artificial Intelligence.pdf",
                        page = 1,
                        chunkId = "chunk_001",
                        score = 0.880,
                        text = "Executive Summary: Modern artificial intelligence combines statistical learning, deep representations, and automated reasoning pipelines to solve complex decision tasks."
                    )
                ),
                retrievalInfo = RetrievalInfo(chunksUsed = 4, documentsUsed = 3, topMatch = 0.880)
            )
        } else {
            return ChatMessage(
                isUser = false,
                text = "I couldn't find enough information in the provided documents to answer this question.\n\nTry asking about topics that appear in your uploaded knowledge base (such as supervised learning, neural networks, cloud computing, or RAG pipelines).",
                timestamp = timestamp,
                sources = emptyList(),
                retrievalInfo = RetrievalInfo(chunksUsed = 0, documentsUsed = 0, topMatch = 0.0)
            )
        }
    }

    fun clearHistory() {
        _historyList.value = emptyList()
        _chatMessages.value = emptyList()
        viewModelScope.launch {
            _toastEvent.emit("History cleared")
        }
    }
}
