package com.example.swadaya

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface TagihanGetApi {
    @POST("api/get-tagihan")
    fun tagihan(@Body request: GetTagihanRequest): Call<GetTagihanResponse>
}
