package com.example.perfumeshop.api

import com.example.perfumeshop.model.AuthResponse
import com.example.perfumeshop.model.LoginRequest
import com.example.perfumeshop.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
}
