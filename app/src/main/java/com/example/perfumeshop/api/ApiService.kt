package com.example.perfumeshop.api

import com.example.perfumeshop.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("api/perfumes")
    suspend fun getAllPerfumes(@Query("gender") gender: String? = null): Response<List<Perfume>>

    // Lấy thông tin cá nhân
    @GET("api/profile/{id}")
    suspend fun getProfile(@Path("id") userId: Int): Response<UserResponse>

    // Cập nhật thông tin cá nhân
    @PUT("api/profile/{id}")
    suspend fun updateProfile(@Path("id") userId: Int, @Body request: UpdateProfileRequest): Response<BaseResponse>

    // Đổi mật khẩu
    @PUT("api/change-password/{id}")
    suspend fun changePassword(@Path("id") userId: Int, @Body request: ChangePasswordRequest): Response<BaseResponse>
}
