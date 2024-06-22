package com.example.swadaya

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {

    @FormUrlEncoded
    @POST("/api/insert-data-tagihan")
    fun sendData(
        @Field("token") token: String,
        @Field("kode_client") scanResult: String,
        @Field("nomor_meter") number: String,
        @Field("id_petugas") userID: String
    ): Call<InsertResponse>
}