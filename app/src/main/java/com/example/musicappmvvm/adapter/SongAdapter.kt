package com.example.musicappmvvm.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicappmvvm.R
import com.example.musicappmvvm.databinding.ItemSongBinding
import com.example.musicappmvvm.model.SongItem
import com.example.musicappmvvm.ui.play.PlayerActivity
import com.example.musicappmvvm.utils.Constants
import com.example.musicappmvvm.utils.Constants.FAVORITE
import com.example.musicappmvvm.utils.Constants.MY_SONG
import com.example.musicappmvvm.utils.Constants.getImageSongFromPath

class SongAdapter(
    val mContext: Context,
    val type: Int
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    var songList = mutableListOf<SongItem>()
    fun setData(songList: MutableList<SongItem>) {
        this.songList = songList
        notifyDataSetChanged()
    }

    private var onItemClickListener: ((SongItem) -> Unit)? = null
    fun setOnItemClickListener(listener: (SongItem) -> Unit) {
        this.onItemClickListener = listener
    }

    inner class SongViewHolder(private val binding: ItemSongBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ResourceAsColor")
        fun bind(song: SongItem) {
            with(binding) {
                tvTitle.text = song.name
                tvArtist.text = song.artists_names
                tvTitle.isSelected = true
                tvArtist.isSelected = true
                when (type) {
                    MY_SONG, FAVORITE -> {
                        tvTitle.setTextColor(Color.BLACK)
                        tvArtist.setTextColor(Color.BLACK)
                        tvDurationTotal.setTextColor(Color.BLACK)
                    }
                    else -> {
                        tvTitle.setTextColor(Color.WHITE)
                        tvArtist.setTextColor(Color.WHITE)
                        tvDurationTotal.setTextColor(Color.WHITE)
                    }
                }
                tvDurationTotal.text = Constants.formattedTime(song.duration.toLong() * 1000)

                if (song.thumbnail != null) {
                    try {
                        if (!song.online)
                            ivSong.setImageBitmap(getImageSongFromPath(song.thumbnail))
                        else
                            Glide.with(mContext).load(song.thumbnail).into(ivSong)
                    } catch (e: Exception) {
                        ivSong.setImageResource(R.drawable.skittle_chan)
                    }
                } else
                    binding.ivSong.setImageResource(R.drawable.unknown_song)
                root.setOnClickListener {
                    onItemClickListener?.let { it(song) }
                }
                if (PlayerActivity.songList.isNotEmpty()) {
                    if (song.id == PlayerActivity.songList[PlayerActivity.position].id) {
                        tvTitle.setTextColor(R.color.select_now_playing)
//                        tvArtist.setTextColor(R.color.select_now_playing)
                    } else {
                        tvTitle.setTextColor(
                            if (type == MY_SONG || type == FAVORITE)
                                Color.BLACK
                            else Color.WHITE)
//                        tvArtist.setTextColor(R.color.gray)
                    }
                }
                if (PlayerActivity.songList.isEmpty()) {
                    tvTitle.setTextColor(
                        if (type == MY_SONG || type == FAVORITE)
                            Color.BLACK
                        else Color.WHITE
                    )
//                    tvArtist.setTextColor(R.color.gray)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) =
        holder.bind(songList[position])

    override fun getItemCount(): Int = songList.size

}