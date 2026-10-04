package com.example.mod3case1.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val DEFAULT_BASE_URL = "http://10.0.2.2:8000/"
    private var currentBaseUrl = DEFAULT_BASE_URL

    private var retrofit: Retrofit? = null
    private var apiService: RagApiService? = null

    fun getBaseUrl(): String = currentBaseUrl

    fun updateBaseUrl(newUrl: String) {
        var formatted = newUrl.trim()
        if (!formatted.endsWith("/")) {
            formatted += "/"
        }
        currentBaseUrl = formatted
        retrofit = null
        apiService = null
    }

    private fun getClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    fun getService(): RagApiService {
        if (apiService == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(currentBaseUrl)
                .client(getClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            apiService = retrofit!!.create(RagApiService::class.java)
        }
        return apiService!!
    }
}
