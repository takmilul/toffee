package com.banglalink.toffee.ui.common

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {
    private val fragmentList: MutableList<Fragment> = mutableListOf()
    
    override fun getItemCount(): Int {
        return fragmentList.size
    }
    
    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

    fun addFragments(fragments: List<Fragment>) {
        fragmentList.addAll(fragments)
    }
    
    fun replaceFragment(fragment: Fragment, position: Int){
        fragmentList[position] = fragment
        this.notifyItemChanged(position)
    }
}
