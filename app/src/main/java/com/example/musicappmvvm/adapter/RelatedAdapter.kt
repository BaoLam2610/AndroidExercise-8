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
import com.example.musicappmvvm.utils.Constants.formattedTime

class RelatedAdapter(
    val mContext: Context,
) : RecyclerView.Adapter<RelatedAdapter.RelatedViewHolder>() {

    var relatedList = mutableListOf<SongItem>()
    fun setData(songList: MutableList<SongItem>) {
        this.relatedList = songList
        notifyDataSetChanged()
    }

    private var onItemClickListener: ((SongItem) -> Unit)? = null
    fun setOnItemClickListener(listener: (SongItem) -> Unit) {
        this.onItemClickListener = listener
    }

    inner class RelatedViewHolder(private val binding: ItemSongBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ResourceAsColor")
        fun bind(related: SongItem) {
            with(binding) {
                tvTitle.text = related.name
                tvArtist.text = related.artists_names
                tvTitle.isSelected = true
                tvArtist.isSelected = true
                tvTitle.setTextColor(Color.WHITE)
                tvArtist.setTextColor(Color.WHITE)
                tvDurationTotal.text = formattedTime(related.duration.toLong() * 1000)
                tvDurationTotal.setTextColor(Color.WHITE)
                if (related.thumbnail != null) {
                    try {
                        when {
                            related.online -> Glide.with(mContext).load(related.thumbnail)
                                .into(ivSong)
                            else -> binding.ivSong.setImageBitmap(
                                Constants.getImageSongFromPath(
                                    related.thumbnail
                                )
                            )
                        }

                    } catch (e: Exception) {
                        binding.ivSong.setImageResource(R.drawable.skittle_chan)
                    }
                } else
                    binding.ivSong.setImageResource(R.drawable.unknown_song)
//                root.setOnClickListener {
//                    iOnClickItem?.onClickItemRelatedListener(related)
//                }
                if (PlayerActivity.songList.isNotEmpty()) {
                    if (related.id == PlayerActivity.songList[PlayerActivity.position].id) {
                        tvTitle.setTextColor(R.color.select_now_playing)
//                        tvArtist.setTextColor(R.color.select_now_playing)
                    } else {
                        tvTitle.setTextColor(Color.WHITE)
//                        tvArtist.setTextColor(R.color.gray)
                    }
                }
                if (PlayerActivity.songList.isEmpty()) {
                    tvTitle.setTextColor(Color.WHITE)
//                    tvArtist.setTextColor(R.color.gray)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RelatedViewHolder {
        val binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RelatedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RelatedViewHolder, position: Int) =
        holder.bind(relatedList[position])

    override fun getItemCount(): Int = relatedList.size
}