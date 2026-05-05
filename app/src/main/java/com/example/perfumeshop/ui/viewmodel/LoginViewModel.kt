package com.example.perfumeshop.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.perfumeshop.api.RetrofitClient
import com.example.perfumeshop.model.LoginRequest
import com.example.perfumeshop.utils.HashUtils
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun onUsernameChange(newValue: String) {
        username = newValue
    }

    fun onPasswordChange(newValue: String) {
        password = newValue
    }

    fun login(onSuccess: (String) -> Unit) {
        if (username.isEmpty() || password.isEmpty()) {
            errorMessage = "Vui lòng nhập đầy đủ thông tin"
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
                    if (body?.success == true) {
                        onSuccess(body.role ?: "user")
                    } else {
                        errorMessage = body?.message ?: "Sai tên đăng nhập hoặc mật khẩu"
                    }
                } else {
                    errorMessage = "Lỗi Server (${response.code()})"
                }
            } catch (e: Exception) {
                errorMessage = "Lỗi kết nối: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}
