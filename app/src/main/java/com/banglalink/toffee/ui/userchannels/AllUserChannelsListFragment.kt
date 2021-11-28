package com.banglalink.toffee.ui.userchannels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.data.database.entities.SubscriptionInfo
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.databinding.FragmentAllUserChannelsListBinding
import com.banglalink.toffee.enums.PageType
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
import com.banglalink.toffee.ui.home.LandingPageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AllUserChannelsListFragment : HomeBaseFragment() {
    private var top = -1
    private var index = -1
    private var categoryInfo: Category? = null
    @Inject lateinit var cacheManager: CacheManager
    private var trendingChannelInfo: UserChannelInfo? = null
    private lateinit var mAdapter: AllUserChannelsListAdapter
    private var _binding: FragmentAllUserChannelsListBinding ? = null
    private val binding get() = _binding!!
    private val landingViewModel by activityViewModels<LandingPageViewModel>()
    private val viewModel by viewModels<AllUserChannelsListViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAllUserChannelsListBinding.inflate(inflater, container, false)
        return binding.root    
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var isInitialized = false
        requireActivity().title = "User Channels"
        landingViewModel.pageType.value = PageType.Channel
        landingViewModel.categoryId.value = 0
        
        categoryInfo = parentFragment?.arguments?.getParcelable(CategoryDetailsFragment.ARG_CATEGORY_ITEM)
        mAdapter = AllUserChannelsListAdapter(object : LandingPopularChannelCallback<UserChannelInfo> {
            override fun onItemClicked(item: UserChannelInfo) {
                homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(item.channelOwnerId)
            }

            override fun onSubscribeButtonClicked(view: View, info: UserChannelInfo) {
                requireActivity().checkVerification {
                    if (info.isSubscribed == 0) {
                        trendingChannelInfo = info.also {
                            it.isSubscribed = 1
                            it.subscriberCount++
                        }
                        mAdapter.notifyItemRangeChanged(0, mAdapter.itemCount, trendingChannelInfo)
                        homeViewModel.sendSubscriptionStatus(SubscriptionInfo(null, info.channelOwnerId, mPref.customerId), 1)
                    } else {
                        UnSubscribeDialog.show(requireContext()) {
                            trendingChannelInfo = info.also {
                                it.isSubscribed = 0
                                it.subscriberCount--
                            }
                            mAdapter.notifyItemRangeChanged(0, mAdapter.itemCount, trendingChannelInfo)
                            homeViewModel.sendSubscriptionStatus(SubscriptionInfo(null, info.channelOwnerId, mPref.customerId), -1)
                        }
                    }
                }
            }
        })

        with(binding) {
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                mAdapter.loadStateFlow.collectLatest {
                    val isEmpty = mAdapter.itemCount <= 0 && ! it.source.refresh.endOfPaginationReached
                    val isLoading = it.source.refresh is LoadState.Loading || !isInitialized
                    footerLoader.isVisible = it.source.append is LoadState.Loading
                    emptyView.isVisible = isEmpty && !isLoading
                    progressBar.isVisible = isLoading
                    userChannelList.isVisible = !isEmpty && !isLoading
                    isInitialized = true
                }
            }
            userChannelList.adapter = mAdapter
            userChannelList.setHasFixedSize(true)
            userChannelList.adapter?.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
        observeList(index)
//        observeSubscribeChannel()
    }

    override fun onPause() {
        super.onPause()
        index = (binding.userChannelList.layoutManager as GridLayoutManager).findFirstVisibleItemPosition()
        val topView: View = binding.userChannelList.getChildAt(0)
        top = topView.top - binding.userChannelList.paddingTop
    }
    
    override fun onResume() {
        super.onResume()
        if (index != -1) {
            binding.userChannelList.smoothScrollToPosition(index)
        }
    }
    
    private fun observeList(index: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadUserChannels(index.div(30)).collectLatest {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
