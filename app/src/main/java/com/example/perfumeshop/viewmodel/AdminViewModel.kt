package com.example.perfumeshop.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.perfumeshop.api.RetrofitClient
import com.example.perfumeshop.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class AdminViewModel : ViewModel() {
    private val apiService = RetrofitClient.instance

    private val _perfumes = MutableStateFlow<List<Perfume>>(emptyList())
    val perfumes: StateFlow<List<Perfume>> = _perfumes

    private val _users = MutableStateFlow<List<UserResponse>>(emptyList())
    val users: StateFlow<List<UserResponse>> = _users

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init { 
        loadPerfumes()
        loadUsers()
        loadOrders()
    }

    fun loadPerfumes() {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = apiService.getAllPerfumes()
                if (response.isSuccessful) {
                    _perfumes.value = response.body() ?: emptyList()
                } else { errorMessage = "Lỗi tải SP: ${response.code()}" }
            } catch (e: Exception) { errorMessage = e.message }
            finally { isLoading = false }
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = apiService.getAllUsers()
                if (response.isSuccessful) {
                    _users.value = response.body() ?: emptyList()
                } else { errorMessage = "Lỗi tải khách hàng: ${response.code()}" }
            } catch (e: Exception) { errorMessage = e.message }
            finally { isLoading = false }
        }
    }

    fun loadOrders() {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = apiService.getAllOrders()
                if (response.isSuccessful) {
                    _orders.value = response.body() ?: emptyList()
                } else {
                    errorMessage = "Lỗi tải đơn hàng: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Lỗi kết nối: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun updateOrderStatus(orderId: Int, status: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.updateOrderStatus(UpdateOrderStatusRequest(orderId, status))
                if (response.isSuccessful) {
                    loadOrders()
                    onResult(true, "Cập nhật trạng thái thành công")
                } else {
                    onResult(false, "Lỗi cập nhật")
                }
            } catch (e: Exception) {
                onResult(false, "Lỗi kết nối: ${e.message}")
            }
        }
    }

    // Xử lý upload ảnh từ Uri sang Server
    fun uploadImage(context: Context, uri: Uri, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val file = uriToFile(context, uri)
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
                
                val response = apiService.uploadImage(body)
                if (response.isSuccessful) {
                    onResult(response.body()?.imageUrl)
                } else {
                    errorMessage = "Lỗi upload: ${response.code()}"
                    onResult(null)
                }
            } catch (e: Exception) {
                errorMessage = "Lỗi kết nối: ${e.localizedMessage}"
                onResult(null)
            }
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return file
    }

    fun addPerfume(perfume: Perfume, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.addPerfume(perfume)
                if (response.isSuccessful) { loadPerfumes(); onSuccess() }
                else { errorMessage = "Không thể lưu SP" }
            } catch (e: Exception) { errorMessage = e.message }
        }
    }

    fun updatePerfume(id: Int, perfume: Perfume, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.updatePerfume(id, perfume)
                if (response.isSuccessful) { loadPerfumes(); onSuccess() }
                else { errorMessage = "Không thể cập nhật" }
            } catch (e: Exception) { errorMessage = e.message }
        }
    }

    fun deletePerfume(id: Int) {
        viewModelScope.launch {
            try {
                val response = apiService.deletePerfume(id)
                if (response.isSuccessful) loadPerfumes()
            } catch (e: Exception) { errorMessage = e.message }
        }
    }

    fun deleteUser(userId: Int) {
        viewModelScope.launch {
            try {
                val response = apiService.deleteUser(userId)
                if (response.isSuccessful) loadUsers()
                else { errorMessage = "Không thể xóa người dùng" }
            } catch (e: Exception) { errorMessage = e.message }
        }
    }

    fun clearError() {
        errorMessage = null
    }
}
