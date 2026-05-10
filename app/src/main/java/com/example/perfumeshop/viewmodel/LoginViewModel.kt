package com.example.perfumeshop.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.perfumeshop.api.RetrofitClient
import com.example.perfumeshop.model.LoginRequest
import com.example.perfumeshop.utils.HashUtils
import com.example.perfumeshop.utils.SessionManager
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun onUsernameChange(newValue: String) { username = newValue }
    fun onPasswordChange(newValue: String) { password = newValue }

    fun forgotPassword(email: String, onResult: (String) -> Unit) {
        if (username.isEmpty() || email.isEmpty()) {
            errorMessage = "Vui lòng nhập Tên đăng nhập và Email"
            return
        }
        isLoading = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.forgotPassword(
                    com.example.perfumeshop.model.ForgotPasswordRequest(username, email)
                )
                if (response.isSuccessful) {
                    onResult(response.body()?.message ?: "Reset thành công")
                } else {
                    errorMessage = "Thông tin không khớp"
                }
            } catch (e: Exception) {
                errorMessage = "Lỗi kết nối"
            } finally {
                isLoading = false
            }
        }
    }

    fun login(sessionManager: SessionManager, onSuccess: (String) -> Unit) {
        if (username.isEmpty() || password.isEmpty()) {
            errorMessage = "Vui lòng nhập Tên đăng nhập và Mật khẩu"
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val hashedPassword = HashUtils.sha256(password)
                val response = RetrofitClient.instance.login(LoginRequest(username, hashedPassword))

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.userId != null) {
                        // Lưu phiên đăng nhập
                        sessionManager.saveSession(
                            userId = body.userId,
                            role = body.role ?: "user",
                            fullName = body.fullName ?: ""
                        )
                        onSuccess(body.role ?: "user")
                    } else {
                        errorMessage = body?.message ?: "Sai tên đăng nhập hoặc mật khẩu"
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    errorMessage = if (errorBody != null) {
                        try {
                            val errorObj = com.google.gson.Gson().fromJson(errorBody, com.example.perfumeshop.model.AuthResponse::class.java)
                            errorObj.message
                        } catch (e: Exception) {
                            "Lỗi Server (${response.code()})"
                        }
                    } else {
                        "Lỗi Server (${response.code()})"
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Lỗi kết nối: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}
