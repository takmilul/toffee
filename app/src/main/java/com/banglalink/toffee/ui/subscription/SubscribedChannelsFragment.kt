package com.banglalink.toffee.ui.subscription

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.filter
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.apiservice.ApiRoutes
import com.banglalink.toffee.data.database.entities.SubscriptionInfo
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.databinding.FragmentSubscribedChannelsBinding
import com.banglalink.toffee.extension.checkVerification
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.listeners.LandingPopularChannelCallback
import com.banglalink.toffee.model.MyChannelNavParams
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.UserChannelInfo
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.common.UnSubscribeDialog
import com.banglalink.toffee.ui.widget.MarginItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SubscribedChannelsFragment : HomeBaseFragment(), LandingPopularChannelCallback<UserChannelInfo> {

    @Inject lateinit var cacheManager: CacheManager
    private var subscribedChannelInfo: UserChannelInfo? = null
    private lateinit var mAdapter: AllSubscribedChannelAdapter
    private var _binding: FragmentSubscribedChannelsBinding ? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<SubscribedChannelsViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSubscribedChannelsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        ToffeeAnalytics.logEvent(ToffeeEvents.SCREEN_SUBSCRIPTION_LIST)
        mAdapter = AllSubscribedChannelAdapter(this)
    
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            var isInitialized = false
            mAdapter.loadStateFlow.collectLatest {
                val isLoading = it.source.refresh is LoadState.Loading || !isInitialized
                val isEmpty = mAdapter.itemCount <= 0 && ! it.source.refresh.endOfPaginationReached
                with(binding) {
                    progressBar.isVisible = isLoading
                    titleTextView.isVisible = !isEmpty
                    subscribedChannelList.isVisible = !isEmpty
                    emptyTextView.isVisible = isEmpty && !isLoading
                }
                isInitialized = true
            }
        }
        
        with(binding.subscribedChannelList) {
            addItemDecoration(MarginItemDecoration(12))
            adapter = mAdapter
        }
        observeList()
        observeSubscribeChannel()
    }
    
    private fun observeList() {
        cacheManager.clearCacheByUrl(ApiRoutes.GET_SUBSCRIBED_CHANNELS)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadSubscribedChannels().collectLatest {
                mAdapter.submitData(it.filter { item -> item.isSubscribed == 1 })
            }
            binding.totalSubscriptionsTextView.setText(mAdapter.itemCount)
        }
    }
    
    private fun observeSubscribeChannel() {
        observe(homeViewModel.subscriptionLiveData) { response ->
            when(response) {
                is Resource.Success -> {
                    subscribedChannelInfo?.apply {
                        isSubscribed = response.data.isSubscribed
                        subscriberCount = response.data.subscriberCount
                    }
                    observeList()
                }
                is Resource.Failure -> {
                    requireContext().showToast(response.error.msg)
                }
            }
        }
    }
    
    override fun onItemClicked(item: UserChannelInfo) {
        super.onItemClicked(item)
        homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(item.channelOwnerId)
    }
    
    override fun onSubscribeButtonClicked(view: View, info: UserChannelInfo, position: Int) {
        requireActivity().checkVerification {
            subscribedChannelInfo = info
            
            if (info.isSubscribed == 0) {
                homeViewModel.sendSubscriptionStatus(SubscriptionInfo(null, info.channelOwnerId, mPref.customerId), 1)
            }
            else {
                UnSubscribeDialog.show(requireContext()) {
                    homeViewModel.sendSubscriptionStatus(SubscriptionInfo(null, info.channelOwnerId, mPref.customerId), - 1)
                }
            }
        }
    }
    
    override fun onDestroyView() {
        binding.subscribedChannelList.adapter = null
        super.onDestroyView()
        _binding = null
    }
}