package com.example.musicappmvvm.repository

import com.example.musicappmvvm.api.RetrofitInstance
import com.example.musicappmvvm.db.SongDatabase
import com.example.musicappmvvm.model.SongItem

class MusicRepository(
    private val db: SongDatabase
) {
    // Database
    fun getFavoriteSongs() = db.songDao().getAllFavoriteSong()

    suspend fun addFavSong(songItem: SongItem) = db.songDao().addSong(songItem)

    suspend fun deleteFavSong(songItem: SongItem) = db.songDao().deleteSong(songItem)

    // Retrofit
    suspend fun getSongCharts() =
        RetrofitInstance.api.getSongCharts()

    suspend fun getSongFilters(query: String) =
        RetrofitInstance.apiFilter.getSongFilters(500, query)
}