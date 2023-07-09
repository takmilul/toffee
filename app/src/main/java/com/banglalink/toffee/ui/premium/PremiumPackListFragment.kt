package com.banglalink.toffee.ui.premium

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentPremiumPackListBinding
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.common.ViewPagerAdapter
import com.banglalink.toffee.util.Log
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class PremiumPackListFragment : BaseFragment() {

    private var _binding: FragmentPremiumPackListBinding? = null
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private var fromChannelItem: Boolean? = false
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<PremiumViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPremiumPackListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = "Premium Packs"
        fromChannelItem = arguments?.getBoolean("clickedFromChannelItem")
        Log.d("PremiumPackText", fromChannelItem.toString())
        loadView()
    }

    private fun loadView(){
        viewPagerAdapter = ViewPagerAdapter(childFragmentManager, lifecycle)
        binding.viewPager.offscreenPageLimit = OFFSCREEN_PAGE_LIMIT_DEFAULT
        binding.viewPager.adapter = viewPagerAdapter

        if (viewPagerAdapter.itemCount == 0) {
            viewPagerAdapter.addFragments(listOf(
                PremiumPacksFragment.newInstance(fromChannelItem!!),
                SubscriptionHistoryFragment()
            ))
        }

        val fragmentTitleList = listOf(resources.getString(R.string.premium_packs_title), resources.getString(R.string.my_subscriptions_title))
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = fragmentTitleList[position]
        }.attach()


        //Preventing network call automatically on viewpager load
        viewModel.setClickedOnSubHistoryFlag(false)
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val selectedPosition = tab!!.position
                if (selectedPosition == 1){
                    viewModel.setClickedOnSubHistoryFlag(true)
                }else {
                    viewModel.setClickedOnSubHistoryFlag(false)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}