package com.example.mod3case1.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class DocumentModel(
    @SerializedName("document_id")
    val documentId: String,
    @SerializedName("filename")
    val filename: String,
    @SerializedName("file_type")
    val fileType: String = "PDF",
    @SerializedName("pages_count")
    val pagesCount: Int = 1,
    @SerializedName("chunks_count")
    val chunksCount: Int = 0,
    @SerializedName("status")
    val status: String = "READY", // PROCESSING, READY, ERROR
    @SerializedName("indexed_at")
    val indexedAt: String = "Just now",
    @SerializedName("summary")
    val summary: String? = null,
    @SerializedName("chunks")
    val chunks: List<ChunkModel> = emptyList()
) : Serializable

data class ChunkModel(
    @SerializedName("chunk_id")
    val chunkId: String,
    @SerializedName("document_id")
    val documentId: String,
    @SerializedName("document_name")
    val documentName: String,
    @SerializedName("page")
    val page: Int? = 1,
    @SerializedName("text")
    val text: String
) : Serializable
