package com.example.perfumeshop.model

import com.google.gson.annotations.SerializedName

data class Order(
    @SerializedName("order_id") val orderId: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("total_amount") val totalAmount: Double,
    @SerializedName("status") val status: String,
    @SerializedName("order_date") val orderDate: String,
    @SerializedName("recipient_name") val recipientName: String? = null,
    @SerializedName("recipient_phone") val recipientPhone: String? = null,
    @SerializedName("recipient_address") val recipientAddress: String? = null
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

data class UploadResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("fileName") val fileName: String
)
