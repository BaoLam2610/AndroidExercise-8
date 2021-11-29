package com.example.musicappmvvm.api

import com.example.musicappmvvm.model.filter.FilterStatus
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface FilterApi {
    @GET("complete?type=artist,song,key,code")
    suspend fun getSongFilters(
        @Query("num") num: Int,
        @Query("query") filter: String
    ): Response<FilterStatus>
}