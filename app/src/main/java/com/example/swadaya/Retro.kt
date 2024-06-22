package com.example.swadaya

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Retro {
    fun getRetroClientInstance(): Retrofit {
        val logging = HttpLoggingInterceptor ()
        logging.setLevel((HttpLoggingInterceptor.Level.BODY))
        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(logging)

        val gson = GsonBuilder().setLenient().create()
        return Retrofit.Builder()
            .baseUrl("https://admin.swadaya.my.id/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(httpClient.build())
            .build()
    }
}