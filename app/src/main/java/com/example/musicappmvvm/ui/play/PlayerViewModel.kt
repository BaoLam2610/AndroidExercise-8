package com.example.musicappmvvm.ui.play

import android.app.Application
import android.database.Cursor
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.musicappmvvm.model.SongItem
import com.example.musicappmvvm.model.related.RelatedStatus
import com.example.musicappmvvm.repository.PlayerRepository
import com.example.musicappmvvm.ui.play.PlayerActivity.Companion.songList
import com.example.musicappmvvm.utils.Constants
import com.example.musicappmvvm.utils.Constants.EXTRA_MY_SONG
import com.example.musicappmvvm.utils.Constants.EXTRA_SONG_FAVORITE
import com.example.musicappmvvm.utils.Constants.hasInternetConnection
import com.example.musicappmvvm.utils.Constants.toSongItem
import com.example.musicappmvvm.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class PlayerViewModel(
    private val app: Application,
    private val playerRepository: PlayerRepository
) : AndroidViewModel(app) {

    companion object {
       var mFavoriteSongs =  mutableListOf<SongItem>()
    }

    val relatedSongs: MutableLiveData<Resource<MutableList<SongItem>>> = MutableLiveData()
    val favoriteSongs = playerRepository.getAllFavSong()


    fun getSongRelated(id: String, type: String) = viewModelScope.launch(Dispatchers.IO) {
        relatedSongs.postValue(Resource.Loading())
        try {
            when (type) {
                EXTRA_MY_SONG -> relatedSongs.postValue(getMySongList())
                EXTRA_SONG_FAVORITE -> {
//                    songList = mFavoriteSongs
                    relatedSongs.postValue(Resource.Success(mFavoriteSongs))
                }
                else -> {
                    if (hasInternetConnection(this@PlayerViewModel)) {
                        val response = playerRepository.getSongRelated(id)
                        relatedSongs.postValue(handleSongRelatedResponse(response))
                    } else {
                        relatedSongs.postValue(Resource.Error("No internet connection"))
                    }
                }
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> relatedSongs.postValue(Resource.Error("Network failure"))
                else -> relatedSongs.postValue(Resource.Error("Conversion error"))
            }
        }
    }

    private fun handleSongRelatedResponse(response: Response<RelatedStatus>): Resource<MutableList<SongItem>> {
        if (response.isSuccessful) {
            response.body()?.let { result ->
                val data = result.data
                val relatedList = data.items
                for (item in relatedList) {
                    songList.add(toSongItem(item))
                }
                return Resource.Success(songList)
            }
        }
        return Resource.Error(response.message())
    }

    private fun getMySongList(): Resource<MutableList<SongItem>> {
        songList.clear()
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
                songList.add(song)
            }
        }
        return Resource.Success(songList)
    }

    fun checkFavorite(): Boolean {
        if (favoriteSongs.value != null) {
            for (item in favoriteSongs.value!!) {
                if (item.id == songList[PlayerActivity.position].id) {
                    return true
                }
            }
        }
        return false
    }

    fun saveFavoriteSong(song: SongItem) = viewModelScope.launch(Dispatchers.IO) {
        playerRepository.addFavSong(song)
    }

    fun unsavedFavoriteSong(song: SongItem) = viewModelScope.launch(Dispatchers.IO) {
        playerRepository.deleteFavSong(song)
    }
}