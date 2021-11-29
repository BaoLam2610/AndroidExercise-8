package com.example.musicappmvvm.ui.main.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicappmvvm.adapter.ChartAdapter
import com.example.musicappmvvm.adapter.FilterAdapter
import com.example.musicappmvvm.databinding.FragmentChartBinding
import com.example.musicappmvvm.ui.main.MusicActivity
import com.example.musicappmvvm.ui.main.MusicViewModel
import com.example.musicappmvvm.ui.play.PlayerActivity
import com.example.musicappmvvm.utils.Constants.CURRENT_SONG
import com.example.musicappmvvm.utils.Constants.EXTRA_SONG_CHART
import com.example.musicappmvvm.utils.Constants.EXTRA_SONG_FILTER
import com.example.musicappmvvm.utils.Constants.EXTRA_TYPE
import com.example.musicappmvvm.utils.Constants.SEARCH_SONG_TIME_DELAY
import com.example.musicappmvvm.utils.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChartFragment : Fragment() {

    lateinit var binding: FragmentChartBinding
    lateinit var viewModel: MusicViewModel


    companion object {
        const val TAG = "ChartFragment"
        lateinit var chartAdapter: ChartAdapter
        lateinit var filterAdapter: FilterAdapter
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChartBinding.inflate(inflater, container, false)
        viewModel = (activity as MusicActivity).viewModel
        chartAdapter = ChartAdapter(requireContext())
        filterAdapter = FilterAdapter(requireContext())


        var job: Job? = null
        binding.etSearch.addTextChangedListener { editable ->
            job?.cancel()
            job = MainScope().launch {
                delay(SEARCH_SONG_TIME_DELAY)
                editable?.let {
                    if (editable.toString().isNotEmpty()) {
                        viewModel.getSongFilters(editable.toString())
                    } else {
                        viewModel.getSongCharts()
                    }
                }
            }
        }
        getCharts()
        getFilters()


        return binding.root
    }

    override fun onResume() {
        super.onResume()
        chartAdapter.notifyDataSetChanged()
        filterAdapter.notifyDataSetChanged()
    }

    fun getCharts() {
        viewModel.songCharts.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let {
                        chartAdapter.setData(it)
                        setupChartRecyclerView()
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Log.e(TAG, "Error: $message")
                        Toast.makeText(activity, "Error: $message", Toast.LENGTH_LONG).show()
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }
    }

    fun getFilters() {
        viewModel.songFilters.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let {
                        filterAdapter.setData(it)
                        setupFilterRecyclerView()
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Log.e(TAG, "Error: $message")
                        Toast.makeText(activity, "Error: $message", Toast.LENGTH_LONG).show()
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }
    }

    fun setupChartRecyclerView() {
        binding.rvSongs.visibility = View.VISIBLE
        binding.rvFilterSongs.visibility = View.GONE
        binding.rvSongs.adapter = chartAdapter
        binding.rvSongs.layoutManager = LinearLayoutManager(requireContext())
        chartAdapter.setOnItemClickListener { chart ->
            Intent(requireContext(), PlayerActivity::class.java).also {
                if (chart.id == PlayerActivity.nowPlayingSong) {
                    it.putExtra(EXTRA_TYPE, CURRENT_SONG)
                } else {
                    it.putExtra(EXTRA_TYPE, TAG)
                }
                it.putExtra(EXTRA_SONG_CHART, chart)
                startActivity(it)
            }
        }
    }

    fun setupFilterRecyclerView() {
        binding.rvSongs.visibility = View.GONE
        binding.rvFilterSongs.visibility = View.VISIBLE
        binding.rvFilterSongs.adapter = filterAdapter
        binding.rvFilterSongs.layoutManager = LinearLayoutManager(requireContext())
        filterAdapter.setOnItemClickListener { filter ->
            Intent(requireContext(), PlayerActivity::class.java).also {
                if (filter.id == PlayerActivity.nowPlayingSong) {
                    it.putExtra(EXTRA_TYPE, CURRENT_SONG)
                } else {
                    it.putExtra(EXTRA_TYPE, TAG)
                }
                it.putExtra(EXTRA_SONG_FILTER, filter)
                startActivity(it)
            }
        }
    }

    private fun hideProgressBar() {
        binding.paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    var isLoading = false
}