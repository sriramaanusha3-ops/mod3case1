package com.example.mod3case1.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ChatRequest(
    @SerializedName("question")
    val question: String,
    @SerializedName("document_id")
    val documentId: String? = null,
    @SerializedName("top_k")
    val topK: Int = 4,
    @SerializedName("similarity_threshold")
    val similarityThreshold: Double = 0.50
)

data class ChatResponse(
    @SerializedName("question")
    val question: String,
    @SerializedName("answer")
    val answer: String,
    @SerializedName("sources")
    val sources: List<SourceItem> = emptyList(),
    @SerializedName("retrieval")
    val retrieval: RetrievalInfo? = null
)

data class SourceItem(
    @SerializedName("document_id")
    val documentId: String,
    @SerializedName("document_name")
    val documentName: String,
    @SerializedName("page")
    val page: Int? = 1,
    @SerializedName("chunk_id")
    val chunkId: String,
    @SerializedName("score")
    val score: Double = 0.0,
    @SerializedName("text")
    val text: String
) : Serializable

data class RetrievalInfo(
    @SerializedName("chunks_used")
    val chunksUsed: Int = 0,
    @SerializedName("documents_used")
    val documentsUsed: Int = 0,
    @SerializedName("top_match")
    val topMatch: Double = 0.0
) : Serializable

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: String = "Just now",
    val sources: List<SourceItem> = emptyList(),
    val retrievalInfo: RetrievalInfo? = null,
    val documentScope: String = "All Documents"
) : Serializable

data class UploadResponse(
    @SerializedName("document_id")
    val documentId: String,
    @SerializedName("filename")
    val filename: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("pages_count")
    val pagesCount: Int = 1,
    @SerializedName("chunks_count")
    val chunksCount: Int = 0,
    @SerializedName("summary")
    val summary: String? = null
)

data class StatusResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: String = "online",
    @SerializedName("documents_count")
    val documentsCount: Int = 0,
    @SerializedName("chunks_count")
    val chunksCount: Int = 0,
    @SerializedName("model")
    val model: String = "all-MiniLM-L6-v2"
)
