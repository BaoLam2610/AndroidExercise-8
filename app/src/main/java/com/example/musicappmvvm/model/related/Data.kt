package com.example.musicappmvvm.model.related

data class Data(
    val image_url: String,
    val items: List<RelatedSong>,
    val total: Int
)