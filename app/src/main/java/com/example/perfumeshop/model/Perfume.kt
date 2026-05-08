package com.example.perfumeshop.model

import com.google.gson.annotations.SerializedName

data class Perfume(
    @SerializedName("perfume_id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("brand") val brand: String,
    @SerializedName("description") val description: String,
    @SerializedName("price") val price: Double,
    @SerializedName("stock_quantity") val stockQuantity: Int,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("gender") val gender: String, // Men, Women, Unisex
    @SerializedName("sold_count") val soldCount: Int = 0
)
