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
import com.banglalink.toffee.data.database.entities.SubscriptionInfo
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.databinding.FragmentSubscribedChannelsBinding
import com.banglalink.toffee.extension.checkVerification
import com.banglalink.toffee.listeners.LandingPopularChannelCallback
import com.banglalink.toffee.model.MyChannelNavParams
import com.banglalink.toffee.model.UserChannelInfo
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.common.UnSubscribeDialog
import com.banglalink.toffee.ui.widget.MarginItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SubscribedChannelsFragment : HomeBaseFragment() {

    @Inject lateinit var cacheManager: CacheManager
    private lateinit var mAdapter: AllSubscribedChannelAdapter
    private var subscribedChannelInfo: UserChannelInfo? = null
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
        mAdapter = AllSubscribedChannelAdapter(object :
            LandingPopularChannelCallback<UserChannelInfo> {
            override fun onItemClicked(item: UserChannelInfo) {
                homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(item.channelOwnerId)
            }

            override fun onSubscribeButtonClicked(view: View, info: UserChannelInfo) {
                requireActivity().checkVerification {
                    if (info.isSubscribed == 0) {
                        subscribedChannelInfo = info.also {
                            it.isSubscribed = 1
                            it.subscriberCount ++
                        }
                        homeViewModel.sendSubscriptionStatus(SubscriptionInfo(null, info.channelOwnerId, mPref.customerId), 1)
                        mAdapter.notifyItemRangeChanged(0, mAdapter.itemCount, subscribedChannelInfo)
                        mAdapter.refresh()
                    }
                    else {
                        UnSubscribeDialog.show(requireContext()) {
                            subscribedChannelInfo = info.also {
                                it.isSubscribed = 0
                                it.subscriberCount --
                            }
                            homeViewModel.sendSubscriptionStatus(SubscriptionInfo(null, info.channelOwnerId, mPref.customerId), - 1)
                            mAdapter.notifyItemRangeChanged(0, mAdapter.itemCount, subscribedChannelInfo)
                            mAdapter.refresh()
                        }
                    }
                }
            }
        })
    
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
    }

    private fun observeList() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadSubscribedChannels().collectLatest {
                mAdapter.submitData(it.filter { item -> item.isSubscribed==1 })
            }
            binding.totalSubscriptionsTextView.setText(mAdapter.itemCount)
        }
    }
    
    override fun onDestroyView() {
        binding.subscribedChannelList.adapter = null
        super.onDestroyView()
        _binding = null
    }
}