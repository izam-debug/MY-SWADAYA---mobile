package com.example.swadaya


data class GetTagihanResponse(
    val data: List<Tagihan>, // Sesuaikan dengan respons API
    val message: String,
    val status: Int
)

