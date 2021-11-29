package com.example.musicappmvvm.ui.main

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.musicappmvvm.repository.MusicRepository

class MusicViewModelProviderFactory(
    val app: Application,
    private val musicRepository: MusicRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MusicViewModel(app, musicRepository) as T
    }
}