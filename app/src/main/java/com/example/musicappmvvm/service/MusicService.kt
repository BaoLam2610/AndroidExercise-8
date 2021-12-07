package com.example.musicappmvvm.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.*
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.example.musicappmvvm.R
import com.example.musicappmvvm.receiver.NotificationReceiver
import com.example.musicappmvvm.ui.play.PlayerActivity
import com.example.musicappmvvm.ui.play.PlayerActivity.Companion.musicService
import com.example.musicappmvvm.ui.play.PlayerActivity.Companion.position
import com.example.musicappmvvm.ui.play.PlayerActivity.Companion.songList
import com.example.musicappmvvm.utils.Constants
import com.example.musicappmvvm.utils.Constants.CHANNEL_ID
import com.example.musicappmvvm.utils.Constants.CLOSE
import com.example.musicappmvvm.utils.Constants.CURRENT_SONG
import com.example.musicappmvvm.utils.Constants.EXTRA_SONG_NOW_PLAYING
import com.example.musicappmvvm.utils.Constants.EXTRA_TYPE
import com.example.musicappmvvm.utils.Constants.NEXT_SONG
import com.example.musicappmvvm.utils.Constants.PLAY_PAUSE_SONG
import com.example.musicappmvvm.utils.Constants.PREV_SONG
import com.example.musicappmvvm.utils.Constants.formattedTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MusicService : Service() {
    private val TAG = "MusicService"
    private var myBinder = MyBinder()
    var mediaPlayer: MediaPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var runnable: Runnable

    override fun onBind(p0: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext, "My Music")
        return myBinder
    }

    inner class MyBinder : Binder() {
        fun currentService(): MusicService {
            return this@MusicService
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun showNotification(btnPlayPause: Int, playBackSpeed: Float) {
        // handle click notif
        val intent = Intent(baseContext, PlayerActivity::class.java)
        intent.putExtra(EXTRA_SONG_NOW_PLAYING, songList[position].id)
        intent.putExtra(EXTRA_TYPE, CURRENT_SONG)
        val pendingIntent =
            PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_IMMUTABLE)

        // prev
        val prevIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(PREV_SONG)
        val prevPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            prevIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        // play
        val playIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(
            PLAY_PAUSE_SONG
        )
        val playPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            playIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // next
        val nextIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(NEXT_SONG)
        val nextPendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            nextIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        // close
        val closeIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(CLOSE)
        val closePendingIntent = PendingIntent.getBroadcast(
            baseContext,
            0,
            closeIntent,
            PendingIntent.FLAG_IMMUTABLE
        )


        val song = songList[position]
        GlobalScope.launch(Dispatchers.Default) {
            var bitmap = if (song.thumbnail != null)
                try {
                    if (song.online)
                        Glide.with(applicationContext).asBitmap()
                            .load(song.thumbnail).submit().get()
                    else
                        Constants.getImageSongFromPath(song.thumbnail)
                } catch (e: Exception) {
                    null
                }
            else
                BitmapFactory.decodeResource(resources, R.drawable.skittle_chan)
            withContext(Dispatchers.Main) {
                val notification = NotificationCompat.Builder(baseContext, CHANNEL_ID)
                    .setContentTitle(song.name)
                    .setContentText(song.artists_names)
                    .setSmallIcon(R.drawable.ic_music)
                    .setLargeIcon(bitmap)
//            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
//                .setMediaSession(mediaSession.sessionToken))//androidx.media.app.NotificationCompat
                    .setStyle(
                        androidx.media.app.NotificationCompat.MediaStyle()
                            .setMediaSession(mediaSession.sessionToken)
                            .setShowActionsInCompactView(0, 1, 2)
                    )
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setOnlyAlertOnce(true)
                    .addAction(R.drawable.ic_skip_previous, "Previous", prevPendingIntent)
                    .addAction(btnPlayPause, "Play", playPendingIntent)
                    .addAction(R.drawable.ic_skip_next, "Next", nextPendingIntent)
                    .addAction(R.drawable.ic_close, "Exit", closePendingIntent)
                    .setContentIntent(pendingIntent)
                    .build()

                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    mediaSession.setMetadata(
                        MediaMetadataCompat.Builder()
                            .putLong(
                                MediaMetadataCompat.METADATA_KEY_DURATION,
                                mediaPlayer!!.duration.toLong()
                            )
                            .build()
                    )
                    mediaSession.setPlaybackState(
                        PlaybackStateCompat.Builder()
                            .setState(
                                PlaybackStateCompat.STATE_PLAYING,
                                mediaPlayer!!.currentPosition.toLong(),
                                playBackSpeed
                            )
                            .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                            .build()
                    )
                }*/

                startForeground(13, notification)
            }
        }

    }

    fun setupSeekBarWithSong() {
        runnable = Runnable {
            PlayerActivity.binding.tvDurationPlayed.text =
                formattedTime(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.seekBar.progress = mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
    }

    fun createMusicPlayer() {
        try {
            val song = songList[position]
            if (musicService!!.mediaPlayer == null)
                musicService!!.mediaPlayer = MediaPlayer()
            musicService?.mediaPlayer?.reset()
            musicService?.mediaPlayer?.setDataSource("http://api.mp3.zing.vn/api/streaming/${song.type}/${song.id}/128")
            musicService?.mediaPlayer?.prepare()
            musicService!!.showNotification(R.drawable.ic_pause, 0F)

            PlayerActivity.binding.tvDurationPlayed.text =
                formattedTime(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.tvDurationTotal.text =
                formattedTime(mediaPlayer!!.duration.toLong())

            PlayerActivity.binding.seekBar.progress = 0
            PlayerActivity.binding.seekBar.max = mediaPlayer!!.duration
            PlayerActivity.nowPlayingSong = songList[position].id
        } catch (e: Exception) {
            return
        }
    }
}