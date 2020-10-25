package com.banglalink.toffee.ui.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.banglalink.toffee.ui.channels.ChannelFragment
import com.banglalink.toffee.ui.landing.LandingRootFragment
import com.banglalink.toffee.ui.mychannel.MyChannelHomeFragment
import com.banglalink.toffee.ui.useractivities.UserActivitiesMainFragment

class HomeTabPagerAdapter(fragment: FragmentActivity):
    FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> {
                LandingRootFragment.newInstance()
            }
            1 -> {
                ChannelFragment.createInstance(
                    0,
                    "",
                    ""//getString(R.string.menu_channel_text)
                )
            }
            2 -> {
                UserActivitiesMainFragment.newInstance()
            }
            else -> MyChannelHomeFragment.newInstance(0, 0)
        }
    }
}