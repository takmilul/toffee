package com.banglalink.toffee.ui.landing

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.map
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.database.entities.SubscriptionInfo
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.data.repository.SubscriptionInfoRepository
import com.banglalink.toffee.databinding.FragmentLandingUserChannelsBinding
import com.banglalink.toffee.databinding.PlaceholderUserChannelsBinding
import com.banglalink.toffee.extension.checkVerification
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.px
import com.banglalink.toffee.extension.showLoadingAnimation
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.listeners.LandingPopularChannelCallback
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.MyChannelNavParams
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.UserChannelInfo
import com.banglalink.toffee.ui.category.CategoryDetailsFragment
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.common.UnSubscribeDialog
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.ui.subscription.SubscribedChannelsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LandingUserChannelsFragment : HomeBaseFragment(), LandingPopularChannelCallback<UserChannelInfo> {
    
    @Inject lateinit var localSync: LocalSync
    private var categoryInfo: Category? = null
    private var subscribedItemPosition: Int = -1
    @Inject lateinit var cacheManager: CacheManager
    private var channelInfo: UserChannelInfo? = null
    private lateinit var mAdapter: LandingUserChannelsListAdapter
    private var _binding: FragmentLandingUserChannelsBinding? = null
    private val binding get() = _binding!!
    @Inject lateinit var subscriptionInfoRepository: SubscriptionInfoRepository
    private val viewModel by activityViewModels<LandingPageViewModel>()
    private val subscribedChannelsViewModel by activityViewModels<SubscribedChannelsViewModel>()

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
            parentFragment?.findNavController()?.navigate(R.id.allUserChannelsFragment)
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
        observeSubscribedChannels()
        observeSubscribeChannel()
    }
    
    private fun observeSubscribedChannels() {
        lifecycleScope.launch {
            if (mPref.isVerifiedUser && subscribedChannelsViewModel.subscribedChannelLiveData.value == null) {
                observe(subscribedChannelsViewModel.subscribedChannelLiveData) {
                    observeList()
                }
                subscribedChannelsViewModel.syncSubscribedChannels()
            } else {
                observeList()
            }
        }
    }
    
    private fun observeList() {
        viewLifecycleOwner.lifecycleScope.launch {
            val content = if (categoryInfo == null) {
                viewModel.loadUserChannels()
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
                    if (response.data != null) {
                        channelInfo?.apply {
                            isSubscribed = response.data?.isSubscribed ?: 0
                            subscriberCount = response.data?.subscriberCount ?: 0
                        }
                        mAdapter.notifyItemChanged(subscribedItemPosition, channelInfo)
                    } else {
                        requireContext().showToast(getString(R.string.try_again_message))
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
    
    override fun onSubscribeButtonClicked(view: View, info: UserChannelInfo, position: Int) {
        if (!mPref.isVerifiedUser){
            ToffeeAnalytics.toffeeLogEvent(
                ToffeeEvents.LOGIN_SOURCE,
                bundleOf(
                    "source" to "follow_channel",
                    "method" to "mobile"
                )
            )
        }
        requireActivity().checkVerification {
            channelInfo = info
            subscribedItemPosition = position
            if (info.isSubscribed == 0) {
                homeViewModel.sendSubscriptionStatus(SubscriptionInfo(null, info.channelOwnerId, mPref.customerId), 1)
            } else {
                UnSubscribeDialog.show(requireContext()) {
                    homeViewModel.sendSubscriptionStatus(SubscriptionInfo(null, info.channelOwnerId, mPref.customerId), -1)
                }
            }
        }
    }
}
