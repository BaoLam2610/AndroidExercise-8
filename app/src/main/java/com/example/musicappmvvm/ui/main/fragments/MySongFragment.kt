package com.example.musicappmvvm.ui.main.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicappmvvm.adapter.SongAdapter
import com.example.musicappmvvm.databinding.FragmentMySongBinding
import com.example.musicappmvvm.ui.main.MusicActivity
import com.example.musicappmvvm.ui.main.MusicViewModel
import com.example.musicappmvvm.ui.play.PlayerActivity
import com.example.musicappmvvm.utils.Constants
import com.example.musicappmvvm.utils.Constants.MY_SONG

class MySongFragment : Fragment() {

    companion object {
        var binding: FragmentMySongBinding? = null
        var mySongAdapter: SongAdapter? = null
        const val TAG = "MySongFragment"
    }
    lateinit var viewModel: MusicViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMySongBinding.inflate(inflater, container, false)
        viewModel = (activity as MusicActivity).viewModel
        mySongAdapter = SongAdapter(requireContext(), MY_SONG)
        viewModel.getMySongList()
        viewModel.mySongs.observe(viewLifecycleOwner){
            mySongAdapter!!.setData(it)
            setupRecyclerView()
        }
        return binding?.root
    }

    fun setupRecyclerView(){
        binding?.rvMySongs?.adapter = mySongAdapter
        binding?.rvMySongs?.layoutManager = LinearLayoutManager(requireContext())
        mySongAdapter?.setOnItemClickListener { song ->
            Intent(requireContext(), PlayerActivity::class.java).also {
                if (song.id == PlayerActivity.nowPlayingSong) {
                    it.putExtra(Constants.EXTRA_TYPE, Constants.CURRENT_SONG)
                } else {
                    it.putExtra(Constants.EXTRA_TYPE, TAG)
                }
                it.putExtra(Constants.EXTRA_MY_SONG, song)
                startActivity(it)
            }
        }
    }

}