package com.example.musicappmvvm.ui.play

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.musicappmvvm.repository.PlayerRepository

class PlayerViewModelProviderFactory(
    val app: Application,
    private val playerRepository: PlayerRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PlayerViewModel(app, playerRepository) as T
    }
}