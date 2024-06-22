package com.example.swadaya

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface UpdateApi {
    @POST("api/update-tagihan")
    fun updateTagihan(@Body request: UpdateTagihanRequest): Call<UpdateTagihanResponse>
}