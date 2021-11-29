package com.example.musicappmvvm.ui.play.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.musicappmvvm.R
import com.example.musicappmvvm.databinding.FragmentPlaySongBinding
import com.example.musicappmvvm.model.SongItem
import com.example.musicappmvvm.ui.play.PlayerActivity
import com.example.musicappmvvm.ui.play.PlayerViewModel
import com.example.musicappmvvm.utils.Constants.BUNDLE_SONG

class PlaySongFragment : Fragment() {

    companion object {
        fun newInstance(song: SongItem?): PlaySongFragment {
            val args = Bundle()
            args.putSerializable(BUNDLE_SONG, song)
            val fragment = PlaySongFragment()
            fragment.arguments = args
            return fragment
        }
        var binding: FragmentPlaySongBinding? = null
    }


    lateinit var viewModel: PlayerViewModel
    lateinit var getSong: SongItem

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlaySongBinding.inflate(inflater, container, false)
        viewModel = (activity as PlayerActivity).viewModel
        getSong = (activity as PlayerActivity).getSong!!
        setSongUI()
        return binding?.root
    }

    fun setSongUI(){
        binding?.tvTitle?.text = getSong.name
        binding?.tvArtist?.text = getSong.artists_names
        binding?.tvTitle?.isSelected = true
        binding?.tvArtist?.isSelected = true
    }


}