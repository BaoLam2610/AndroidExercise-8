package com.example.musicappmvvm.ui.play.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.musicappmvvm.adapter.SongAdapter
import com.example.musicappmvvm.databinding.FragmentSongInfoBinding
import com.example.musicappmvvm.model.SongItem
import com.example.musicappmvvm.ui.main.fragments.ChartFragment
import com.example.musicappmvvm.ui.play.PlayerActivity
import com.example.musicappmvvm.ui.play.PlayerActivity.Companion.songList
import com.example.musicappmvvm.ui.play.PlayerViewModel
import com.example.musicappmvvm.utils.Constants.BUNDLE_SONG
import com.example.musicappmvvm.utils.Constants.EXTRA_MY_SONG
import com.example.musicappmvvm.utils.Constants.EXTRA_SONG_FAVORITE
import com.example.musicappmvvm.utils.Constants.RELATED
import com.example.musicappmvvm.utils.Resource

class SongInfoFragment : Fragment() {

    companion object {
        fun newInstance(song: SongItem?): SongInfoFragment {
            val args = Bundle()
            args.putSerializable(BUNDLE_SONG, song)
            val fragment = SongInfoFragment()
            fragment.arguments = args
            return fragment
        }

        var binding: FragmentSongInfoBinding? = null
        var adapter: SongAdapter? = null
    }

    lateinit var viewModel: PlayerViewModel
    lateinit var getSong: SongItem

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSongInfoBinding.inflate(inflater, container, false)
        viewModel = (activity as PlayerActivity).viewModel
        adapter = SongAdapter(requireContext(), RELATED)

        getSong = (activity as PlayerActivity).getSong!!

        val typeMySong = (activity as PlayerActivity).intent.hasExtra(EXTRA_MY_SONG)
        val typeFavSong = (activity as PlayerActivity).intent.hasExtra(EXTRA_SONG_FAVORITE)
        viewModel.getSongRelated(getSong.id, when{
            typeMySong -> EXTRA_MY_SONG
            typeFavSong -> EXTRA_SONG_FAVORITE
            else -> ""
        })
        viewModel.relatedSongs.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.let {
                        songList = it
                        adapter?.setData(it)
                        (activity as PlayerActivity).setupRelatedRecyclerView()
                    }
                }
                is Resource.Error -> {
                    response.message?.let { message ->
                        Log.e(ChartFragment.TAG, "Error: $message")
                        Toast.makeText(activity, "Error: $message", Toast.LENGTH_LONG).show()
                    }
                }
                is Resource.Loading -> {
                }
            }
        }
        binding?.tvName?.text = "Bài hát: ${getSong.name}"
        binding?.tvArtist?.text = "Ca sỹ: ${getSong.artists_names}"
        binding?.tvAlbum?.text = "Album: ${getSong.album}"
        return binding?.root
    }

    /*fun setupRelatedRecyclerView() {
        binding?.rvSongRelated?.adapter = adapter
        binding?.rvSongRelated?.layoutManager = LinearLayoutManager(requireContext())
        adapter?.setOnItemClickListener { song ->
            position = songList.indexOfFirst { it.id == song.id }
            createMusicPlayer()
        }
    }*/

    override fun onResume() {
        super.onResume()
        adapter?.notifyDataSetChanged()
    }

}