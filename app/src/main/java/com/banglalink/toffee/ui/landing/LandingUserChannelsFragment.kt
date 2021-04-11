package com.banglalink.toffee.ui.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.map
import androidx.recyclerview.widget.LinearLayoutManager
import com.banglalink.toffee.R
import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.database.entities.SubscriptionInfo
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.databinding.FragmentLandingUserChannelsBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.listeners.LandingPopularChannelCallback
import com.banglalink.toffee.model.*
import com.banglalink.toffee.ui.category.CategoryDetailsFragment
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.common.UnSubscribeDialog
import com.banglalink.toffee.ui.home.LandingPageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class LandingUserChannelsFragment : HomeBaseFragment() {
    @Inject lateinit var localSync: LocalSync
    private var categoryInfo: Category? = null
    @Inject lateinit var cacheManager: CacheManager
    private var channelInfo: UserChannelInfo? = null
    private lateinit var mAdapter: LandingUserChannelsListAdapter
    private var _binding: FragmentLandingUserChannelsBinding? = null
    private val binding get() = _binding!!
    private val viewModel by activityViewModels<LandingPageViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLandingUserChannelsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onDestroyView() {
        binding.userChannelList.adapter = null
        super.onDestroyView()
        _binding = null
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryInfo = parentFragment?.arguments?.getParcelable(
            CategoryDetailsFragment.ARG_CATEGORY_ITEM
        )

        mAdapter = LandingUserChannelsListAdapter(object : LandingPopularChannelCallback<UserChannelInfo> {
            override fun onItemClicked(item: UserChannelInfo) {
                homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(item.channelOwnerId)
            }

            override fun onSubscribeButtonClicked(view: View, info: UserChannelInfo) {
//                channelInfo = info
                if (info.isSubscribed == 0) {
                    channelInfo = info.also { userChannelInfo ->
                        userChannelInfo.isSubscribed = 1
                        userChannelInfo.subscriberCount++
                    }
//                    subscriptionViewModel.setSubscriptionStatus(info.id, 1, info.channelOwnerId)
                    homeViewModel.sendSubscriptionStatus(SubscriptionInfo(null, info.channelOwnerId, mPref.customerId), 1)
                    mAdapter.notifyItemRangeChanged(0, mAdapter.itemCount, channelInfo)
                }
                else {
                    UnSubscribeDialog.show(requireContext()){
                        channelInfo = info.also { userChannelInfo ->
                            userChannelInfo.isSubscribed = 0
                            userChannelInfo.subscriberCount--
                        }
//                        subscriptionViewModel.setSubscriptionStatus(info.id, 0, info.channelOwnerId)
                        homeViewModel.sendSubscriptionStatus(SubscriptionInfo(null, info.channelOwnerId, mPref.customerId), -1)
                        mAdapter.notifyItemRangeChanged(0, mAdapter.itemCount, channelInfo)
                    }
                }
            }
        })

        binding.viewAllButton.setOnClickListener {
            parentFragment?.findNavController()?.navigate(R.id.action_menu_feed_to_trendingChannelsFragment)
        }

        with(binding.userChannelList) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = mAdapter
        }
        /*val userChannelList = PagingData.from(listOf(
            UserChannelInfo(contentProviderName = "Channel"),
            UserChannelInfo(contentProviderName = "Channel"),
            UserChannelInfo(contentProviderName = "Channel"),
            UserChannelInfo(contentProviderName = "Channel"),
        ))
        lifecycleScope.launch {
            mAdapterLanding.submitData(userChannelList)
        }*/
        observeList()
//        observeSubscribeChannel()
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
                mAdapter.submitData(it.map { userChannelInfo ->
                    localSync.syncUserChannel(userChannelInfo)
                    userChannelInfo
                })
            }
        }
    }

    private fun observeSubscribeChannel() {
        observe(homeViewModel.subscriptionLiveData) { response ->
            when(response) {
                is Resource.Success -> {
                    channelInfo?.let {
                        val status = response.data.isSubscribed.takeIf { it == 1 } ?: -1
                        if(response.data.isSubscribed == 1) {
                            it.isSubscribed = 1
                            ++ it.subscriberCount
                        }
                        else {
                            it.isSubscribed = 0
                            -- it.subscriberCount
                        }
                        mAdapter.notifyItemRangeChanged(0, mAdapter.itemCount, channelInfo)
                        homeViewModel.updateSubscriptionCountTable(SubscriptionInfo(null, it.channelOwnerId, mPref.customerId), status)
                    }
                }
                is Resource.Failure -> {
                    requireContext().showToast(response.error.msg)
                }
            }
        }
    }
    
    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {

    }
}
