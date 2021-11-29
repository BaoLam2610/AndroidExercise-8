package com.example.musicappmvvm.ui.main

import android.app.Application
import android.database.Cursor
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.musicappmvvm.model.SongItem
import com.example.musicappmvvm.model.chart.Song
import com.example.musicappmvvm.model.chart.Status
import com.example.musicappmvvm.model.filter.FilterSong
import com.example.musicappmvvm.model.filter.FilterStatus
import com.example.musicappmvvm.repository.MusicRepository
import com.example.musicappmvvm.utils.Constants.hasInternetConnection
import com.example.musicappmvvm.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class MusicViewModel(
    val app: Application,
    private val musicRepository: MusicRepository
) : AndroidViewModel(app) {

    init {
        getSongCharts()
    }

    // Get Chart
    val songCharts: MutableLiveData<Resource<List<Song>>> = MutableLiveData()
    val songFilters: MutableLiveData<Resource<List<FilterSong>>> = MutableLiveData()
    val mySongs: MutableLiveData<MutableList<SongItem>> = MutableLiveData()
    val favoriteSongs = musicRepository.getFavoriteSongs()

    fun getSongCharts() = viewModelScope.launch(Dispatchers.IO) {
        try {
            songCharts.postValue(Resource.Loading())
            if (hasInternetConnection(this@MusicViewModel)) {
                val response = musicRepository.getSongCharts()
                songCharts.postValue(handleSongChartsResponse(response))
            } else {
                songCharts.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> songCharts.postValue(Resource.Error("Network failure"))
                else -> songCharts.postValue(Resource.Error("Conversion error"))
            }
        }
    }

    fun getSongFilters(query: String) = viewModelScope.launch(Dispatchers.IO) {
        try {
            songFilters.postValue(Resource.Loading())
            if (hasInternetConnection(this@MusicViewModel)) {
                val response = musicRepository.getSongFilters(query)
                songFilters.postValue(handleSongFiltersResponse(response))
            } else {
                songFilters.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> songFilters.postValue(Resource.Error("Network failure"))
                else -> songFilters.postValue(Resource.Error("Conversion error"))
            }
        }
    }

    fun getMySongList() = viewModelScope.launch(Dispatchers.IO) {
        var mySongList = mutableListOf<SongItem>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION
        )

        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val uriExternal = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
//        val uriInternal = MediaStore.Audio.Media.INTERNAL_CONTENT_URI
        val cursor: Cursor? = app.contentResolver.query(
            uriExternal,
            projection,
            selection,
            null,
            null
        )
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val song = SongItem(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),//cursor.getString(3),// image
                    cursor.getString(4),//cursor.getString(3),// image
                    cursor.getLong(5).toInt() / 1000,
                    false
                )
                mySongList.add(song)
            }
        }
        mySongs.postValue(mySongList)
    }

    private fun handleSongChartsResponse(response: Response<Status>): Resource<List<Song>> {
        if (response.isSuccessful) {
            response.body()?.let { result ->
                val data = result.data
                val songChartList = data.song
                return Resource.Success(songChartList)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleSongFiltersResponse(response: Response<FilterStatus>): Resource<List<FilterSong>> {
        if (response.isSuccessful) {
            response.body()?.let { result ->
                val data = result.data
                val songList = data[0].song
                return Resource.Success(songList)
            }
        }
        return Resource.Error(response.message())
    }

    fun saveFavorite(songItem: SongItem) = viewModelScope.launch(Dispatchers.IO) {
        musicRepository.addFavSong(songItem)
    }

    fun unSavedFavorite(songItem: SongItem) = viewModelScope.launch(Dispatchers.IO) {
        musicRepository.deleteFavSong(songItem)
    }
}