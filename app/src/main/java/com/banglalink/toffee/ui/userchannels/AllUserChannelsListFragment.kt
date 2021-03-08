package com.banglalink.toffee.ui.userchannels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.banglalink.toffee.R
import com.banglalink.toffee.data.database.entities.SubscriptionInfo
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.listeners.LandingPopularChannelCallback
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelNavParams
import com.banglalink.toffee.model.UserChannelInfo
import com.banglalink.toffee.ui.category.CategoryDetailsFragment
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.common.UnSubscribeDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_all_user_channels_list.*
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class AllUserChannelsListFragment : HomeBaseFragment() {
    
    private var categoryInfo: Category? = null
    @Inject lateinit var cacheManager: CacheManager
    private lateinit var mAdapter: AllUserChannelsListAdapter
    private var trendingChannelInfo: UserChannelInfo? = null
    private val viewModel by viewModels<AllUserChannelsListViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_all_user_channels_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryInfo = parentFragment?.arguments?.getParcelable(CategoryDetailsFragment.ARG_CATEGORY_ITEM)

        mAdapter = AllUserChannelsListAdapter(object : LandingPopularChannelCallback<UserChannelInfo> {
            override fun onItemClicked(item: UserChannelInfo) {
                homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(item.channelOwnerId)
            }

            override fun onSubscribeButtonClicked(view: View, info: UserChannelInfo) {
                if (info.isSubscribed == 0) {
                    trendingChannelInfo = info.also {
                        it.isSubscribed = 1
                        it.subscriberCount++
                    }
                    homeViewModel.sendSubscriptionStatus(SubscriptionInfo(null, info.channelOwnerId, mPref.customerId) ,1)
                    mAdapter.notifyItemRangeChanged(0, mAdapter.itemCount, trendingChannelInfo)
                }
                else {
                    UnSubscribeDialog.show(requireContext()) {
                        trendingChannelInfo = info.also {
                            it.isSubscribed = 0
                            it.subscriberCount--
                        }
                        homeViewModel.sendSubscriptionStatus(SubscriptionInfo(null, info.channelOwnerId, mPref.customerId) ,-1)
                        mAdapter.notifyItemRangeChanged(0, mAdapter.itemCount, trendingChannelInfo)
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
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {

    }
}
