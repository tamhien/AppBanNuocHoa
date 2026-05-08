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

    // --- ADMIN APIs ---
    
    // Quản lý sản phẩm
    @POST("api/perfumes")
    suspend fun addPerfume(@Body perfume: Perfume): Response<BaseResponse>

    @PUT("api/perfumes/{id}")
    suspend fun updatePerfume(@Path("id") id: Int, @Body perfume: Perfume): Response<BaseResponse>

    @DELETE("api/perfumes/{id}")
    suspend fun deletePerfume(@Path("id") id: Int): Response<BaseResponse>

    // Quản lý đơn hàng
    @GET("api/admin/orders")
    suspend fun getAllOrders(): Response<List<Order>>

    @PUT("api/admin/orders/status")
    suspend fun updateOrderStatus(@Body request: UpdateOrderStatusRequest): Response<BaseResponse>

    // Quản lý khách hàng
    @GET("api/admin/users")
    suspend fun getAllUsers(): Response<List<UserResponse>>

    @DELETE("api/admin/users/{id}")
    suspend fun deleteUser(@Path("id") userId: Int): Response<BaseResponse>

    // Thống kê
    @GET("api/admin/revenue")
    suspend fun getRevenue(): Response<RevenueResponse>

    // Upload ảnh
    @Multipart
    @POST("api/upload")
    suspend fun uploadImage(@Part image: okhttp3.MultipartBody.Part): Response<UploadResponse>
}
