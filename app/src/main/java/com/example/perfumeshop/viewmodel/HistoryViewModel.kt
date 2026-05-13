package com.example.perfumeshop.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.perfumeshop.api.RetrofitClient
import com.example.perfumeshop.model.Order
import com.example.perfumeshop.utils.SessionManager
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application)
    private val apiService = RetrofitClient.instance

    var orders by mutableStateOf<List<Order>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    
    var selectedStatus by mutableStateOf("All")
        private set

    val statuses = listOf(
        "All" to "Tất cả",
        "Pending" to "Chờ xác nhận",
        "Processing" to "Đang xử lý",
        "Shipping" to "Đang giao",
        "Completed" to "Hoàn thành",
        "Cancelled" to "Đã hủy"
    )

    fun onStatusSelected(status: String) {
        selectedStatus = status
        fetchOrders()
    }

    fun fetchOrders() {
        val userId = sessionManager.getUserId()
        if (userId == -1) return

        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val response = apiService.getUserOrders(userId, if (selectedStatus == "All") null else selectedStatus)
                if (response.isSuccessful) {
                    orders = response.body() ?: emptyList()
                } else {
                    errorMessage = "Lỗi tải lịch sử đơn hàng"
                }
            } catch (e: Exception) {
                errorMessage = "Lỗi kết nối: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}
