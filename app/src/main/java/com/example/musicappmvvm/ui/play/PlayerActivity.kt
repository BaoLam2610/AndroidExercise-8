package com.example.musicappmvvm.ui.play

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.*
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.musicappmvvm.R
import com.example.musicappmvvm.adapter.TabLayoutAdapter
import com.example.musicappmvvm.databinding.ActivityPlayerBinding
import com.example.musicappmvvm.db.SongDatabase
import com.example.musicappmvvm.model.SongItem
import com.example.musicappmvvm.model.chart.Song
import com.example.musicappmvvm.model.filter.FilterSong
import com.example.musicappmvvm.repository.PlayerRepository
import com.example.musicappmvvm.service.MusicService
import com.example.musicappmvvm.shared.SharedInstance
import com.example.musicappmvvm.ui.main.fragments.ChartFragment
import com.example.musicappmvvm.ui.main.fragments.FavoriteFragment
import com.example.musicappmvvm.ui.main.fragments.MySongFragment
import com.example.musicappmvvm.ui.main.fragments.NowPlayingFragment
import com.example.musicappmvvm.ui.play.fragments.PlaySongFragment
import com.example.musicappmvvm.ui.play.fragments.SongInfoFragment
import com.example.musicappmvvm.utils.Constants
import com.example.musicappmvvm.utils.Constants.CURRENT_SONG
import com.example.musicappmvvm.utils.Constants.EXTRA_MY_SONG
import com.example.musicappmvvm.utils.Constants.EXTRA_SONG_CHART
import com.example.musicappmvvm.utils.Constants.EXTRA_SONG_FAVORITE
import com.example.musicappmvvm.utils.Constants.EXTRA_SONG_FILTER
import com.example.musicappmvvm.utils.Constants.EXTRA_SONG_NOW_PLAYING
import com.example.musicappmvvm.utils.Constants.EXTRA_TYPE
import com.example.musicappmvvm.utils.Constants.SHARED_PREF_REPEAT_ALL
import com.example.musicappmvvm.utils.Constants.SHARED_PREF_REPEAT_ONE
import com.example.musicappmvvm.utils.Constants.SHARED_PREF_SHUFFLE
import com.example.musicappmvvm.utils.Constants.formattedTime
import com.example.musicappmvvm.utils.Constants.setSongPosition
import com.example.musicappmvvm.utils.Constants.toSongItem
import kotlinx.coroutines.*
import java.io.File
import java.util.*

class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {


    lateinit var tabLayoutAdapter: TabLayoutAdapter
    lateinit var viewModel: PlayerViewModel
    var getSong: SongItem? = null

    companion object {
        var musicService: MusicService? = null
        var songList = mutableListOf<SongItem>()//: MutableList<SongItem>? = null
        var position = 0
        var nowPlayingSong = ""
        lateinit var binding: ActivityPlayerBinding
        var isPlaying = false


    }
    var favoriteSongs = emptyList<SongItem>()
    var isShuffle = false
    var isRepeatOne = false
    var isRepeatAll = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val repository = PlayerRepository(SongDatabase.getDatabase(this))
        val providerFactory = PlayerViewModelProviderFactory(application, repository)
        viewModel = ViewModelProvider(this, providerFactory)[PlayerViewModel::class.java]
        isShuffle =
            SharedInstance.getSharedPref(applicationContext).getBoolean(SHARED_PREF_SHUFFLE, false)
        isRepeatOne =
            SharedInstance.getSharedPref(applicationContext)
                .getBoolean(SHARED_PREF_REPEAT_ONE, false)
        isRepeatAll =
            SharedInstance.getSharedPref(applicationContext)
                .getBoolean(SHARED_PREF_REPEAT_ALL, true)

        binding.btnBack.setOnClickListener {
            finish()
        }
        getSong = when {
            intent.hasExtra(EXTRA_SONG_CHART) -> {
                toSongItem(intent.getSerializableExtra(EXTRA_SONG_CHART) as Song)
            }
            intent.hasExtra(EXTRA_SONG_FILTER) -> {
                toSongItem(intent.getSerializableExtra(EXTRA_SONG_FILTER) as FilterSong)
            }
            intent.hasExtra(EXTRA_SONG_FAVORITE) -> {
                intent.getSerializableExtra(EXTRA_SONG_FAVORITE) as SongItem
            }
            intent.hasExtra(EXTRA_SONG_NOW_PLAYING) -> {
                intent.getSerializableExtra(EXTRA_SONG_NOW_PLAYING) as SongItem
            }
            intent.hasExtra(EXTRA_MY_SONG) -> {
                intent.getSerializableExtra(EXTRA_MY_SONG) as SongItem
            }
            else -> null
        }
        songList.clear()
        getSong?.let { songList.add(it) }

        val fragmentList = listOf(
            PlaySongFragment.newInstance(getSong),
            SongInfoFragment.newInstance(getSong)
        )
        tabLayoutAdapter = TabLayoutAdapter(supportFragmentManager, lifecycle, fragmentList)
        binding.viewPager2.adapter = tabLayoutAdapter
        binding.indicator.setViewPager2(binding.viewPager2)

        displayStatus()
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                if (fromUser)
                    musicService!!.mediaPlayer!!.seekTo(progress)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) = Unit
            override fun onStopTrackingTouch(p0: SeekBar?) = Unit
        })
        viewModel.favoriteSongs.observe(this){
            favoriteSongs = it
        }
    }

    fun displayStatus() {
        if (isShuffle) {
            binding.btnShuffle.setImageResource(R.drawable.ic_shuffle)
        } else {
            binding.btnShuffle.setImageResource(R.drawable.ic_shuffle_off)
        }
        if (isRepeatOne) {
            binding.btnRepeat.setImageResource(R.drawable.ic_repeat_one)
        } else if (isRepeatAll) {
            binding.btnRepeat.setImageResource(R.drawable.ic_repeat)
        } else if (!isRepeatOne && !isRepeatAll) {  // off repeat
            binding.btnRepeat.setImageResource(R.drawable.ic_repeat_off)
        }
    }

    fun checkService() {
        when (intent.getStringExtra(EXTRA_TYPE)) {
            ChartFragment.TAG, FavoriteFragment.TAG, MySongFragment.TAG -> {
                val it = Intent(this, MusicService::class.java)
                bindService(it, this, BIND_AUTO_CREATE)
                startService(it)
                position = 0
                setSongUI()
            }
            NowPlayingFragment.TAG, CURRENT_SONG -> {
                position = 0
                setSongUI()
                binding.tvDurationPlayed.text =
                    formattedTime(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.tvDurationPlayed.text =
                    formattedTime(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seekBar.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.seekBar.max = musicService!!.mediaPlayer!!.duration
                if (isPlaying)
                    binding.btnPlayAndPause.setImageResource(R.drawable.ic_pause)
                else
                    binding.btnPlayAndPause.setImageResource(R.drawable.ic_play_arrow)
            }
        }

    }

    private fun createMusicPlayer() {
        try {
            if (musicService!!.mediaPlayer == null)
                musicService!!.mediaPlayer = MediaPlayer()

            musicService?.mediaPlayer?.reset()

            musicService?.mediaPlayer?.setDataSource(
                if (songList[position].online)
                    "http://api.mp3.zing.vn/api/streaming/${songList[position].type}/${songList[position].id}/128"
                else
                    songList[position].type
            )
            musicService?.mediaPlayer?.prepare()
            musicService?.mediaPlayer?.start()
            isPlaying = true
            musicService!!.showNotification(R.drawable.ic_pause, 1F)
            binding.tvDurationPlayed.text =
                formattedTime(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.tvDurationTotal.text =
                formattedTime(musicService!!.mediaPlayer!!.duration.toLong())
            binding.seekBar.progress = 0
            binding.seekBar.max = musicService!!.mediaPlayer!!.duration
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
            nowPlayingSong = songList[position].id
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Bạn phải nạp tiền để chạy được bài hát này", Toast.LENGTH_SHORT)
                .show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        createMusicPlayer()
        musicService!!.setupSeekBarWithSong()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    override fun onResume() {
        super.onResume()
        checkService()
        nextSong()
        previousSong()
        playAndPauseSong()
        repeatSong()
        downloadSong()
        shuffleSong()
        saveFavorite()
    }

    private fun nextSong() {
        binding.btnNext.setOnClickListener {
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
            binding.btnPlayAndPause.setImageResource(R.drawable.ic_pause)
            setSongPosition(true, applicationContext)
            setSongUI()
            createMusicPlayer()
            SongInfoFragment.adapter?.notifyDataSetChanged()
        }
    }

    private fun previousSong() {
        binding.btnPrevious.setOnClickListener {
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
            binding.btnPlayAndPause.setImageResource(R.drawable.ic_pause)
            setSongPosition(false, applicationContext)
            setSongUI()
            createMusicPlayer()
            SongInfoFragment.adapter?.notifyDataSetChanged()
        }
    }

    private fun playAndPauseSong() {
        binding.btnPlayAndPause.setOnClickListener {
            if (isPlaying) {  // pause
                isPlaying = false
                musicService!!.showNotification(R.drawable.ic_play_arrow, 1F)
                binding.btnPlayAndPause.setImageResource(R.drawable.ic_play_arrow)
                musicService?.mediaPlayer?.pause()
            } else {
                isPlaying = true
                musicService!!.showNotification(R.drawable.ic_pause, 0F)
                binding.btnPlayAndPause.setImageResource(R.drawable.ic_pause)
                musicService?.mediaPlayer?.start()
            }
        }
    }

    fun shuffleSong() {
        binding.btnShuffle.setOnClickListener {
            if (isShuffle) {    // shuffle on
                isShuffle = false
                SharedInstance.getSharedPrefEditor(applicationContext)
                    .putBoolean(SHARED_PREF_SHUFFLE, isShuffle)?.commit()
                binding.btnShuffle.setImageResource(R.drawable.ic_shuffle_off)
            } else {            // off
                isShuffle = true
                SharedInstance.getSharedPrefEditor(applicationContext)
                    .putBoolean(SHARED_PREF_SHUFFLE, isShuffle)?.commit()

                binding.btnShuffle.setImageResource(R.drawable.ic_shuffle)
            }
        }
    }

    fun repeatSong() {
        binding.btnRepeat.setOnClickListener {
            if (isRepeatOne) {
                isRepeatOne = false
                isRepeatAll = true
                SharedInstance.getSharedPrefEditor(this).putBoolean(
                    SHARED_PREF_REPEAT_ONE,
                    isRepeatOne
                )?.commit()
                SharedInstance.getSharedPrefEditor(this).putBoolean(
                    SHARED_PREF_REPEAT_ALL,
                    isRepeatAll
                )?.commit()
                binding.btnRepeat.setImageResource(R.drawable.ic_repeat)
            } else if (isRepeatAll) {
                isRepeatAll = false
                SharedInstance.getSharedPrefEditor(this).putBoolean(
                    SHARED_PREF_REPEAT_ALL,
                    isRepeatAll
                )?.commit()
                binding.btnRepeat.setImageResource(R.drawable.ic_repeat_off)
            } else if (!isRepeatOne && !isRepeatAll) {  // off repeat
                isRepeatOne = true
                SharedInstance.getSharedPrefEditor(this).putBoolean(
                    SHARED_PREF_REPEAT_ONE,
                    isRepeatOne
                )?.commit()
                binding.btnRepeat.setImageResource(R.drawable.ic_repeat_one)
            }
        }
    }

    fun downloadSong() {
        binding.btnDownload.setOnClickListener {
            if (songList[position].online) {
                val path = Environment.getExternalStoragePublicDirectory("Music")
                val file = File("$path/${songList[position].name}.mp3")

                if (file.exists()) {
                    Toast.makeText(
                        applicationContext, "Bài này bạn đã tải rồi",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    val url =
                        "http://api.mp3.zing.vn/api/streaming/${songList[position].type}/${songList[position].id}/128"
                    val request = DownloadManager.Request(Uri.parse(url))
                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                    request.setTitle(songList[position].name)
                    request.setDescription("Download song...")
                    request.setNotificationVisibility((DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED))
                    request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_MUSIC,
                        "${songList[position].name}.mp3"//System.currentTimeMillis().toString() + ".mp3"
                    )
                    val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
                    try {
                        manager?.enqueue(request)
                        Toast.makeText(this, "Tải thành công", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
                }
            } else {
                Toast.makeText(this, "Bài này bạn đã tải rồi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun checkFavorite(): Boolean  {
       for(item in favoriteSongs){
           if(songList[position].id == item.id)
               return true
       }
        return false
    }

    fun saveFavorite() {
        binding.btnFavorite.setOnClickListener {
            if (!checkFavorite()) {
                viewModel.saveFavoriteSong(songList[position])
                binding.btnFavorite.setImageResource(R.drawable.ic_baseline_favorite_24)
                Toast.makeText(
                    this,
                    "Đã thêm bài hát vào danh sách yêu thích",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                viewModel.unsavedFavoriteSong(songList[position])
                binding.btnFavorite.setImageResource(R.drawable.ic_favorite)
                Toast.makeText(
                    this,
                    "Đã xóa bài hát khỏi danh sách yêu thích",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        if (isRepeatAll) {
            setSongPosition(true, applicationContext)
            createMusicPlayer()
            try {
                setSongUI()
                if (songList[position].thumbnail != null)
                    Glide.with(this).load(songList[position].thumbnail).into(
                        NowPlayingFragment.binding.ivSong
                    ) else
                    NowPlayingFragment.binding.ivSong.setImageResource(R.drawable.skittle_chan)
                NowPlayingFragment.binding.tvTitle.text = songList[position].name
                NowPlayingFragment.binding.tvArtist.text = songList[position].artists_names
                NowPlayingFragment.binding.btnPlayAndPause.setImageResource(
                    if (isPlaying)
                        R.drawable.ic_pause
                    else
                        R.drawable.ic_play_arrow
                )
                SongInfoFragment.adapter?.notifyDataSetChanged()
            } catch (e: Exception) {
                return
            }
        } else if (isRepeatOne) {
            createMusicPlayer()
            try {
                setSongUI()
            } catch (e: Exception) {
                return
            }
        } else if (!isRepeatAll && !isRepeatOne) {
            isPlaying = false
            musicService!!.showNotification(R.drawable.ic_play_arrow, 0F)
            NowPlayingFragment.binding.btnPlayAndPause.setImageResource(R.drawable.ic_play_arrow)
            binding.btnPlayAndPause.setImageResource(R.drawable.ic_play_arrow)
        }
    }

    @SuppressLint("SetTextI18n")
    fun setSongUI() {
        val song = songList[position]
        PlaySongFragment.binding?.tvTitle?.text = song.name.trim()
        PlaySongFragment.binding?.tvArtist?.text = song.artists_names.trim()
        PlaySongFragment.binding?.tvTitle?.isSelected = true
        PlaySongFragment.binding?.tvArtist?.isSelected = true
        var bitmap: Bitmap? = null
        binding.tvDurationTotal.text =
            formattedTime(song.duration.toLong() * 1000)
        GlobalScope.launch(Dispatchers.Default) {

            bitmap = if (song.thumbnail != null)
                try {
                    when {
                        song.online -> {
                            Glide.with(this@PlayerActivity)
                                .asBitmap()
                                .load(song.thumbnail)
                                .submit()
                                .get()
                        }
                        else -> {
                            Constants.getImageSongFromPath(song.thumbnail)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
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
                        binding.clContainer.background = gradientContainer
                    }
                } else {
                    PlaySongFragment.binding?.ivSong?.setImageResource(R.drawable.unknown_song)
                    PlaySongFragment.binding?.tvTitle?.setTextColor(Color.WHITE)
                    PlaySongFragment.binding?.tvArtist?.setTextColor(Color.WHITE)
                    PlaySongFragment.binding?.ivGradientImage?.setBackgroundResource(R.drawable.custom_bgr_gradient_music_player)
                    PlaySongFragment.binding?.ivGradientImage1?.setBackgroundResource(R.drawable.custom_bgr_gradient_music_player_1)
                    binding.clContainer.setBackgroundResource(R.drawable.custom_bgr_music_player)
                }
                if (checkFavorite()) {
                    binding.btnFavorite.setImageResource(R.drawable.ic_baseline_favorite_24)
                } else {
                    binding.btnFavorite.setImageResource(R.drawable.ic_favorite)
                }
                SongInfoFragment.binding?.tvName?.text = "Bài hát: ${song.name}"
                SongInfoFragment.binding?.tvArtist?.text = "Ca sỹ: ${song.artists_names}"
                SongInfoFragment.binding?.tvAlbum?.text = "Album: ${song.album}"
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun setupRelatedRecyclerView() {
        SongInfoFragment.binding?.rvSongRelated?.adapter = SongInfoFragment.adapter
        SongInfoFragment.binding?.rvSongRelated?.layoutManager = LinearLayoutManager(this)
        SongInfoFragment.adapter?.setOnItemClickListener { song ->
            position = songList.indexOfFirst { it.id == song.id }
            createMusicPlayer()
            setSongUI()
            val song = songList[position]
            SongInfoFragment.binding?.tvName?.text = "Bài hát: ${song.name}"
            SongInfoFragment.binding?.tvArtist?.text = "Ca sỹ: ${song.artists_names}"
            SongInfoFragment.binding?.tvAlbum?.text = "Album: ${song.album}"
            SongInfoFragment.adapter?.notifyDataSetChanged()
        }
    }
}