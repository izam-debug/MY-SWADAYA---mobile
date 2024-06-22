package com.example.swadaya

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class UpdateTagihanRequest(
    @SerializedName("token") val token: String,
    @SerializedName("id") val id: Int,
    @SerializedName("kode_client") val kodeClient: String,
    @SerializedName("nomor_meter") val nomorMeter: Int
)



