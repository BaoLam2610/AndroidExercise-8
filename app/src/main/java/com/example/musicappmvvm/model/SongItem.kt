package com.example.musicappmvvm.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "song")
data class SongItem(
    @PrimaryKey
    val id: String,
    val name: String,
    val artists_names: String,
    val album: String,
    val thumbnail: String?,
    val type: String,
    val duration: Int,
    val online: Boolean
) : Serializable