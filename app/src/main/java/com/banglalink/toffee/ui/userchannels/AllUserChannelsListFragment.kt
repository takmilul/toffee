package com.banglalink.toffee.ui.userchannels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.data.database.entities.SubscriptionInfo
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.databinding.FragmentAllUserChannelsListBinding
import com.banglalink.toffee.extension.checkVerification
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.listeners.LandingPopularChannelCallback
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.MyChannelNavParams
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.UserChannelInfo
import com.banglalink.toffee.ui.category.CategoryDetailsFragment
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.common.UnSubscribeDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class AllUserChannelsListFragment : HomeBaseFragment() {
    
    private var categoryInfo: Category? = null
    @Inject lateinit var cacheManager: CacheManager
    private var trendingChannelInfo: UserChannelInfo? = null
    private lateinit var mAdapter: AllUserChannelsListAdapter
    private var _binding: FragmentAllUserChannelsListBinding ? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<AllUserChannelsListViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAllUserChannelsListBinding.inflate(inflater, container, false)
        return binding.root    
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryInfo = parentFragment?.arguments?.getParcelable(CategoryDetailsFragment.ARG_CATEGORY_ITEM)
        ToffeeAnalytics.logEvent(ToffeeEvents.SCREEN_SUBSCRIPTION_LIST,null)

        mAdapter = AllUserChannelsListAdapter(object : LandingPopularChannelCallback<UserChannelInfo> {
            override fun onItemClicked(item: UserChannelInfo) {
                homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(item.channelOwnerId)
            }

            override fun onSubscribeButtonClicked(view: View, info: UserChannelInfo) {
                requireActivity().checkVerification {
//                trendingChannelInfo = info
                    if (info.isSubscribed == 0) {
                        ToffeeAnalytics.logEvent(ToffeeEvents.CHANNEL_SUBSCRIPTION,null)
                        trendingChannelInfo = info.also {
                            it.isSubscribed = 1
                            it.subscriberCount++
                        }
                        mAdapter.notifyItemRangeChanged(0, mAdapter.itemCount, trendingChannelInfo)
                        homeViewModel.sendSubscriptionStatus(
                            SubscriptionInfo(
                                null,
                                info.channelOwnerId,
                                mPref.customerId
                            ), 1
                        )
                    } else {
                        UnSubscribeDialog.show(requireContext()) {
                            trendingChannelInfo = info.also {
                                it.isSubscribed = 0
                                it.subscriberCount--
                            }
                            mAdapter.notifyItemRangeChanged(
                                0,
                                mAdapter.itemCount,
                                trendingChannelInfo
                            )
                            homeViewModel.sendSubscriptionStatus(
                                SubscriptionInfo(
                                    null,
                                    info.channelOwnerId,
                                    mPref.customerId
                                ), -1
                            )
                        }
                    }
                }
            }
        })

        with(binding.userChannelList) {
            layoutManager = GridLayoutManager(context, 3)
            adapter = mAdapter
        }
        observeList()
//        observeSubscribeChannel()
    }

    private fun observeList() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            val content = viewModel.loadUserChannels()
            content.collectLatest {
                mAdapter.submitData(it)
            }
        }
    }
    
    private fun observeSubscribeChannel() {
        observe(homeViewModel.subscriptionLiveData) { response ->
            when(response) {
                is Resource.Success -> {
                    trendingChannelInfo?.let {
                        val status = response.data.isSubscribed.takeIf { it == 1 } ?: -1
                        if(response.data.isSubscribed == 1) {
                            it.isSubscribed = 1
                            ++ it.subscriberCount
                        }
                        else {
                            it.isSubscribed = 0
                            -- it.subscriberCount
                        }
                        mAdapter.notifyItemRangeChanged(0, mAdapter.itemCount, it)
                        homeViewModel.updateSubscriptionCountTable(SubscriptionInfo(null, it.channelOwnerId, mPref.customerId), status)
                    }
                }
                is Resource.Failure -> {
                    requireContext().showToast(response.error.msg)
                }
            }
        }
    }
}
