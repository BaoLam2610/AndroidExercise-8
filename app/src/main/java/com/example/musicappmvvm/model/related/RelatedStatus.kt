package com.example.musicappmvvm.model.related

data class RelatedStatus(
    val `data`: Data,
    val err: Int,
    val msg: String,
    val timestamp: Long
)