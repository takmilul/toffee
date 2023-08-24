package com.banglalink.toffee.ui.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentPremiumBinding
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.common.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class PremiumFragment : BaseFragment() {
    
    private var contentId: String? = null
    private val binding get() = _binding!!
    private var _binding: FragmentPremiumBinding? = null
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private var fromChannelItem: Boolean? = false
    private val viewModel by activityViewModels<PremiumViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPremiumBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.toffee_premium)
        contentId = arguments?.getString("contentId")
        fromChannelItem = arguments?.getBoolean("clickedFromChannelItem")
        loadView()
    }

    private fun loadView() {
        viewPagerAdapter = ViewPagerAdapter(childFragmentManager, lifecycle)
        binding.viewPager.offscreenPageLimit = OFFSCREEN_PAGE_LIMIT_DEFAULT
        binding.viewPager.adapter = viewPagerAdapter

        if(viewPagerAdapter.itemCount == 0){
            viewPagerAdapter.addFragments(listOf(
                PremiumPacksFragment.newInstance(contentId, fromChannelItem ?: false),
                SubscriptionHistoryFragment()
            ))
        }

        val fragmentTitleList = listOf(
            resources.getString(R.string.premium_packs_title),
            resources.getString(R.string.my_subscriptions_title)
        )

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = fragmentTitleList[position]
        }.attach()

        // Preventing network call automatically on viewpager load
        viewModel.clickedOnSubHistory.value = false

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab?.position.let {
                    viewModel.clickedOnPackList.value = it == 0
                    viewModel.clickedOnSubHistory.value = it == 1
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        //after sign in from subscription history fragment, showing subscription history programmatically
        if (mPref.isLoggedInFromSubHistory) {
            binding.tabLayout.getTabAt(1)?.apply {
                select()
                binding.viewPager.setCurrentItem(1, false)
            }
            mPref.isLoggedInFromSubHistory = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}