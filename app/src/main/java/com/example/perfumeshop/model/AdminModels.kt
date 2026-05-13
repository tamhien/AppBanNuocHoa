package com.example.perfumeshop.model

import com.google.gson.annotations.SerializedName

data class UploadResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("fileName") val fileName: String
)
