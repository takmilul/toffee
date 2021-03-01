package com.banglalink.toffee.ui.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.banglalink.toffee.R
import com.banglalink.toffee.data.database.entities.SubscriptionInfo
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.*
import com.banglalink.toffee.ui.category.CategoryDetailsFragment
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.common.UnSubscribeDialog
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.ui.home.UserChannelsListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_landing_user_channels.*
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class LandingUserChannelsFragment : HomeBaseFragment() {
    @Inject lateinit var cacheManager: CacheManager
    private lateinit var mAdapter: UserChannelsListAdapter
    private var categoryInfo: UgcCategory? = null
    private var channelInfo: UgcUserChannelInfo? = null
    private val viewModel by activityViewModels<LandingPageViewModel>()
    private val subscriptionViewModel by viewModels<UserChannelViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_landing_user_channels, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryInfo = parentFragment?.arguments?.getParcelable(
            CategoryDetailsFragment.ARG_CATEGORY_ITEM
        )

        mAdapter = UserChannelsListAdapter(object : LandingPopularChannelCallback<UgcUserChannelInfo> {
            override fun onItemClicked(item: UgcUserChannelInfo) {
                homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(item.id.toInt(), item.channelOwnerId, item.isSubscribed)
            }

            override fun onSubscribeButtonClicked(view: View, info: UgcUserChannelInfo) {

                if (info.isSubscribed == 0) {
                    channelInfo = info.also { userChannelInfo ->
                        userChannelInfo.isSubscribed = 1
                        userChannelInfo.subscriberCount++
                    }
                    subscriptionViewModel.setSubscriptionStatus(info.id, 1, info.channelOwnerId)
                    viewModel.insertSubscribe(SubscriptionInfo(null,info.channelOwnerId,0,1))
                }
                else {
                    UnSubscribeDialog.show(requireContext()){
                        channelInfo = info.also { userChannelInfo ->
                            userChannelInfo.isSubscribed = 0
                            userChannelInfo.subscriberCount--
                        }
                        subscriptionViewModel.setSubscriptionStatus(info.id, 0, info.channelOwnerId)
                        viewModel.insertSubscribe(SubscriptionInfo(null,info.channelOwnerId,0,1))
                    }
                }
            }
        })

        viewAllButton.setOnClickListener {
            parentFragment?.findNavController()?.navigate(R.id.action_menu_feed_to_trendingChannelsFragment)
        }

        with(userChannelList) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = mAdapter
        }
        observeList()
    }

    private fun observeList() {
        lifecycleScope.launchWhenStarted {
            val content = if (categoryInfo == null) {
                viewModel.loadUserChannels
            }
            else {
                viewModel.loadUserChannelsByCategory(categoryInfo!!)
            }
            content.collectLatest {
                mAdapter.submitData(it)
            }
        }

        observe(subscriptionViewModel.subscriptionResponse) {
            if(it is Resource.Success) {
                cacheManager.clearSubscriptionCache()
                mAdapter.notifyItemRangeChanged(0, mAdapter.itemCount, channelInfo)
            }
            else requireContext().showToast("Failed to subscribe channel")
        }
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {

    }
}
