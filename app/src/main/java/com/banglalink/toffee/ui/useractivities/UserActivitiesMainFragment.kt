package com.banglalink.toffee.ui.useractivities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.banglalink.toffee.databinding.TabActivitiesMainBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserActivitiesMainFragment: Fragment() {
    private var _binding: TabActivitiesMainBinding ? = null
    private val binding get() = _binding!!

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
        _binding = TabActivitiesMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectedTab = arguments?.getInt(ARG_SELECTED_TAB, 0) ?: 0

        binding.activitiesPager.isUserInputEnabled = false
        binding.activitiesPager.adapter = UserActivitiesPagerAdapter(this)
        TabLayoutMediator(binding.tabLayout, binding.activitiesPager) { tab, position ->
            tab.text = tabTitle[position]
            binding.activitiesPager.currentItem = tab.position
        }.attach()
        binding.activitiesPager.currentItem = selectedTab
    }
}