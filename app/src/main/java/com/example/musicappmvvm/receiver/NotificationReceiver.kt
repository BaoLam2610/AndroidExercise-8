package com.example.musicappmvvm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.example.musicappmvvm.R
import com.example.musicappmvvm.db.SongDatabase
import com.example.musicappmvvm.model.SongItem
import com.example.musicappmvvm.shared.SharedInstance
import com.example.musicappmvvm.ui.main.fragments.ChartFragment
import com.example.musicappmvvm.ui.main.fragments.FavoriteFragment
import com.example.musicappmvvm.ui.main.fragments.MySongFragment
import com.example.musicappmvvm.ui.main.fragments.NowPlayingFragment
import com.example.musicappmvvm.ui.play.PlayerActivity
import com.example.musicappmvvm.ui.play.PlayerActivity.Companion.isPlaying
import com.example.musicappmvvm.ui.play.PlayerActivity.Companion.musicService
import com.example.musicappmvvm.ui.play.PlayerActivity.Companion.position
import com.example.musicappmvvm.ui.play.PlayerActivity.Companion.songList
import com.example.musicappmvvm.ui.play.fragments.PlaySongFragment
import com.example.musicappmvvm.ui.play.fragments.SongInfoFragment
import com.example.musicappmvvm.utils.Constants
import com.example.musicappmvvm.utils.Constants.CLOSE
import com.example.musicappmvvm.utils.Constants.NEXT_SONG
import com.example.musicappmvvm.utils.Constants.PLAY_PAUSE_SONG
import com.example.musicappmvvm.utils.Constants.PREV_SONG
import com.example.musicappmvvm.utils.Constants.setSongPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.exitProcess

class NotificationReceiver : BroadcastReceiver() {

    var isShuffle: Boolean? = null
    var isRepeatOne: Boolean? = null
    var isRepeatAll: Boolean? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        isShuffle =
            SharedInstance.getSharedPref(context!!)
                .getBoolean(Constants.SHARED_PREF_SHUFFLE, false)
        isRepeatOne =
            SharedInstance.getSharedPref(context)
                .getBoolean(Constants.SHARED_PREF_REPEAT_ONE, false)
        isRepeatAll =
            SharedInstance.getSharedPref(context)
                .getBoolean(Constants.SHARED_PREF_REPEAT_ALL, true)

        when (intent?.action) {
            PLAY_PAUSE_SONG -> {
                if (isPlaying) pauseSong()
                else playSong()
            }
            NEXT_SONG -> prevNextSong(true, context)
            PREV_SONG -> prevNextSong(false, context)
            CLOSE -> {
                musicService!!.stopForeground(true)
                musicService!!.mediaPlayer!!.release()
                musicService = null
                exitProcess(1)
            }
        }

    }

    private fun playSong() {
        isPlaying = true
        musicService!!.mediaPlayer!!.start()
        musicService!!.showNotification(R.drawable.ic_pause, 1F)
        PlayerActivity.binding.btnPlayAndPause.setImageResource(R.drawable.ic_pause)
        NowPlayingFragment.binding.btnPlayAndPause.setImageResource(R.drawable.ic_pause)
    }

    private fun pauseSong() {
        isPlaying = false
        musicService!!.mediaPlayer!!.pause()
        musicService!!.showNotification(R.drawable.ic_play_arrow, 0F)
        PlayerActivity.binding.btnPlayAndPause.setImageResource(R.drawable.ic_play_arrow)
        NowPlayingFragment.binding.btnPlayAndPause.setImageResource(R.drawable.ic_play_arrow)
    }

    private fun prevNextSong(increment: Boolean, context: Context) {
        setSongPosition(increment, context)
        musicService!!.createMusicPlayer()
        // set layout player activity
        setSongUI(context)
        playSong()
        // set fragment
        val song = songList[position]
        if (song.thumbnail != null)
            Glide.with(context).load(song.thumbnail).into(NowPlayingFragment.binding.ivSong)
        else
            NowPlayingFragment.binding.ivSong.setImageResource(R.drawable.skittle_chan)
        NowPlayingFragment.binding.tvTitle.text = song.name
        NowPlayingFragment.binding.tvArtist.text = song.artists_names
        NowPlayingFragment.binding.btnPlayAndPause.setImageResource(R.drawable.ic_pause)
        playSong()
        ChartFragment.chartAdapter.notifyDataSetChanged()
        ChartFragment.filterAdapter.notifyDataSetChanged()
        SongInfoFragment.adapter?.notifyDataSetChanged()
        FavoriteFragment.favAdapter?.notifyDataSetChanged()
        MySongFragment.mySongAdapter?.notifyDataSetChanged()

    }

    fun checkFavorite(context: Context): Boolean {
        var songs = listOf<SongItem>()
        GlobalScope.launch(Dispatchers.IO) {
            songs = SongDatabase.getDatabase(context).songDao().getFavoriteSongs()
        }
        for (item in songs) {
            if (songList[position].id.contains(item.id))
                return true
        }
        return false
    }

    fun setSongUI(context: Context) {
        val song = songList[position]
        PlaySongFragment.binding?.tvTitle?.text = song.name.trim()
        PlaySongFragment.binding?.tvArtist?.text = song.artists_names.trim()
        PlaySongFragment.binding?.tvTitle?.isSelected = true
        PlaySongFragment.binding?.tvArtist?.isSelected = true
        var bitmap: Bitmap? = null
        PlayerActivity.binding.tvDurationTotal.text =
            Constants.formattedTime(song.duration.toLong() * 1000)
        GlobalScope.launch(Dispatchers.Default) {

            bitmap = if (song.thumbnail != null)
                try {
                    Glide.with(context)
                        .asBitmap()
                        .load(song.thumbnail)
                        .submit()
                        .get()
                } catch (e: Exception) {
                    null
                }
            else null

            withContext(Dispatchers.Main) {
                if (bitmap != null) {
                    PlaySongFragment.binding?.ivSong?.setImageBitmap(bitmap)
                    Palette.from(bitmap!!).generate {
                        val swatch = it?.dominantSwatch
                        val gradientSong: GradientDrawable
                        val gradientSong1: GradientDrawable
                        val gradientContainer: GradientDrawable
                        if (swatch != null) {
                            gradientSong =
                                GradientDrawable(
                                    GradientDrawable.Orientation.BOTTOM_TOP,
                                    intArrayOf(swatch.rgb, 0x00000000)
                                )
                            gradientSong1 =
                                GradientDrawable(
                                    GradientDrawable.Orientation.TOP_BOTTOM,
                                    intArrayOf(swatch.rgb, 0x00000000)
                                )
                            gradientContainer =
                                GradientDrawable(
                                    GradientDrawable.Orientation.BOTTOM_TOP,
                                    intArrayOf(swatch.rgb, swatch.rgb)
                                )
                            PlaySongFragment.binding?.tvTitle?.setTextColor(swatch.titleTextColor)
                            PlaySongFragment.binding?.tvArtist?.setTextColor(swatch.titleTextColor)
                        } else {
                            gradientSong =
                                GradientDrawable(
                                    GradientDrawable.Orientation.BOTTOM_TOP,
                                    intArrayOf(0xff000000.toInt(), 0x00000000)
                                )
                            gradientSong1 =
                                GradientDrawable(
                                    GradientDrawable.Orientation.TOP_BOTTOM,
                                    intArrayOf(0xff000000.toInt(), 0x00000000)
                                )
                            gradientContainer =
                                GradientDrawable(
                                    GradientDrawable.Orientation.BOTTOM_TOP,
                                    intArrayOf(0xff000000.toInt(), 0xff000000.toInt())
                                )
                            PlaySongFragment.binding?.tvTitle?.setTextColor(Color.WHITE)
                            PlaySongFragment.binding?.tvArtist?.setTextColor(Color.WHITE)
                        }
                        PlaySongFragment.binding?.ivGradientImage?.background = gradientSong
                        PlaySongFragment.binding?.ivGradientImage1?.background = gradientSong1
                        PlayerActivity.binding.clContainer.background = gradientContainer
                    }
                } else {
                    PlaySongFragment.binding?.ivSong?.setImageResource(R.drawable.unknown_song)
                    PlaySongFragment.binding?.tvTitle?.setTextColor(Color.WHITE)
                    PlaySongFragment.binding?.tvArtist?.setTextColor(Color.WHITE)
                    PlaySongFragment.binding?.ivGradientImage?.setBackgroundResource(R.drawable.custom_bgr_gradient_music_player)
                    PlaySongFragment.binding?.ivGradientImage1?.setBackgroundResource(R.drawable.custom_bgr_gradient_music_player_1)
                    PlayerActivity.binding.clContainer.setBackgroundResource(R.drawable.custom_bgr_music_player)
                }
                if (isShuffle == true) {
                    PlayerActivity.binding.btnShuffle.setImageResource(R.drawable.ic_shuffle)
                } else {
                    PlayerActivity.binding.btnShuffle.setImageResource(R.drawable.ic_shuffle_off)
                }
                if (isRepeatOne == true) {
                    PlayerActivity.binding.btnRepeat.setImageResource(R.drawable.ic_repeat_one)
                } else if (isRepeatAll == true) {
                    PlayerActivity.binding.btnRepeat.setImageResource(R.drawable.ic_repeat)
                } else if (isRepeatOne == false && isRepeatAll == false) {  // off repeat
                    PlayerActivity.binding.btnRepeat.setImageResource(R.drawable.ic_repeat_off)
                }
            }
        }
        if (checkFavorite(context)) {
            PlayerActivity.binding.btnFavorite.setImageResource(R.drawable.ic_baseline_favorite_24)
        } else {
            PlayerActivity.binding.btnFavorite.setImageResource(R.drawable.ic_favorite)
        }
        SongInfoFragment.binding?.tvName?.text = "Bài hát: ${song.name}"
        SongInfoFragment.binding?.tvArtist?.text = "Ca sỹ: ${song.artists_names}"
        SongInfoFragment.binding?.tvAlbum?.text = "Album: ${song.album}"
    }
}