package com.example.musicappmvvm.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.musicappmvvm.model.SongItem


@Dao
interface SongDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSong(song: SongItem): Long

    @Query("SELECT * FROM song")
    fun getAllFavoriteSong(): LiveData<List<SongItem>>

    @Query("SELECT * FROM song")
    fun getFavoriteSongs(): List<SongItem>

    @Delete
    suspend fun deleteSong(song: SongItem)


}