package com.banglalink.toffee.ui.useractivities

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.banglalink.toffee.ui.recent.RecentFragment

class UserActivitiesPagerAdapter(fragment: Fragment):
    FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> UserActivitiesListFragment.newInstance()
            else -> ChannelSubscriptionFragment.newInstance()
        }
    }
}