package com.banglalink.toffee.ui.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.databinding.FragmentLandingTvChannelsBinding
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PopularTVChannelsFragment : HomeBaseFragment() {
    private lateinit var mAdapter: ChannelAdapter
    private lateinit var binding: FragmentLandingTvChannelsBinding
    val viewModel by activityViewModels<LandingPageViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLandingTvChannelsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = ChannelAdapter(object : BaseListItemCallback<ChannelInfo> {
            override fun onItemClicked(item: ChannelInfo) {
                homeViewModel.fragmentDetailsMutableLiveData.postValue(item)
            }
        })

        with(binding.channelList) {
            adapter = mAdapter
        }

        binding.viewAllButton.setOnClickListener {
            homeViewModel.switchBottomTab.postValue(1)
        }
        val channelInfoList = PagingData.from(listOf(
            ChannelInfo("", type = "LIVE"),
            ChannelInfo("", type = "LIVE"),
            ChannelInfo("", type = "LIVE"),
            ChannelInfo("", type = "LIVE"),
            ChannelInfo("", type = "LIVE"),
        ))
        lifecycleScope.launch { 
            mAdapter.submitData(channelInfoList)
        }
        observeList()
    }

    private fun observeList() {
        lifecycleScope.launchWhenStarted {
            viewModel.loadChannels.collectLatest {
                mAdapter.submitData(it)
            }
        }
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {

    }
}