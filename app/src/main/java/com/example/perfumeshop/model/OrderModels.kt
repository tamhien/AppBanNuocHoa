package com.example.perfumeshop.model

import com.google.gson.annotations.SerializedName

data class OrderRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("selected_cart_ids") val selectedCartIds: List<Int>,
    @SerializedName("total_amount") val totalAmount: Double,
    @SerializedName("payment_method") val paymentMethod: String,
    @SerializedName("recipient_name") val recipientName: String,
    @SerializedName("recipient_phone") val recipientPhone: String,
    @SerializedName("recipient_address") val recipientAddress: String,
    @SerializedName("note") val note: String?
)

data class OrderResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("order_id") val orderId: Int?
)

data class Order(
    @SerializedName("order_id") val orderId: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("total_amount") val totalAmount: Double,
    @SerializedName("status") val status: String, // Pending, Processing, Shipping, Completed, Cancelled
    @SerializedName("payment_method") val paymentMethod: String,
    @SerializedName("recipient_name") val recipientName: String,
    @SerializedName("recipient_phone") val recipientPhone: String,
    @SerializedName("recipient_address") val recipientAddress: String,
    @SerializedName("note") val note: String?,
    @SerializedName("order_date") val orderDate: String,
    @SerializedName("items") val items: List<OrderItem> = emptyList(),
    @SerializedName("full_name") val userFullName: String? = null // For Admin display
)

data class OrderItem(
    @SerializedName("detail_id") val detailId: Int,
    @SerializedName("order_id") val orderId: Int,
    @SerializedName("perfume_id") val perfumeId: Int,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("unit_price") val unitPrice: Double,
    @SerializedName("name") val name: String,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("brand") val brand: String
)

data class UpdateOrderStatusRequest(
    @SerializedName("order_id") val orderId: Int,
    @SerializedName("status") val status: String
)

data class RevenueResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("total_revenue") val totalRevenue: Double,
    @SerializedName("order_count") val orderCount: Int
)
