package com.example.perfumeshop.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.perfumeshop.api.RetrofitClient
import com.example.perfumeshop.model.RegisterRequest
import com.example.perfumeshop.utils.HashUtils
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    var username by mutableStateOf("")
    var fullName by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var phone by mutableStateOf("")
    var address by mutableStateOf("")
    
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun onUsernameChange(newValue: String) { username = newValue }
    fun onFullNameChange(newValue: String) { fullName = newValue }
    fun onEmailChange(newValue: String) { email = newValue }
    fun onPasswordChange(newValue: String) { password = newValue }
    fun onPhoneChange(newValue: String) { phone = newValue }
    fun onAddressChange(newValue: String) { address = newValue }

    fun register(onSuccess: () -> Unit) {
        if (username.isEmpty() || fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            errorMessage = "Vui lòng nhập đầy đủ thông tin bắt buộc"
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val hashedPassword = HashUtils.sha256(password)
                val request = RegisterRequest(username, hashedPassword, fullName, email, phone, address)
                val response = RetrofitClient.instance.register(request)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        onSuccess()
                    } else {
                        errorMessage = body?.message ?: "Đăng ký thất bại"
                    }
                } else {
                    // Cố gắng lấy thông báo lỗi từ body của response lỗi (ví dụ lỗi 500)
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
