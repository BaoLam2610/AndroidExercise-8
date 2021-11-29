package com.example.musicappmvvm.ui.play.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicappmvvm.adapter.RelatedAdapter
import com.example.musicappmvvm.adapter.SongAdapter
import com.example.musicappmvvm.databinding.FragmentSongInfoBinding
import com.example.musicappmvvm.model.SongItem
import com.example.musicappmvvm.ui.main.fragments.ChartFragment
import com.example.musicappmvvm.ui.play.PlayerActivity
import com.example.musicappmvvm.ui.play.PlayerActivity.Companion.position
import com.example.musicappmvvm.ui.play.PlayerActivity.Companion.songList
import com.example.musicappmvvm.ui.play.PlayerViewModel
import com.example.musicappmvvm.utils.Constants
import com.example.musicappmvvm.utils.Constants.BUNDLE_SONG
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

        val type = (activity as PlayerActivity).intent.hasExtra(Constants.EXTRA_MY_SONG)
        viewModel.getSongRelated(getSong.id, type)
        viewModel.relatedSongs.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.let {
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