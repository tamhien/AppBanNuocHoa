package com.example.perfumeshop.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    const val BASE_URL = "http://10.0.2.2:3000/api/"

    // Hàm tiện ích để lấy URL ảnh đầy đủ
    fun getFullImageUrl(relativeUrl: String?): String {
        if (relativeUrl.isNullOrBlank()) return ""
        if (relativeUrl.startsWith("http")) return relativeUrl
        val cleanPath = if (relativeUrl.startsWith("/")) relativeUrl.substring(1) else relativeUrl
        // Sử dụng base URL gốc (không có /api/) cho ảnh nếu ảnh được lưu ở /uploads
        return "http://10.0.2.2:3000/" + cleanPath
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
            .create(ApiService::class.java)
    }
}
