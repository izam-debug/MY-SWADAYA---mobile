package com.example.swadaya

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface UserApi {
    @POST("api/user")
    fun login(
        @Body userRequest: UserRequest
    ): Call<UserResponse>
}