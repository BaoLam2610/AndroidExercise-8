package com.example.musicappmvvm.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class TabLayoutAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    var fragList: List<Fragment>
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int = fragList.size

    override fun createFragment(position: Int): Fragment = fragList[position]
}