package com.example.perfumeshop.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.perfumeshop.api.RetrofitClient
import com.example.perfumeshop.model.CartItem
import com.example.perfumeshop.model.OrderRequest
import com.example.perfumeshop.utils.SessionManager
import kotlinx.coroutines.launch

class CheckoutViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application)
    private val apiService = RetrofitClient.instance

    var checkoutItems by mutableStateOf<List<CartItem>>(emptyList())
    
    var recipientName by mutableStateOf("")
    var recipientPhone by mutableStateOf("")
    var recipientAddress by mutableStateOf("")
    var note by mutableStateOf("")
    var paymentMethod by mutableStateOf("Thanh toán khi nhận hàng (COD)")

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    val totalAmount: Double
        get() = checkoutItems.sumOf { it.price * it.quantity }

    val totalQuantity: Int
        get() = checkoutItems.sumOf { it.quantity }

    init {
        fetchUserInfo()
    }

    private fun fetchUserInfo() {
        val userId = sessionManager.getUserId()
        if (userId == -1) return
        
        viewModelScope.launch {
            try {
                val response = apiService.getProfile(userId)
                if (response.isSuccessful) {
                    val profile = response.body()
                    profile?.let {
                        recipientName = it.fullName
                        recipientPhone = it.phone
                        recipientAddress = it.address
                    }
                }
            } catch (e: Exception) {
                // Ignore auto-fill error
            }
        }
    }

    fun placeOrder(onSuccess: (Int) -> Unit) {
        if (!validateInfo()) return

        val userId = sessionManager.getUserId()
        if (userId == -1) {
            errorMessage = "Vui lòng đăng nhập lại"
            return
        }

        if (checkoutItems.isEmpty()) {
            errorMessage = "Không có sản phẩm để thanh toán"
            return
        }

        isLoading = true
        errorMessage = null

        val request = OrderRequest(
            userId = userId,
            selectedCartIds = checkoutItems.map { it.cartId },
            totalAmount = totalAmount,
            paymentMethod = paymentMethod,
            recipientName = recipientName,
            recipientPhone = recipientPhone,
            recipientAddress = recipientAddress,
            note = note.ifBlank { null }
        )

        viewModelScope.launch {
            try {
                val response = apiService.checkout(request)
                val result = response.body()
                
                if (response.isSuccessful && result != null) {
                    if (result.success) {
                        onSuccess(result.orderId ?: 0)
                    } else {
                        errorMessage = result.message
                    }
                } else {
                    // Xử lý khi response không thành công (ví dụ 404, 500)
                    val errorMsg = response.errorBody()?.string() ?: "Lỗi kết nối Server"
                    errorMessage = "Thanh toán thất bại: $errorMsg"
                }
            } catch (e: Exception) {
                errorMessage = "Lỗi mạng: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    private fun validateInfo(): Boolean {
        if (recipientName.isBlank()) {
            errorMessage = "Họ tên người nhận không được để trống"
            return false
        }
        if (recipientPhone.isBlank()) {
            errorMessage = "Số điện thoại không được để trống"
            return false
        }
        if (recipientAddress.isBlank()) {
            errorMessage = "Địa chỉ giao hàng không được để trống"
            return false
        }
        return true
    }
}
