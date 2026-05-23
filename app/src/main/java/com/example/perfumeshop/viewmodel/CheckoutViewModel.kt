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
                        recipientName = it.fullName ?: ""
                        recipientPhone = it.phone ?: ""
                        recipientAddress = it.address ?: ""
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
                
                if (response.isSuccessful && result != null && result.success) {
                    val orderId = result.orderId ?: 0
                    lastOrderId = orderId
                    
                    if (paymentMethod == "Thanh toán qua VNPay") {
                        val vnpayResponse = apiService.createVnpayUrl(com.example.perfumeshop.model.VnpayRequest(orderId, totalAmount))
                        if (vnpayResponse.isSuccessful && vnpayResponse.body()?.success == true) {
                            isLoading = false
                            vnpayUrl = vnpayResponse.body()?.url
                        } else {
                            isLoading = false
                            errorMessage = vnpayResponse.body()?.message ?: "Không thể tạo liên kết thanh toán VNPay"
                        }
                    } else {
                        isLoading = false
                        onSuccess(orderId)
                    }
                } else {
                    isLoading = false
                    errorMessage = result?.message ?: "Thanh toán thất bại: Lỗi hệ thống"
                }
            } catch (e: Exception) {
                errorMessage = "Lỗi mạng: ${e.message}"
                isLoading = false
            }
        }
    }

    var vnpayUrl by mutableStateOf<String?>(null)
    var lastOrderId by mutableStateOf(0)

    private fun validateInfo(): Boolean {
        if (recipientName.trim().isBlank()) {
            errorMessage = "Họ tên người nhận không được để trống"
            return false
        }
        if (recipientPhone.trim().isBlank()) {
            errorMessage = "Số điện thoại không được để trống"
            return false
        }
        val phoneRegex = "^[0-9]{10,11}$".toRegex()
        if (!phoneRegex.matches(recipientPhone.trim())) {
            errorMessage = "Số điện thoại không hợp lệ (10-11 chữ số)"
            return false
        }
        if (recipientAddress.trim().isBlank()) {
            errorMessage = "Địa chỉ giao hàng không được để trống"
            return false
        }
        if (checkoutItems.isEmpty()) {
            errorMessage = "Giỏ hàng trống"
            return false
        }
        if (totalAmount <= 0) {
            errorMessage = "Tổng số tiền không hợp lệ"
            return false
        }
        return true
    }
}
