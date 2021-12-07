package com.example.musicappmvvm.ui.main.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.musicappmvvm.R
import com.example.musicappmvvm.databinding.FragmentNowPlayingBinding
import com.example.musicappmvvm.ui.main.fragments.ChartFragment.Companion.chartAdapter
import com.example.musicappmvvm.ui.main.fragments.ChartFragment.Companion.filterAdapter
import com.example.musicappmvvm.ui.main.fragments.FavoriteFragment.Companion.favAdapter
import com.example.musicappmvvm.ui.main.fragments.MySongFragment.Companion.mySongAdapter
import com.example.musicappmvvm.ui.play.PlayerActivity
import com.example.musicappmvvm.ui.play.PlayerActivity.Companion.isPlaying
import com.example.musicappmvvm.ui.play.PlayerActivity.Companion.musicService
import com.example.musicappmvvm.ui.play.PlayerActivity.Companion.position
import com.example.musicappmvvm.ui.play.PlayerActivity.Companion.songList
import com.example.musicappmvvm.utils.Constants.EXTRA_SONG_NOW_PLAYING
import com.example.musicappmvvm.utils.Constants.EXTRA_TYPE
import com.example.musicappmvvm.utils.Constants.getImageSongFromPath
import com.example.musicappmvvm.utils.Constants.setSongPosition

class NowPlayingFragment : Fragment() {

    companion object {
        val TAG = "NowPlayingFragment"
        lateinit var binding: FragmentNowPlayingBinding
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNowPlayingBinding.inflate(inflater, container, false)
        binding.root.visibility = View.GONE
        binding.btnPlayAndPause.setOnClickListener {
            if (isPlaying) pauseSong()
            else playSong()
        }
        binding.btnNext.setOnClickListener {
            setSongPosition(true, requireContext())
            musicService!!.createMusicPlayer()
            setSongBottomUI()
            musicService!!.showNotification(R.drawable.ic_pause, 1F)
            playSong()
            chartAdapter.notifyDataSetChanged()
            filterAdapter.notifyDataSetChanged()
            favAdapter?.notifyDataSetChanged()
            mySongAdapter?.notifyDataSetChanged()
        }

        binding.btnPrevious.setOnClickListener {
            setSongPosition(false, requireContext())
            musicService!!.createMusicPlayer()
            setSongBottomUI()
            musicService!!.showNotification(R.drawable.ic_pause, 1F)
            playSong()
            chartAdapter.notifyDataSetChanged()
            filterAdapter.notifyDataSetChanged()
            favAdapter?.notifyDataSetChanged()
            mySongAdapter?.notifyDataSetChanged()
        }

        binding.root.setOnClickListener {
            Intent(requireContext(), PlayerActivity::class.java).also {
                it.putExtra(EXTRA_SONG_NOW_PLAYING, songList[position])
                it.putExtra(EXTRA_TYPE, TAG)
                startActivity(it)
            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (musicService != null) {
            binding.root.visibility = View.VISIBLE
            binding.tvTitle.isSelected = true
            binding.tvArtist.isSelected = true
            setSongBottomUI()
        }
    }

    private fun setSongBottomUI() {
        if (songList[position].thumbnail != null)
            if (songList[position].online)
                Glide.with(requireContext()).load(songList[position].thumbnail).into(binding.ivSong)
            else
                binding.ivSong.setImageBitmap(getImageSongFromPath(songList[position].thumbnail!!))
        else
            binding.ivSong.setImageResource(R.drawable.skittle_chan)
        binding.tvTitle.text = songList[position].name
        binding.tvArtist.text = songList[position].artists_names

        binding.btnPlayAndPause.setImageResource(
            if (isPlaying)
                R.drawable.ic_pause
            else
                R.drawable.ic_play_arrow
        )
    }

    private fun playSong() {
        musicService!!.mediaPlayer!!.start()
        binding.btnPlayAndPause.setImageResource(R.drawable.ic_pause)
        musicService!!.showNotification(R.drawable.ic_pause, 1F)
        isPlaying = true
    }

    private fun pauseSong() {
        musicService!!.mediaPlayer!!.pause()
        binding.btnPlayAndPause.setImageResource(R.drawable.ic_play_arrow)
        musicService!!.showNotification(R.drawable.ic_play_arrow, 0F)
        isPlaying = false
    }
}