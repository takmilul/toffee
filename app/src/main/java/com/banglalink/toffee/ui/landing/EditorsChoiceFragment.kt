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
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.databinding.FragmentEditorsChoiceBinding
import com.banglalink.toffee.enums.PageType
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelNavParams
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.coroutines.flow.collectLatest

class EditorsChoiceFragment: HomeBaseFragment(), ProviderIconCallback<ChannelInfo> {
    
    private lateinit var mAdapter: EditorsChoiceListAdapter
    private var _binding: FragmentEditorsChoiceBinding?=null
    private val binding get() = _binding!!
    private val landingPageViewModel by activityViewModels<LandingPageViewModel>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditorsChoiceBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onDestroyView() {
        binding.editorsChoiceList.adapter = null
        super.onDestroyView()
        _binding = null
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var isInitialized = false
        mAdapter = EditorsChoiceListAdapter(this)
        
        /*if (landingPageViewModel.pageType.value != PageType.Landing) {
            startLoadingAnimation(false)
        }*/
        
        with(binding.editorsChoiceList) {
            mAdapter.addLoadStateListener {
                val isLoading = it.source.refresh is LoadState.Loading || !isInitialized
                val isEmpty = mAdapter.itemCount <= 0 && ! it.source.refresh.endOfPaginationReached
                binding.placeholder.isVisible = isEmpty
                binding.editorsChoiceList.isVisible = ! isEmpty
                startLoadingAnimation(isLoading)
                isInitialized = true
            }
            binding.placeholder.isVisible = true
            adapter = mAdapter
        }
        
        observeList()
    }
    
    private fun observeList() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            val content = if (landingPageViewModel.pageType.value == PageType.Landing) {
                landingPageViewModel.loadLandingEditorsChoiceContent
            }
            else {
                landingPageViewModel.loadEditorsChoiceContent()
            }
            content.collectLatest { 
                mAdapter.submitData(it)
            }
        }
    }

    override fun onItemClicked(item: ChannelInfo) {
        homeViewModel.fragmentDetailsMutableLiveData.postValue(item)
    }

    override fun onProviderIconClicked(item: ChannelInfo) {
        super.onProviderIconClicked(item)
        homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(item.channel_owner_id)
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
    
    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {

    }
}