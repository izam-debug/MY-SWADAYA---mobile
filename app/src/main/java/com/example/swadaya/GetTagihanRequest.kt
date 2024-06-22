package com.example.swadaya

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class GetTagihanRequest(
    @SerializedName("token")
    val token: String
)



