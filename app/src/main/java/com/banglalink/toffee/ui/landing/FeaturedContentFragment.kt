package com.banglalink.toffee.ui.landing

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.databinding.FragmentLandingFeaturedBinding
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.util.currentDateTime
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FeaturedContentFragment : HomeBaseFragment(), BaseListItemCallback<ChannelInfo> {
    
    private var slideJob: Job? = null
    private lateinit var mAdapter: FeaturedContentAdapter
    private var _binding:FragmentLandingFeaturedBinding ? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<LandingPageViewModel>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
         _binding = FragmentLandingFeaturedBinding.inflate(inflater, container, false)
         return binding.root
    }
    
    override fun onDestroyView() {
        slideJob?.cancel()
        binding.featuredViewpager.adapter = null
        super.onDestroyView()
        _binding = null
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.featuredJob?.cancel()
        viewModel.featuredJob = null
        mAdapter = FeaturedContentAdapter(this)
        binding.featuredViewpager.adapter = mAdapter
        TabLayoutMediator(binding.featuredIndicator, binding.featuredViewpager, true) { _, _ -> }.attach()
        observeList()
        viewModel.loadFeaturedContentList()
    }

    private fun observeList() {
        observe(viewModel.featuredContents) {
            if(it.isNotEmpty()) {
                mAdapter.removeAll()
                mAdapter.addAll(it)
                startPageScroll()
                binding.placeholder.root.hide()
                binding.featuredViewpager.show()
                binding.placeholder.root.stopShimmer()
            }
            else {
                binding.placeholder.root.stopShimmer()
            }
        }
    }
    
    override fun onItemClicked(item: ChannelInfo) {
        item.bannerEventName?.let {
            ToffeeAnalytics.logEvent(it, bundleOf(
                "timestamp" to currentDateTime,
                "banner-code" to it,
                "page" to viewModel.featuredPageName.value,
                "content-id" to item.id,
                "app-version" to cPref.appVersionName,
                "device-type" to "1",
                "device-id" to cPref.deviceId,
                "msisdn" to mPref.phoneNumber
            ))
        }
        homeViewModel.playContentLiveData.postValue(item)
    }
    
    private fun startPageScroll() {
        slideJob?.cancel()
        slideJob = lifecycleScope.launch {
            while (isActive) {
                delay(5000)
                if (isActive && mAdapter.itemCount > 0) {
                    binding.featuredViewpager.currentItem = (binding.featuredViewpager.currentItem + 1) % mAdapter.itemCount
                }
            }
        }
    }
}