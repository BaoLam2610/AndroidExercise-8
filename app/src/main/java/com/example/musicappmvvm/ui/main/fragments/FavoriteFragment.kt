package com.example.musicappmvvm.ui.main.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicappmvvm.adapter.SongAdapter
import com.example.musicappmvvm.databinding.FragmentFavoriteBinding
import com.example.musicappmvvm.model.SongItem
import com.example.musicappmvvm.ui.main.MusicActivity
import com.example.musicappmvvm.ui.main.MusicViewModel
import com.example.musicappmvvm.ui.play.PlayerActivity
import com.example.musicappmvvm.utils.Constants
import com.example.musicappmvvm.utils.Constants.CURRENT_SONG
import com.example.musicappmvvm.utils.Constants.FAVORITE
import com.google.android.material.snackbar.Snackbar

class FavoriteFragment : Fragment() {

    companion object {
        const val TAG = "FavoriteFragment"
        var favAdapter: SongAdapter? = null
    }

    lateinit var binding: FragmentFavoriteBinding
    lateinit var viewModel: MusicViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        viewModel = (activity as MusicActivity).viewModel
        favAdapter = SongAdapter(requireContext(), FAVORITE)
        viewModel.favoriteSongs.observe(viewLifecycleOwner) {
            favAdapter?.setData(it as MutableList<SongItem>)
            setupRecyclerView()
        }
        val itemTouchHelperCallBack = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val song = favAdapter?.songList?.get(position)
                viewModel.unSavedFavorite(song!!)
                Snackbar.make(binding.root, "Successfully deleted article", Snackbar.LENGTH_SHORT)
                    .apply {
                        setAction("Undo") {
                            viewModel.saveFavorite(song)
                        }
                        show()
                    }
            }
        }
        ItemTouchHelper(itemTouchHelperCallBack).apply {
            attachToRecyclerView(binding.rvFavoriteSongs)
        }
        return binding.root
    }


    fun setupRecyclerView() {
        binding.rvFavoriteSongs.adapter = favAdapter
        binding.rvFavoriteSongs.layoutManager = LinearLayoutManager(requireContext())
        favAdapter?.setOnItemClickListener { favorite ->
            Intent(requireContext(), PlayerActivity::class.java).also {
                if (favorite.id == PlayerActivity.nowPlayingSong) {
                    it.putExtra(Constants.EXTRA_TYPE, CURRENT_SONG)
                } else {
                    it.putExtra(Constants.EXTRA_TYPE, TAG)
                }
                it.putExtra(Constants.EXTRA_SONG_FAVORITE, favorite)
                startActivity(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        favAdapter?.notifyDataSetChanged()
    }
}