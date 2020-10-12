package com.banglalink.toffee.ui.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.ui.home.PopularVideoListAdapter
import kotlinx.android.synthetic.main.fragment_landing_latest_videos.*
import kotlinx.coroutines.flow.collectLatest

class LatestVideosFragment: HomeBaseFragment() {
    private lateinit var mAdapter: PopularVideoListAdapter

    val viewModel by activityViewModels<LandingPageViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_landing_latest_videos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = PopularVideoListAdapter(object : BaseListItemCallback<ChannelInfo> {
            override fun onItemClicked(item: ChannelInfo) {
                homeViewModel.fragmentDetailsMutableLiveData.postValue(item)
            }
        })

        with(latestVideosList) {
            adapter = mAdapter
        }

        observeList()
    }

    private fun observeList() {
        lifecycleScope.launchWhenStarted {
            viewModel.loadLatestVideos().collectLatest {
                mAdapter.submitData(it)
            }
        }
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {

    }
}