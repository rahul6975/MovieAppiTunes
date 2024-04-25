package com.rahul.movieappitunes.network

import com.google.gson.GsonBuilder
import com.rahul.movieappitunes.utils.ApiConstants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * A singleton implementation of the API.
 */
object Network {

    private val httpLoggingInterceptor =
        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    private val client =
        OkHttpClient.Builder().addInterceptor(httpLoggingInterceptor).build()
    val service: ApiService = Retrofit.Builder()
        .baseUrl(ApiConstants.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build().create(ApiService::class.java)
}
