package com.banglalink.toffee.ui.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.FeaturedListAdapter
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.util.unsafeLazy
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_landing_featured.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FeaturedFragment: HomeBaseFragment() {

    private lateinit var mAdapter: FeaturedListAdapter
    private var slideJob: Job? = null

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

        mAdapter = FeaturedListAdapter(object: BaseListItemCallback<ChannelInfo> {
            override fun onItemClicked(item: ChannelInfo) {
                homeViewModel.fragmentDetailsMutableLiveData.postValue(item)
            }
        })

        featured_viewpager.adapter = mAdapter
        featured_viewpager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                mAdapter.getItemByIndex(position)?.let {
                    featureDescription.text = it.program_name
                }
            }
        })

        TabLayoutMediator(featured_indicator, featured_viewpager, true) { tab_, position -> }.attach()

        observeList()
    }

    private fun observeList() {
        lifecycleScope.launchWhenStarted {
            viewModel.loadFeatureContents().collectLatest {
                mAdapter.submitData(it)
                startPageScroll()
            }
        }
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {

    }

    private fun startPageScroll() {
        slideJob?.cancel()
        slideJob = lifecycleScope.launch {
            while(isActive) {
                delay(5000)
                if(isActive && mAdapter.itemCount > 0) {
                    featured_viewpager?.currentItem = (featured_viewpager.currentItem + 1) % mAdapter.itemCount
                }
            }
        }
    }
}