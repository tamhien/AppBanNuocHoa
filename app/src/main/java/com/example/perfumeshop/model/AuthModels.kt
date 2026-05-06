package com.example.perfumeshop.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("address") val address: String
)

data class AuthResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("user_id") val userId: Int?,
    @SerializedName("role") val role: String?,
    @SerializedName("full_name") val fullName: String?
)

// Thêm các models mới để giải quyết lỗi Unresolved reference

data class BaseResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)

data class UserResponse(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("username") val username: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("address") val address: String,
    @SerializedName("role") val role: String
)

data class UpdateProfileRequest(
    @SerializedName("full_name") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("address") val address: String
)

data class ChangePasswordRequest(
    @SerializedName("current_password") val currentPassword: String,
    @SerializedName("new_password") val newPassword: String
)
