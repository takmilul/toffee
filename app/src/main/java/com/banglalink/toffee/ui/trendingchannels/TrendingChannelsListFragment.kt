package com.banglalink.toffee.ui.trendingchannels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.banglalink.toffee.R
import com.banglalink.toffee.data.database.entities.SubscriptionCount
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.*
import com.banglalink.toffee.ui.category.CategoryDetailsFragment
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.common.UnSubscribeDialog
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.ui.landing.LandingPopularChannelCallback
import com.banglalink.toffee.ui.landing.UserChannelViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_trending_channels_list.*
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class TrendingChannelsListFragment : HomeBaseFragment() {
    private lateinit var mAdapter: TrendingChannelsListAdapter
    private var categoryInfo: UgcCategory? = null
    @Inject lateinit var cacheManager: CacheManager
    private val viewModel by viewModels<TrendingChannelsListViewModel>()
    private val subscriptionViewModel by viewModels<UserChannelViewModel>()
    private val landingViewModel by viewModels<LandingPageViewModel>()
    private var trendingChannelInfo: TrendingChannelInfo? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_trending_channels_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryInfo = parentFragment?.arguments?.getParcelable(
            CategoryDetailsFragment.ARG_CATEGORY_ITEM
        )

        mAdapter = TrendingChannelsListAdapter(object : LandingPopularChannelCallback<TrendingChannelInfo> {
            override fun onItemClicked(item: TrendingChannelInfo) {
                homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(item.id.toInt(), item.channelOwnerId, item.isSubscribed)
            }

            override fun onSubscribeButtonClicked(view: View, info: TrendingChannelInfo) {
                if (info.isSubscribed == 0) {
                    trendingChannelInfo = info.also {
                        it.isSubscribed = 1
                        it.subscriberCount++
                    }
                    subscriptionViewModel.setSubscriptionStatus(info.id, 1, info.channelOwnerId)
                    landingViewModel.insertSubscribe(SubscriptionCount(null,info.channelOwnerId,0,1))
                }
                else {
                    UnSubscribeDialog.show(requireContext()) {
                        trendingChannelInfo = info.also {
                            it.isSubscribed = 0
                            it.subscriberCount--
                        }
                        subscriptionViewModel.setSubscriptionStatus(info.id, 0, info.channelOwnerId)
                        landingViewModel.insertSubscribe(SubscriptionCount(null,info.channelOwnerId,0,-1))
                    }
                }
            }
        })

        with(userChannelList) {
            layoutManager = GridLayoutManager(context, 3)
            adapter = mAdapter
        }
        observeList()
    }

    private fun observeList() {
        lifecycleScope.launchWhenStarted {
            val content = viewModel.loadUserChannels()
            content.collectLatest {
                mAdapter.submitData(it)
            }
        }

        observe(subscriptionViewModel.subscriptionResponse) {
            if(it is Resource.Success) {
                cacheManager.clearSubscriptionCache()
                mAdapter.notifyItemRangeChanged(0, mAdapter.itemCount, trendingChannelInfo)
            }
            else requireContext().showToast("Failed to subscribe channel")
        }
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {

    }
}
