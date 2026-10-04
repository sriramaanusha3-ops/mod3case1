package com.example.mod3case1.data.api

import com.example.mod3case1.data.model.ChatRequest
import com.example.mod3case1.data.model.ChatResponse
import com.example.mod3case1.data.model.DocumentModel
import com.example.mod3case1.data.model.StatusResponse
import com.example.mod3case1.data.model.UploadResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface RagApiService {

    @GET("/")
    suspend fun getHealth(): Response<StatusResponse>

    @GET("/documents")
    suspend fun getDocuments(): Response<List<DocumentModel>>

    @GET("/documents/{document_id}")
    suspend fun getDocumentDetail(@Path("document_id") documentId: String): Response<DocumentModel>

    @Multipart
    @POST("/documents/upload")
    suspend fun uploadDocument(
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>

    @POST("/documents/sample-corpus")
    suspend fun loadSampleCorpus(): Response<List<DocumentModel>>

    @DELETE("/documents/{document_id}")
    suspend fun deleteDocument(@Path("document_id") documentId: String): Response<Map<String, Any>>

    @POST("/chat")
    suspend fun sendChat(@Body request: ChatRequest): Response<ChatResponse>
}
