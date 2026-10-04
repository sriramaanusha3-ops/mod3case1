package com.example.mod3case1.data.repository

import com.example.mod3case1.data.api.ApiClient
import com.example.mod3case1.data.model.ChatRequest
import com.example.mod3case1.data.model.ChatResponse
import com.example.mod3case1.data.model.DocumentModel
import com.example.mod3case1.data.model.StatusResponse
import com.example.mod3case1.data.model.UploadResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class RagRepository {

    suspend fun checkHealth(): Result<StatusResponse> {
        return try {
            val res = ApiClient.getService().getHealth()
            if (res.isSuccessful && res.body() != null) {
                Result.success(res.body()!!)
            } else {
                Result.failure(Exception("Backend status error: ${res.code()} ${res.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDocuments(): Result<List<DocumentModel>> {
        return try {
            val res = ApiClient.getService().getDocuments()
            if (res.isSuccessful && res.body() != null) {
                Result.success(res.body()!!)
            } else {
                Result.failure(Exception("Failed to load documents: ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDocumentDetail(documentId: String): Result<DocumentModel> {
        return try {
            val res = ApiClient.getService().getDocumentDetail(documentId)
            if (res.isSuccessful && res.body() != null) {
                Result.success(res.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch document: ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadFile(file: File): Result<UploadResponse> {
        return try {
            val reqFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, reqFile)
            val res = ApiClient.getService().uploadDocument(body)
            if (res.isSuccessful && res.body() != null) {
                Result.success(res.body()!!)
            } else {
                Result.failure(Exception("Upload failed: ${res.code()} ${res.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadSampleCorpus(): Result<List<DocumentModel>> {
        return try {
            val res = ApiClient.getService().loadSampleCorpus()
            if (res.isSuccessful && res.body() != null) {
                Result.success(res.body()!!)
            } else {
                Result.failure(Exception("Failed to load sample corpus: ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDocument(documentId: String): Result<Boolean> {
        return try {
            val res = ApiClient.getService().deleteDocument(documentId)
            if (res.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to delete document: ${res.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun askQuestion(
        question: String,
        documentId: String?,
        topK: Int,
        similarityThreshold: Double
    ): Result<ChatResponse> {
        return try {
            val req = ChatRequest(
                question = question.trim(),
                documentId = documentId,
                topK = topK,
                similarityThreshold = similarityThreshold
            )
            val res = ApiClient.getService().sendChat(req)
            if (res.isSuccessful && res.body() != null) {
                Result.success(res.body()!!)
            } else {
                val errorMsg = res.errorBody()?.string() ?: res.message()
                Result.failure(Exception("Chat request failed: $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
