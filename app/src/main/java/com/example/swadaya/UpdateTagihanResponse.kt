package com.example.swadaya

import com.google.gson.annotations.SerializedName

data class UpdateTagihanResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String
)