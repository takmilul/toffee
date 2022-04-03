package com.banglalink.toffee.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.filter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.banglalink.toffee.R
import com.banglalink.toffee.apiservice.CatchupParams
import com.banglalink.toffee.common.paging.ListLoadStateAdapter
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.database.entities.SubscriptionInfo
import com.banglalink.toffee.databinding.FragmentCatchupBinding
import com.banglalink.toffee.enums.NativeAdType.SMALL
import com.banglalink.toffee.enums.Reaction.Love
import com.banglalink.toffee.extension.checkVerification
import com.banglalink.toffee.extension.handleShare
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelNavParams
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.*
import com.banglalink.toffee.ui.mychannel.MyChannelVideosViewModel
import com.banglalink.toffee.ui.nativead.NativeAdAdapter
import com.banglalink.toffee.ui.player.AddToPlaylistData
import com.banglalink.toffee.ui.widget.MarginItemDecoration
import com.banglalink.toffee.util.BindingUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CatchupDetailsFragment: HomeBaseFragment(), ContentReactionCallback<ChannelInfo> {
    @Inject lateinit var localSync: LocalSync
    private lateinit var mAdapter: ConcatAdapter
    @Inject lateinit var bindingUtil: BindingUtil
    private lateinit var currentItem: ChannelInfo
    private var _binding: FragmentCatchupBinding ? = null
    private val binding get() = _binding!!
    private lateinit var detailsAdapter: ChannelHeaderAdapter
    private lateinit var catchupAdapter: CatchUpDetailsAdapter
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
        lifecycleScope.launch {
            localSync.syncData(currentItem)
            initAdapter()
            
            with(binding.listview) {
                addItemDecoration(MarginItemDecoration(12))
                layoutManager = LinearLayoutManager(context)
                adapter = mAdapter
            }
    
            if (currentItem.channel_owner_id == mPref.customerId){
                observeMyChannelVideos()
            } else {
                observeList()
            }
            observeListState()
            observeSubscribeChannel()
        }
    }

    override fun onSubscribeButtonClicked(view: View, item: ChannelInfo) {
        super.onSubscribeButtonClicked(view, item)
        requireActivity().checkVerification {
            if (item.isSubscribed == 0) {
                homeViewModel.sendSubscriptionStatus(SubscriptionInfo(null, item.channel_owner_id, mPref.customerId), 1)
            } else {
                UnSubscribeDialog.show(requireContext()) {
                    homeViewModel.sendSubscriptionStatus(SubscriptionInfo(null, item.channel_owner_id, mPref.customerId), -1)
                }
            }
        }
    }
    
    private fun initAdapter() {
        catchupAdapter = CatchUpDetailsAdapter(object : ProviderIconCallback<ChannelInfo> {
            override fun onItemClicked(item: ChannelInfo) {
                homeViewModel.playContentLiveData.postValue(item)
            }

            override fun onOpenMenu(view: View, item: ChannelInfo) {
                super.onOpenMenu(view, item)
                onOptionClicked(view, item)
            }

            override fun onProviderIconClicked(item: ChannelInfo) {
                super.onProviderIconClicked(item)
                homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(item.channel_owner_id)
            }
        })
        detailsAdapter = ChannelHeaderAdapter(currentItem, this, mPref)
        
        val recommendedAdUnitId = mPref.recommendedNativeAdUnitId.value
        if (mPref.isRecommendedAdActive && mPref.recommendedAdInterval > 0 && !recommendedAdUnitId.isNullOrBlank()) {
            nativeAdBuilder = NativeAdAdapter.Builder.with(recommendedAdUnitId, catchupAdapter as Adapter<ViewHolder>, SMALL)
            val nativeAdAdapter = nativeAdBuilder!!.adItemInterval(mPref.recommendedAdInterval).build(bindingUtil)
            mAdapter = ConcatAdapter(detailsAdapter, nativeAdAdapter)
        } else {
            mAdapter = ConcatAdapter(detailsAdapter, catchupAdapter.withLoadStateFooter(ListLoadStateAdapter{catchupAdapter.retry()}))
        }
    }
    
    private fun observeListState() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            catchupAdapter.loadStateFlow.collect {
                val list = catchupAdapter.snapshot()
                if(list.size > 0) {
                    homeViewModel.addToPlayListMutableLiveData.postValue(
                        AddToPlaylistData(-1, listOf(currentItem, list.items[0]))
                    )
                }
            }
        }
    }

    private fun observeMyChannelVideos() {
        viewLifecycleOwner.lifecycleScope.launch {
            myChannelVideosViewModel.getMyChannelVideos(currentItem.channel_owner_id).collectLatest {
                catchupAdapter.submitData(it.filter { channelInfo -> channelInfo.id != currentItem.id && !channelInfo.isExpired })
            }
        }
    }
    
    private fun observeList() {
        viewLifecycleOwner.lifecycleScope.launch {
            val catchupParams = CatchupParams(currentItem.id, currentItem.video_tags, currentItem.categoryId, currentItem.subCategoryId)
            viewModel.loadRelativeContent(catchupParams).collectLatest {
                catchupAdapter.submitData(it.filter { !it.isExpired })
            }
        }
    }
    
    private fun observeSubscribeChannel() {
        observe(homeViewModel.subscriptionLiveData) { response ->
            when(response) {
                is Resource.Success -> {
                    currentItem.apply {
                        isSubscribed = response.data.isSubscribed
                        subscriberCount = response.data.subscriberCount
                    }
                    detailsAdapter.notifyDataSetChanged()
                }
                is Resource.Failure -> {
                    requireContext().showToast(response.error.msg)
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
                Log.e(ReactionPopup.TAG, "setReaction: icon", )
            }
        })
        }
        childFragmentManager.commit { add(reactionPopupFragment, ReactionPopup.TAG) }
    }

    override fun onProviderIconClicked(item: ChannelInfo) {
        super.onProviderIconClicked(item)
        homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(item.channel_owner_id)
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