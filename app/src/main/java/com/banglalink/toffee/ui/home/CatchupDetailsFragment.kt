package com.banglalink.toffee.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState.Loading
import androidx.paging.filter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.apiservice.CatchupParams
import com.banglalink.toffee.common.paging.ListLoadStateAdapter
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.database.entities.SubscriptionInfo
import com.banglalink.toffee.databinding.FragmentCatchupBinding
import com.banglalink.toffee.enums.NativeAdAreaType
import com.banglalink.toffee.enums.Reaction.Love
import com.banglalink.toffee.extension.checkIfFragmentAttached
import com.banglalink.toffee.extension.checkVerification
import com.banglalink.toffee.extension.handleShare
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelNavParams
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.ContentReactionCallback
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.common.ReactionIconCallback
import com.banglalink.toffee.ui.common.ReactionPopup
import com.banglalink.toffee.ui.common.UnSubscribeDialog
import com.banglalink.toffee.ui.mychannel.MyChannelVideosViewModel
import com.banglalink.toffee.ui.nativead.NativeAdAdapter
import com.banglalink.toffee.ui.player.AddToPlaylistData
import com.banglalink.toffee.ui.widget.MarginItemDecoration
import com.banglalink.toffee.util.BindingUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CatchupDetailsFragment: HomeBaseFragment(), ContentReactionCallback<ChannelInfo> {
    
    @Inject lateinit var localSync: LocalSync
    private var mAdapter: ConcatAdapter? = null
    @Inject lateinit var bindingUtil: BindingUtil
    private var currentItem: ChannelInfo? = null
    private var _binding: FragmentCatchupBinding? = null
    private val binding get() = _binding!!
    private var headerAdapter: ChannelHeaderAdapter? = null
    private var detailsAdapter: CatchUpDetailsAdapterNew? = null
    private var nativeAdBuilder: NativeAdAdapter.Builder? = null
    private val viewModel by viewModels<CatchupDetailsViewModel>()
    private val myChannelVideosViewModel by activityViewModels<MyChannelVideosViewModel>()
    
    companion object{
        const val CHANNEL_INFO = "channel_info_"
        
        fun createInstance(channelInfo: ChannelInfo): CatchupDetailsFragment {
            return CatchupDetailsFragment().apply {
                arguments = Bundle().also {
                    it.putParcelable(CHANNEL_INFO, channelInfo)
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentItem = arguments?.getParcelable(CHANNEL_INFO)!!
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCatchupBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.progressBar.load(R.drawable.content_loader)
        lifecycleScope.launch {
            currentItem?.let { localSync.syncData(it, false, LocalSync.SYNC_FLAG_ALL) }
            _binding?.listview?.addItemDecoration(MarginItemDecoration(12))
            
            checkIfFragmentAttached {
                initAdapter()
                if (currentItem?.channel_owner_id == mPref.customerId) {
                    observeMyChannelVideos()
                } else {
                    observeList()
                }
                observeListState()
                observeSubscribeChannel()
            }
        }
    
        observe(mPref.isShowMoreToggled) {
            headerAdapter?.toggleShowMore(it)
        }
    }

    override fun onSubscribeButtonClicked(view: View, item: ChannelInfo) {
        super.onSubscribeButtonClicked(view, item)
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
            checkIfFragmentAttached {
                if (item.isSubscribed == 0) {
                    homeViewModel.sendSubscriptionStatus(SubscriptionInfo(null, item.channel_owner_id, mPref.customerId), 1)
                } else {
                    UnSubscribeDialog.show(requireContext()) {
                        homeViewModel.sendSubscriptionStatus(SubscriptionInfo(null, item.channel_owner_id, mPref.customerId), -1)
                    }
                }
            }
        }
    }
    
    private fun initAdapter() {
        val nativeAdSettings = mPref.nativeAdSettings.value?.find {
            it.area== NativeAdAreaType.RECOMMEND_VIDEO.value
        }
        val adUnitId = nativeAdSettings?.adUnitId
        val adInterval =  nativeAdSettings?.adInterval ?: 0
        val isAdActive = nativeAdSettings?.isActive ?:false
        val isNativeAdActive = mPref.isNativeAdActive && isAdActive && adInterval > 0 && !adUnitId.isNullOrBlank()
        
        headerAdapter = ChannelHeaderAdapter(currentItem, this@CatchupDetailsFragment, mPref)
        detailsAdapter = CatchUpDetailsAdapterNew(isNativeAdActive, adInterval, adUnitId, mPref, bindingUtil, 
            object : 
            ProviderIconCallback<ChannelInfo> {
            override fun onItemClicked(item: ChannelInfo) {
                checkIfFragmentAttached {
                    homeViewModel.playContentLiveData.postValue(item)
                }
            }
            
            override fun onOpenMenu(view: View, item: ChannelInfo) {
                super.onOpenMenu(view, item)
                onOptionClicked(view, item)
            }
            
            override fun onProviderIconClicked(item: ChannelInfo) {
                super.onProviderIconClicked(item)
                checkIfFragmentAttached {
                    homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(item.channel_owner_id)
                }
            }
        })
        
        mAdapter = ConcatAdapter(headerAdapter, detailsAdapter?.withLoadStateFooter(ListLoadStateAdapter{detailsAdapter?.retry()}))
        with(binding.listview) {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
    }
    
    private fun observeListState() {
        var isInitialized = false
        runCatching {
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                detailsAdapter?.loadStateFlow?.collect {
                    val list = detailsAdapter?.snapshot()
                    val isLoading = it.source.refresh is Loading || !isInitialized
                    val isEmpty = (list?.size ?: 0) <= 0 && !it.source.refresh.endOfPaginationReached
                    binding.progressBar.isVisible = isLoading && isEmpty
                    val emptyTextView = view?.findViewById<TextView>(R.id.empty_view_text_no_item_found)
                    emptyTextView?.isVisible = !isLoading && isEmpty
                    isInitialized = true
                    if (currentItem != null && (list?.size ?: 0) > 0) {
                        checkIfFragmentAttached {
                            homeViewModel.addToPlayListMutableLiveData.postValue(
                                AddToPlaylistData(-1, listOf(currentItem!!, list!!.items[0]))
                            )
                        }
                    }
                }
            }
        }
    }

    private fun observeMyChannelVideos() {
        viewLifecycleOwner.lifecycleScope.launch {
            currentItem?.channel_owner_id?.let {
                myChannelVideosViewModel.getMyChannelVideos(it).collectLatest {
                    detailsAdapter?.submitData(it.filter { channelInfo -> channelInfo.id != currentItem?.id && !channelInfo.isExpired })
                }
            }
        }
    }
    
    private fun observeList() {
        viewLifecycleOwner.lifecycleScope.launch {
            currentItem?.let {
                val catchupParams = CatchupParams(it.id, it.video_tags, it.categoryId, it.subCategoryId)
                viewModel.loadRelativeContent(catchupParams).collectLatest {
                    detailsAdapter?.submitData(it.filter { !it.isExpired })
                }
            }
        }
    }
    
    private fun observeSubscribeChannel() {
        checkIfFragmentAttached {
            observe(homeViewModel.subscriptionLiveData) { response ->
                when (response) {
                    is Resource.Success -> {
                        if (response.data == null) {
                            requireContext().showToast(getString(R.string.try_again_message))
                        } else {
                            currentItem?.apply {
                                isSubscribed = response.data?.isSubscribed ?: 0
                                subscriberCount = response.data?.subscriberCount ?: 0
                            }
                            headerAdapter?.notifyDataSetChanged()
                        }
                    }
                    
                    is Resource.Failure -> {
                        requireContext().showToast(response.error.msg)
                    }
                }
            }
        }
    }
    
    override fun onReactionClicked(view: View, reactionCountView: View, item: ChannelInfo) {
        super.onReactionClicked(view, reactionCountView, item)
        val iconLocation = IntArray(2)
        view.getLocationOnScreen(iconLocation)
        val reactionPopupFragment = ReactionPopup.newInstance(item, iconLocation, view.height, true).apply { setCallback(object : ReactionIconCallback {
            override fun onReactionChange(reactionCount: String, reactionText: String, reactionIcon: Int) {
                (reactionCountView as TextView).text = reactionCount
                (view as TextView).text = reactionText
                view.setCompoundDrawablesWithIntrinsicBounds(reactionIcon, 0, 0, 0)
                if (reactionText == Love.name) {
                    view.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
                }
                else{
                    view.setTextColor(ContextCompat.getColor(requireContext(), R.color.fixed_second_text_color))
                }
                Log.e(ReactionPopup.TAG, "setReaction: icon")
            }
        })
        }
        childFragmentManager.commit { add(reactionPopupFragment, ReactionPopup.TAG) }
    }

    override fun onProviderIconClicked(item: ChannelInfo) {
        super.onProviderIconClicked(item)
        checkIfFragmentAttached {
            homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(item.channel_owner_id)
        }
    }

    override fun onShareClicked(view: View, item: ChannelInfo, isPlaylist: Boolean) {
        super.onShareClicked(view, item, isPlaylist)
        requireActivity().handleShare(item)
    }
    
    override fun onOpenMenu(view: View, item: ChannelInfo) {
        super.onOpenMenu(view, item)
        onOptionClicked(view, item)
    }
    
    override fun showShareMenuItem(hide: Boolean): Boolean {
        return true
    }
    
    override fun onDestroyView() {
        nativeAdBuilder?.destroyAd()
        binding.listview.adapter = null
        nativeAdBuilder=null
        super.onDestroyView()
        _binding = null
    }
}