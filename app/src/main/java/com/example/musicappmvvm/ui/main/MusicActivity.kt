package com.example.musicappmvvm.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.musicappmvvm.R
import com.example.musicappmvvm.databinding.ActivityMusicBinding
import com.example.musicappmvvm.db.SongDatabase
import com.example.musicappmvvm.repository.MusicRepository
import com.example.musicappmvvm.ui.play.PlayerActivity.Companion.isPlaying
import com.example.musicappmvvm.ui.play.PlayerActivity.Companion.musicService
import kotlin.system.exitProcess

class MusicActivity : AppCompatActivity() {

    lateinit var binding: ActivityMusicBinding
    lateinit var viewModel: MusicViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMusicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermission()

        val repository = MusicRepository(SongDatabase.getDatabase(this))
        val providerFactory = MusicViewModelProviderFactory(application, repository)
        viewModel = ViewModelProvider(this, providerFactory)[MusicViewModel::class.java]

        val navHost =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHost.navController
        binding.bottomNavigationView.setupWithNavController(navController)
    }

    override fun onResume() {
        super.onResume()
        if (musicService != null) {
            binding.fragmentNowPlaying.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isPlaying && musicService != null) {
            musicService?.stopForeground(true)
            musicService!!.mediaPlayer!!.release()
            musicService = null
            exitProcess(1)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val dialog = AlertDialog.Builder(this)
            .setTitle("Exit")
            .setMessage("Do you want to exit?")
            .setPositiveButton("Yes") { _, _ ->
                if (musicService != null) {
                    musicService!!.stopForeground(true)
                    musicService!!.mediaPlayer!!.release()
                    musicService = null
                    exitProcess(1)
                }
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog.show()
    }

    private fun checkPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                123
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    123
                )
            }
        }
    }
}