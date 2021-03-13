package com.banglalink.toffee.ui.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_landing_featured.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FeaturedContentFragment : HomeBaseFragment() {

    private var isDataLoaded = false
    private var slideJob: Job? = null
    private lateinit var mAdapter: FeaturedContentAdapter
    val viewModel by activityViewModels<LandingPageViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_landing_featured, container, false)
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

        featured_viewpager.adapter = mAdapter
        TabLayoutMediator(featured_indicator, featured_viewpager, true) { tab_, position -> }.attach()
        
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
                    featured_viewpager?.currentItem = (featured_viewpager.currentItem + 1) % mAdapter.itemCount
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