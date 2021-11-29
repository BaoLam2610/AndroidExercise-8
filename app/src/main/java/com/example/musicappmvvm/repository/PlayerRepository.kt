package com.example.musicappmvvm.repository

import com.example.musicappmvvm.api.RetrofitInstance
import com.example.musicappmvvm.db.SongDatabase
import com.example.musicappmvvm.model.SongItem

class PlayerRepository(
    private val db: SongDatabase){

    suspend fun addFavSong(songItem: SongItem) = db.songDao().addSong(songItem)

    suspend fun deleteFavSong(songItem: SongItem) = db.songDao().deleteSong(songItem)

    fun getAllFavSong() = db.songDao().getAllFavoriteSong()

    suspend fun getSongRelated(id: String) = RetrofitInstance.api.getSongRelated(id)
}