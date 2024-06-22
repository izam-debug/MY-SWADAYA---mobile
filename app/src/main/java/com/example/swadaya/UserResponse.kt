package com.example.swadaya

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class UserResponse {
    @SerializedName("data")
    @Expose
    var data: User? = null

    class User {
        @SerializedName("username")
        @Expose
        var username: String? = null

        @SerializedName("name")
        @Expose
        var name: String? = null

        @SerializedName("token")
        @Expose
        var token: String? = null

        @SerializedName("id")
        @Expose
        var id: Int? = null
    }
}