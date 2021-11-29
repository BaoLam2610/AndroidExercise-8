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
import com.example.musicappmvvm.model.chart.Song
import com.example.musicappmvvm.ui.play.PlayerActivity
import com.example.musicappmvvm.utils.Constants.formattedTime

class ChartAdapter(
    val mContext: Context
) : RecyclerView.Adapter<ChartAdapter.SongViewHolder>() {

    var songList = emptyList<Song>()
    fun setData(songList: List<Song>) {
        this.songList = songList
        notifyDataSetChanged()
    }

    private var onItemClickListener: ((Song) -> Unit)? = null
    fun setOnItemClickListener(listener: (Song) -> Unit) {
        this.onItemClickListener = listener
    }

    inner class SongViewHolder(private val binding: ItemSongBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ResourceAsColor")
        fun bind(song: Song) {
            with(binding) {
                tvTitle.text = song.title
                tvArtist.text = song.artists_names
                tvTitle.isSelected = true
                tvArtist.isSelected = true
                tvDurationTotal.text = formattedTime(song.duration.toLong() * 1000)
                Glide.with(mContext).load(song.thumbnail).into(ivSong)
                root.setOnClickListener {
                    onItemClickListener?.let {
                        it(song)
                    }
                }
                if(PlayerActivity.songList.isNotEmpty()) {
                    if (song.id == PlayerActivity.songList[PlayerActivity.position].id) {
                        tvTitle.setTextColor(R.color.select_now_playing)
//                        tvArtist.setTextColor(R.color.select_now_playing)
                    } else {
                        tvTitle.setTextColor(Color.BLACK)
//                        tvArtist.setTextColor(R.color.gray)
                    }
                }
                if(PlayerActivity.songList.isEmpty()){
                    tvTitle.setTextColor(Color.BLACK)
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