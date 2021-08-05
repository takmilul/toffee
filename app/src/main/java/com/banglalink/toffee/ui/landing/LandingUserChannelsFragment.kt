package com.banglalink.toffee.ui.landing

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.map
import com.banglalink.toffee.R
import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.database.entities.SubscriptionInfo
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.databinding.FragmentLandingUserChannelsBinding
import com.banglalink.toffee.databinding.PlaceholderUserChannelsBinding
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.listeners.LandingPopularChannelCallback
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.MyChannelNavParams
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.UserChannelInfo
import com.banglalink.toffee.ui.category.CategoryDetailsFragment
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.common.UnSubscribeDialog
import com.banglalink.toffee.ui.home.LandingPageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class LandingUserChannelsFragment : HomeBaseFragment(), LandingPopularChannelCallback<UserChannelInfo> {
    
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
        var isInitialized = false
        categoryInfo = parentFragment?.arguments?.getParcelable(
            CategoryDetailsFragment.ARG_CATEGORY_ITEM
        )
    
        mAdapter = LandingUserChannelsListAdapter(this)

        binding.viewAllButton.setOnClickListener {
            parentFragment?.findNavController()?.navigate(R.id.trendingChannelsFragment)
        }
    
        with(binding.placeholder) {
            val calculatedSize = (Resources.getSystem().displayMetrics.widthPixels - (16.px * 4)) / 3.5    // 16dp margin
            this.forEach { placeholderView ->
                val binder = DataBindingUtil.bind<PlaceholderUserChannelsBinding>(placeholderView)
                binder?.let {
                    it.container.layoutParams.width = calculatedSize.toInt()
                    it.iconHolder.layoutParams.apply {
                        width = calculatedSize.toInt() - 16
                        height = calculatedSize.toInt() - 16
                    }
                }
            }
        }
    
        with(binding.userChannelList) {
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                mAdapter.loadStateFlow.collectLatest {
                    val isLoading = it.source.refresh is LoadState.Loading || !isInitialized
                    val isEmpty = mAdapter.itemCount <= 0 && ! it.source.refresh.endOfPaginationReached
                    binding.placeholder.isVisible = isEmpty
                    binding.userChannelList.isVisible = ! isEmpty
                    binding.placeholder.showLoadingAnimation(isLoading)
                    isInitialized = true
                }
            }
            adapter = mAdapter
            setHasFixedSize(true)
        }
        
        observeList()
//        observeSubscribeChannel()
    }
    
    private fun observeList() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
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
    
    override fun onItemClicked(item: UserChannelInfo) {
        homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(item.channelOwnerId)
    }
    
    override fun onSubscribeButtonClicked(view: View, info: UserChannelInfo) {
        requireActivity().checkVerification {
//                channelInfo = info
            if (info.isSubscribed == 0) {
                channelInfo = info.also { userChannelInfo ->
                    userChannelInfo.isSubscribed = 1
                    userChannelInfo.subscriberCount++
                }
//                    subscriptionViewModel.setSubscriptionStatus(info.id, 1, info.channelOwnerId)
                homeViewModel.sendSubscriptionStatus(
                    SubscriptionInfo(
                        null,
                        info.channelOwnerId,
                        mPref.customerId
                    ), 1
                )
                mAdapter.notifyItemRangeChanged(0, mAdapter.itemCount, channelInfo)
            } else {
                UnSubscribeDialog.show(requireContext()) {
                    channelInfo = info.also { userChannelInfo ->
                        userChannelInfo.isSubscribed = 0
                        userChannelInfo.subscriberCount--
                    }
//                        subscriptionViewModel.setSubscriptionStatus(info.id, 0, info.channelOwnerId)
                    homeViewModel.sendSubscriptionStatus(
                        SubscriptionInfo(
                            null,
                            info.channelOwnerId,
                            mPref.customerId
                        ), -1
                    )
                    mAdapter.notifyItemRangeChanged(0, mAdapter.itemCount, channelInfo)
                }
            }
        }
    }
}
