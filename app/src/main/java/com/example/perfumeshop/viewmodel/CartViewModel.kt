package com.example.perfumeshop.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.perfumeshop.api.RetrofitClient
import com.example.perfumeshop.model.AddToCartRequest
import com.example.perfumeshop.model.CartItem
import com.example.perfumeshop.model.UpdateCartRequest
import com.example.perfumeshop.utils.SessionManager
import kotlinx.coroutines.launch

class CartViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application)
    private val apiService = RetrofitClient.instance

    var cartItems by mutableStateOf<List<CartItem>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var isSelectAll by mutableStateOf(false)
        private set
    var totalPrice by mutableStateOf(0.0)
        private set

    init {
        fetchCart()
    }

    fun fetchCart() {
        val userId = sessionManager.getUserId()
        if (userId == -1) {
            cartItems = emptyList()
            return
        }
        isLoading = true
        viewModelScope.launch {
            try {
                val response = apiService.getCart(userId)
                if (response.isSuccessful) {
                    cartItems = response.body() ?: emptyList()
                    calculateTotalPrice()
                    checkIfAllSelected()
                } else {
                    errorMessage = "Lỗi tải giỏ hàng"
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun addToCart(perfumeId: Int, quantity: Int, onResult: (Boolean, String) -> Unit) {
        val userId = sessionManager.getUserId()
        if (userId == -1) {
            onResult(false, "Vui lòng đăng nhập để thêm vào giỏ hàng")
            return
        }
        viewModelScope.launch {
            try {
                val response = apiService.addToCart(AddToCartRequest(userId, perfumeId, quantity))
                if (response.isSuccessful) {
                    fetchCart()
                    onResult(true, response.body()?.message ?: "Đã thêm vào giỏ hàng")
                } else {
                    val message = when(response.code()) {
                        404 -> "Lỗi: Không tìm thấy API (Hãy khởi động lại Server Node.js)"
                        500 -> "Lỗi Server: Có lỗi xảy ra ở phía Backend"
                        else -> "Lỗi: ${response.message()}"
                    }
                    onResult(false, message)
                }
            } catch (e: Exception) {
                onResult(false, "Lỗi kết nối: ${e.message}")
            }
        }
    }

    fun updateQuantity(cartId: Int, newQuantity: Int) {
        if (newQuantity <= 0) {
            deleteItem(cartId)
            return
        }
        
        // Optimistic update
        val updatedList = cartItems.map {
            if (it.cartId == cartId) it.copy(quantity = newQuantity) else it
        }
        cartItems = updatedList
        calculateTotalPrice()

        viewModelScope.launch {
            try {
                val response = apiService.updateCart(UpdateCartRequest(cartId, newQuantity))
                if (!response.isSuccessful) {
                    fetchCart() // Revert if failed
                }
            } catch (e: Exception) {
                fetchCart() // Revert if error
            }
        }
    }

    fun deleteItem(cartId: Int) {
        // Optimistic delete
        val itemToDelete = cartItems.find { it.cartId == cartId }
        cartItems = cartItems.filter { it.cartId != cartId }
        calculateTotalPrice()
        checkIfAllSelected()

        viewModelScope.launch {
            try {
                val response = apiService.deleteCartItem(cartId)
                if (!response.isSuccessful) {
                    fetchCart() // Revert
                }
            } catch (e: Exception) {
                fetchCart() // Revert
            }
        }
    }

    fun toggleSelection(cartId: Int) {
        cartItems = cartItems.map {
            if (it.cartId == cartId) it.copy(isSelected = !it.isSelected) else it
        }
        calculateTotalPrice()
        checkIfAllSelected()
    }

    fun toggleSelectAll() {
        val newValue = !isSelectAll
        isSelectAll = newValue
        cartItems = cartItems.map { it.copy(isSelected = newValue) }
        calculateTotalPrice()
    }

    private fun checkIfAllSelected() {
        isSelectAll = cartItems.isNotEmpty() && cartItems.all { it.isSelected }
    }

    fun calculateTotalPrice() {
        totalPrice = cartItems.filter { it.isSelected }.sumOf { it.price * it.quantity }
    }

    fun getSelectedItems(): List<CartItem> {
        return cartItems.filter { it.isSelected }
    }
}
