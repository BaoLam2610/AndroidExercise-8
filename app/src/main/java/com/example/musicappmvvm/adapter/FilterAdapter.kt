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
import com.example.musicappmvvm.model.filter.FilterSong
import com.example.musicappmvvm.ui.play.PlayerActivity
import com.example.musicappmvvm.utils.Constants.formattedTime

class FilterAdapter(
    val mContext: Context
) : RecyclerView.Adapter<FilterAdapter.FilterViewHolder>(){

    var filterList = emptyList<FilterSong>()
    fun setData(songList: List<FilterSong>) {
        this.filterList = songList
        notifyDataSetChanged()
    }

    private var onItemClickListener: ((FilterSong) -> Unit)? = null
    fun setOnItemClickListener(listener: (FilterSong) -> Unit) {
        this.onItemClickListener = listener
    }

    inner class FilterViewHolder(private val binding: ItemSongBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ResourceAsColor")
        fun bind(filterSong: FilterSong) {
            with(binding) {
                tvTitle.text = filterSong.name
                tvArtist.text = filterSong.artist
                tvTitle.isSelected = true
                tvArtist.isSelected = true
                tvDurationTotal.text = formattedTime(filterSong.duration.toLong() * 1000)
                if (filterSong.thumb != null)
                    Glide.with(mContext)
                        .load("https://photo-resize-zmp3.zadn.vn/w94_r1x1_jpeg/${filterSong.thumb}")
                        .into(ivSong)
                else
                    ivSong.setImageResource(R.drawable.unknown_song)
                root.setOnClickListener {
                    onItemClickListener?.let {
                        it(filterSong)
                    }
                }
                if (PlayerActivity.songList.isNotEmpty()) {
                    if (filterSong.id == PlayerActivity.songList[PlayerActivity.position].id) {
                        tvTitle.setTextColor(R.color.select_now_playing)
//                        tvArtist.setTextColor(R.color.select_now_playing)
                    } else {
                        tvTitle.setTextColor(Color.BLACK)
//                        tvArtist.setTextColor(R.color.gray)
                    }
                }
                if (PlayerActivity.songList.isEmpty()) {
                    tvTitle.setTextColor(Color.BLACK)
//                    tvArtist.setTextColor(R.color.gray)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FilterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) =
        holder.bind(filterList[position])

    override fun getItemCount(): Int = filterList.size
}