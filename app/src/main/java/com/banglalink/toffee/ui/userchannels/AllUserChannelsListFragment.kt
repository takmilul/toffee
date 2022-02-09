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
import com.banglalink.toffee.apiservice.BrowsingScreens
import com.banglalink.toffee.common.paging.BaseListRepositoryImpl
import com.banglalink.toffee.data.database.LocalSync
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
class AllUserChannelsListFragment : HomeBaseFragment(), LandingPopularChannelCallback<UserChannelInfo> {
    private var index = -1
    private var initialPage = 0
    @Inject lateinit var localSync: LocalSync
    private var categoryInfo: Category? = null
    private var subscribedItemPosition: Int = -1
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
        landingViewModel.pageName.value = BrowsingScreens.ALL_USER_CHANNELS_PAGE
        landingViewModel.categoryId.value = 0
        
        categoryInfo = parentFragment?.arguments?.getParcelable(CategoryDetailsFragment.ARG_CATEGORY_ITEM)
        mAdapter = AllUserChannelsListAdapter(this)
        
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
            val gridLayoutManager = object : GridLayoutManager(context, 3, VERTICAL, false) { 
                override fun onLayoutCompleted(state: RecyclerView.State?) { 
                    super.onLayoutCompleted(state)
                    if (index != -1) {
                        binding.userChannelList.smoothScrollToPosition(index)
                        index = -1
                    }
                }
            }
            userChannelList.layoutManager = gridLayoutManager
            userChannelList.adapter = mAdapter
            userChannelList.setHasFixedSize(true)
            userChannelList.adapter?.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
        observeList()
        observeSubscribeChannel()
    }
    
    override fun onPause() {
        super.onPause()
        // calculate the visible item position and page to show the proper item visible when navigating back from other individual item detail page
        index = (binding.userChannelList.layoutManager as GridLayoutManager).findFirstCompletelyVisibleItemPosition()
        if (initialPage > 0) {
            initialPage += index.div(BaseListRepositoryImpl.PAGE_SIZE) - 1
            index = index.rem(BaseListRepositoryImpl.PAGE_SIZE).plus(initialPage * BaseListRepositoryImpl.PAGE_SIZE)
        } else {
            initialPage = index.div(BaseListRepositoryImpl.PAGE_SIZE)
        }
    }
    
    private fun observeList() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadUserChannels(initialPage).collectLatest {
                mAdapter.submitData(it)
            }
        }
    }
    
    private fun observeSubscribeChannel() {
        observe(homeViewModel.subscriptionLiveData) { response ->
            when(response) {
                is Resource.Success -> {
                    trendingChannelInfo?.apply {
                        isSubscribed = response.data.isSubscribed
                        subscriberCount = response.data.subscriberCount
                    }
                    mAdapter.notifyItemChanged(subscribedItemPosition, trendingChannelInfo)
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
            trendingChannelInfo = info
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
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
