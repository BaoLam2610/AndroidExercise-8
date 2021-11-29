package com.example.musicappmvvm.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import com.example.musicappmvvm.MusicApplication
import com.example.musicappmvvm.model.SongItem
import com.example.musicappmvvm.model.chart.Song
import com.example.musicappmvvm.model.filter.FilterSong
import com.example.musicappmvvm.model.related.RelatedSong
import com.example.musicappmvvm.shared.SharedInstance
import com.example.musicappmvvm.ui.play.PlayerActivity.Companion.position
import com.example.musicappmvvm.ui.play.PlayerActivity.Companion.songList
import java.util.concurrent.TimeUnit

object Constants {
    const val BASE_URL = "http://mp3.zing.vn/"
    const val FILTER_URL =
        "http://ac.mp3.zing.vn/"//complete?type=artist,song,key,code&num=500&query="
    const val MUSIC_SHARED_PREFERENCES = "music_sp"
    const val SHARED_PREF_SHUFFLE = "sp_shuffle"
    const val SHARED_PREF_REPEAT_ONE = "sp_repeat_one"
    const val SHARED_PREF_REPEAT_ALL = "sp_repeat_all"
    const val EXTRA_SONG_CHART = "song_chart"
    const val EXTRA_SONG_FAVORITE = "song_favorite"
    const val EXTRA_SONG_FILTER = "song_filter"
    const val EXTRA_MY_SONG = "my_song"
    const val EXTRA_SONG_NOW_PLAYING = "song_now_playing"
    const val EXTRA_TYPE = "type"
    const val CURRENT_SONG = "current_song"
    const val CHANNEL_ID = "channel_1"
    const val PLAY_PAUSE_SONG = "play_pause"
    const val NEXT_SONG = "next"
    const val PREV_SONG = "prev"
    const val CLOSE = "close"
    const val BUNDLE_SONG = "song"
    const val DB_NAME = "song_db"

    const val CHART = 0
    const val RELATED = 1
    const val FAVORITE = 2
    const val MY_SONG = 3

    const val SEARCH_SONG_TIME_DELAY = 200L

    fun formattedTime(duration: Long): String {
        val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
        val seconds = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) -
                minutes * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun getImageSongFromPath(path: String): Bitmap? {
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(path)
        val albumImage = mmr.embeddedPicture
        if (albumImage != null) {
            return BitmapFactory.decodeByteArray(albumImage, 0, albumImage.size)
        }
        return null
    }

    fun hasInternetConnection(model: AndroidViewModel): Boolean {
        val connectivityManager = model.getApplication<MusicApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_MOBILE -> true
                    ConnectivityManager.TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }

    fun setSongPosition(increment: Boolean, context: Context) {
        val isShuffle =
            SharedInstance.getSharedPref(context).getBoolean(SHARED_PREF_SHUFFLE, false)
        if(!isShuffle) {
            if (increment) {
                if (position == songList.size - 1)
                    position = 0
                else position++
            } else {
                if (position == 0)
                    position = songList.size - 1
                else position--
            }
        } else {
            var tempPos = position
            do {
                position = (0 until songList.size).random()
            } while (tempPos == position)
        }
    }

    fun toSongItem(songChart: Song): SongItem {
        return SongItem(
            songChart.id,
            songChart.name,
            songChart.artists_names,
            if (songChart.album != null) songChart.album.name else "Không có",
            songChart.thumbnail,
            songChart.type,
            songChart.duration,
            true
        )
    }

    fun toSongItem(item: RelatedSong): SongItem {
        return SongItem(
            item.id,
            item.name,
            item.artists_names,
            "Không có",
            item.thumbnail,
            item.type,
            item.duration,
            true
        )
    }

    fun toSongItem(filter: FilterSong): SongItem {
        return SongItem(
            filter.id,
            filter.name,
            filter.artist,
            "Không có",
            "https://photo-resize-zmp3.zadn.vn/w94_r1x1_jpeg/${filter.thumb}",//filter.thumb,
            "audio",//type
            filter.duration.toInt(),
            true
        )
    }
}