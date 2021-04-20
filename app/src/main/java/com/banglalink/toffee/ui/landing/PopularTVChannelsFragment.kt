package com.banglalink.toffee.ui.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.databinding.FragmentLandingTvChannelsBinding
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.facebook.shimmer.ShimmerFrameLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class PopularTVChannelsFragment : HomeBaseFragment(), BaseListItemCallback<ChannelInfo> {
    
    private lateinit var mAdapter: ChannelAdapter
    private  var _binding: FragmentLandingTvChannelsBinding?=null
    private val binding get() = _binding!!
    val viewModel by activityViewModels<LandingPageViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View {
        _binding = FragmentLandingTvChannelsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onDestroyView() {
        binding.channelList.adapter = null
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var isInitialized = false
        mAdapter = ChannelAdapter(this)

        with(binding.channelList) {
            mAdapter.addLoadStateListener {
                val isLoading = it.source.refresh is LoadState.Loading || !isInitialized
                val isEmpty = mAdapter.itemCount <= 0 && ! it.source.refresh.endOfPaginationReached
                binding.placeholder.isVisible = isEmpty
                binding.channelList.isVisible = ! isEmpty
                startLoadingAnimation(isLoading)
                isInitialized = true
            }
            adapter = mAdapter
        }

        binding.viewAllButton.setOnClickListener {
            homeViewModel.switchBottomTab.postValue(1)
        }
        
        observeList()
    }
    
    private fun startLoadingAnimation(isStart: Boolean) {
        binding.placeholder.children.forEach {
            if (it is ShimmerFrameLayout) {
                if (isStart) {
                    it.startShimmer()
                }
                else {
                    it.stopShimmer()
                }
            }
        }
    }
    
    private fun observeList() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.loadChannels.collectLatest {
                mAdapter.submitData(it)
            }
        }
    }
    
    override fun onItemClicked(item: ChannelInfo) {
        if (item.id.isNotBlank()) {
            homeViewModel.fragmentDetailsMutableLiveData.postValue(item)
        }
    }
    
    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {

    }
}