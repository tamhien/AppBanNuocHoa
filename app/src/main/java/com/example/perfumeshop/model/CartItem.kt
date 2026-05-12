package com.example.perfumeshop.model

import com.google.gson.annotations.SerializedName

data class CartItem(
    @SerializedName("cart_id") val cartId: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("perfume_id") val perfumeId: Int,
    @SerializedName("quantity") var quantity: Int,
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Double,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("brand") val brand: String,
    var isSelected: Boolean = false // UI state
)

data class AddToCartRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("perfume_id") val perfumeId: Int,
    @SerializedName("quantity") val quantity: Int
)

data class UpdateCartRequest(
    @SerializedName("cart_id") val cartId: Int,
    @SerializedName("quantity") val quantity: Int
)
