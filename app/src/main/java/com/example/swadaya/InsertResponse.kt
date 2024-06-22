package com.example.swadaya

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class InsertResponse {
    @SerializedName("status")
    val status: Int = 0
    @SerializedName("message")
    val message: String = ""
}