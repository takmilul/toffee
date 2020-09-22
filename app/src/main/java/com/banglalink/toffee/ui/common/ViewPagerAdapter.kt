package com.banglalink.toffee.ui.common

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragment: Fragment?, private val fragmentList: ArrayList<Fragment>) : FragmentStateAdapter(fragment !!) {
    
    override fun getItemCount(): Int {
        return fragmentList.size
    }
    
    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }
}
