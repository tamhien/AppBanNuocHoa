package com.example.perfumeshop.api

import com.example.perfumeshop.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<BaseResponse>

    @POST("send-otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): Response<BaseResponse>

    @POST("reset-password-otp")
    suspend fun resetPasswordOtp(@Body request: ResetPasswordOtpRequest): Response<BaseResponse>

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("perfumes")
    suspend fun getAllPerfumes(@Query("gender") gender: String? = null): Response<List<Perfume>>

    // Lấy thông tin cá nhân
    @GET("profile/{id}")
    suspend fun getProfile(@Path("id") userId: Int): Response<UserResponse>

    // Cập nhật thông tin cá nhân
    @PUT("profile/{id}")
    suspend fun updateProfile(@Path("id") userId: Int, @Body request: UpdateProfileRequest): Response<BaseResponse>

    // Đổi mật khẩu
    @PUT("change-password/{id}")
    suspend fun changePassword(@Path("id") userId: Int, @Body request: ChangePasswordRequest): Response<BaseResponse>

    // --- ADMIN APIs ---
    
    // Quản lý sản phẩm
    @POST("perfumes")
    suspend fun addPerfume(@Body perfume: Perfume): Response<BaseResponse>

    @PUT("perfumes/{id}")
    suspend fun updatePerfume(@Path("id") id: Int, @Body perfume: Perfume): Response<BaseResponse>

    @DELETE("perfumes/{id}")
    suspend fun deletePerfume(@Path("id") id: Int): Response<BaseResponse>

    // Quản lý đơn hàng
    @GET("admin/orders")
    suspend fun getAllOrders(): Response<List<Order>>

    @PUT("admin/orders/status")
    suspend fun updateOrderStatus(@Body request: UpdateOrderStatusRequest): Response<BaseResponse>

    // Quản lý khách hàng
    @GET("admin/users")
    suspend fun getAllUsers(): Response<List<UserResponse>>

    @DELETE("admin/users/{id}")
    suspend fun deleteUser(@Path("id") userId: Int): Response<BaseResponse>

    // Thống kê
    @GET("admin/revenue")
    suspend fun getRevenue(@Query("month") month: String? = null): Response<RevenueResponse>

    // Upload ảnh
    @Multipart
    @POST("upload")
    suspend fun uploadImage(@Part image: okhttp3.MultipartBody.Part): Response<UploadResponse>

    // Favorites
    @POST("favorites")
    suspend fun addFavorite(@Body request: FavoriteRequest): Response<BaseResponse>

    @GET("favorites/{userId}")
    suspend fun getFavorites(@Path("userId") userId: Int): Response<List<Perfume>>

    @DELETE("favorites/{userId}/{perfumeId}")
    suspend fun removeFavorite(@Path("userId") userId: Int, @Path("perfumeId") perfumeId: Int): Response<BaseResponse>

    // Cart
    @POST("cart/add")
    suspend fun addToCart(@Body request: AddToCartRequest): Response<BaseResponse>

    @GET("cart/{userId}")
    suspend fun getCart(@Path("userId") userId: Int): Response<List<CartItem>>

    @PUT("cart/update")
    suspend fun updateCart(@Body request: UpdateCartRequest): Response<BaseResponse>

    @DELETE("cart/{cartId}")
    suspend fun deleteCartItem(@Path("cartId") cartId: Int): Response<BaseResponse>

    @POST("checkout")
    suspend fun checkout(@Body request: OrderRequest): Response<OrderResponse>

    @GET("orders/user/{userId}")
    suspend fun getUserOrders(
        @Path("userId") userId: Int,
        @Query("status") status: String? = null
    ): Response<List<Order>>

    // Reviews
    @POST("reviews")
    suspend fun addReview(@Body request: ReviewRequest): Response<BaseResponse>

    @GET("reviews/{perfumeId}")
    suspend fun getPerfumeReviews(@Path("perfumeId") perfumeId: Int): Response<List<Review>>
}
