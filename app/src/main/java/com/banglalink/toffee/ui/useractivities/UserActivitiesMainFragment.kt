package com.banglalink.toffee.ui.useractivities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.banglalink.toffee.R
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.tab_activities_main.*

@AndroidEntryPoint
class UserActivitiesMainFragment: Fragment() {
    companion object {
        const val ARG_SELECTED_TAB = "selected-tab"

        fun newInstance(): UserActivitiesMainFragment {
            return UserActivitiesMainFragment()
        }

        private val tabTitle = listOf("Activities", "Subscriptions")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.tab_activities_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectedTab = arguments?.getInt(ARG_SELECTED_TAB, 0) ?: 0

        activities_pager.isUserInputEnabled = false
        activities_pager.adapter = UserActivitiesPagerAdapter(this)
        TabLayoutMediator(tab_layout, activities_pager) { tab, position ->
            tab.text = tabTitle[position]
            activities_pager.currentItem = tab.position
        }.attach()
        activities_pager.currentItem = selectedTab
    }
}