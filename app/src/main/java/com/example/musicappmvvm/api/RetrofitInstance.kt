package com.example.musicappmvvm.api

import com.example.musicappmvvm.utils.Constants.BASE_URL
import com.example.musicappmvvm.utils.Constants.FILTER_URL
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {
    companion object {
        private val retrofit by lazy {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()
            val gson = GsonBuilder()
                .setLenient()
                .setDateFormat("dd/MM/yyyy")
                .create()
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }
        private val retrofitFilter by lazy {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()
            val gson = GsonBuilder()
                .setLenient()
                .setDateFormat("dd/MM/yyyy")
                .create()
            Retrofit.Builder()
                .baseUrl(FILTER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }
        val api: MusicApi by lazy {
            retrofit.create(MusicApi::class.java)
        }
        val apiFilter: FilterApi by lazy {
            retrofitFilter.create(FilterApi::class.java)
        }
    }
}