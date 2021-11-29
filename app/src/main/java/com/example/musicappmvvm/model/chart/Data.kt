package com.example.musicappmvvm.model.chart

data class Data(
    val customied: List<Customied>,
    val peak_score: Int,
    val song: List<Song>,
    val songHis: SongHis
)