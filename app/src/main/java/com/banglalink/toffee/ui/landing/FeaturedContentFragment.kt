package com.banglalink.toffee.ui.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.databinding.FragmentLandingFeaturedBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FeaturedContentFragment : HomeBaseFragment() {

    private var isDataLoaded = false
    private var slideJob: Job? = null
    private lateinit var mAdapter: FeaturedContentAdapter
    val viewModel by activityViewModels<LandingPageViewModel>()
    private lateinit var binding:FragmentLandingFeaturedBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
         binding = FragmentLandingFeaturedBinding.inflate(inflater, container, false)
         return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAdapter = FeaturedContentAdapter(object : BaseListItemCallback<ChannelInfo> {
            override fun onItemClicked(item: ChannelInfo) {
                if(isDataLoaded) {
                    homeViewModel.fragmentDetailsMutableLiveData.postValue(item)
                }
            }
        })

        binding.featuredViewpager.adapter = mAdapter
        TabLayoutMediator(binding.featuredIndicator, binding.featuredViewpager, true) { tab_, position -> }.attach()
        
        val channelInfoList = listOf(
            ChannelInfo(""), 
            ChannelInfo(""), 
            ChannelInfo(""), 
            ChannelInfo("")
        )
        mAdapter.removeAll()
        mAdapter.addAll(channelInfoList)
        startPageScroll()

        observeList()
        viewModel.loadFeaturedContentList()
    }

    private fun observeList() {
        lifecycleScope.launchWhenStarted {
            observe(viewModel.featuredContents) {
                when (it) {
                    is Success -> {
                        isDataLoaded = true
                        it.data?.let { channelInfoList ->
                            startPageScroll()
                            mAdapter.removeAll()
                            mAdapter.addAll(channelInfoList)
                        }
                    }
                    is Failure -> {
                    }
                }
            }
        }
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {

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

    override fun onStop() {
        viewModel.featuredContents.removeObservers(this)
        super.onStop()
    }
    
    override fun onDestroy() {
        slideJob?.cancel()
        super.onDestroy()
    }
}