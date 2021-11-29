package com.example.musicappmvvm.api

import com.example.musicappmvvm.model.chart.Status
import com.example.musicappmvvm.model.filter.FilterSong
import com.example.musicappmvvm.model.related.RelatedSong
import com.example.musicappmvvm.model.related.RelatedStatus
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MusicApi {
    @GET("xhr/chart-realtime?songId=0&videoId=0&albumId=0&chart=song&time=-1")
    suspend fun getSongCharts(): Response<Status>

    @GET("xhr/recommend?type=audio")
    suspend fun getSongRelated(
        @Query("id") id: String
    ): Response<RelatedStatus>
}