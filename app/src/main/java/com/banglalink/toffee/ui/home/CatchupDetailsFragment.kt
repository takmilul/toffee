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
import com.banglalink.toffee.R
import com.banglalink.toffee.apiservice.CatchupParams
import com.banglalink.toffee.common.paging.ListLoadStateAdapter
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.data.database.entities.SubscriptionInfo
import com.banglalink.toffee.data.repository.SubscriptionCountRepository
import com.banglalink.toffee.data.repository.SubscriptionInfoRepository
import com.banglalink.toffee.databinding.FragmentCatchupBinding
import com.banglalink.toffee.enums.Reaction.Love
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelNavParams
import com.banglalink.toffee.ui.common.*
import com.banglalink.toffee.ui.mychannel.MyChannelVideosViewModel
import com.banglalink.toffee.ui.player.AddToPlaylistData
import com.banglalink.toffee.ui.widget.MarginItemDecoration
import com.suke.widget.SwitchButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CatchupDetailsFragment:HomeBaseFragment(), ContentReactionCallback<ChannelInfo> {
    private var isSubscribed: Int = 0
    private var subscriberCount: Long = 0
    private lateinit var mAdapter: ConcatAdapter
    private lateinit var currentItem: ChannelInfo
    private lateinit var catchupAdapter: CatchUpDetailsAdapter
    private lateinit var detailsAdapter: ChannelHeaderAdapter
    @Inject lateinit var subscriptionInfoRepository: SubscriptionInfoRepository
    @Inject lateinit var subscriptionCountRepository: SubscriptionCountRepository
    private val viewModel by viewModels<CatchupDetailsViewModel>()
    private val landingPageViewModel by activityViewModels<LandingPageViewModel>()
    private val myChannelVideosViewModel by activityViewModels<MyChannelVideosViewModel>()
    private lateinit var binding: FragmentCatchupBinding
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCatchupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        initAdapter()
        val channelId = currentItem.channel_owner_id

        lifecycleScope.launch {
            isSubscribed = if(subscriptionInfoRepository.getSubscriptionInfoByChannelId(channelId, mPref.customerId) != null) 1 else 0
            subscriberCount = subscriptionCountRepository.getSubscriberCount(channelId)
            currentItem.isSubscribed = isSubscribed
            currentItem.subscriberCount = subscriberCount.toInt()
            detailsAdapter.notifyDataSetChanged()
        }
        
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
    }

    fun isAutoPlayEnabled(): Boolean {
        return view?.findViewById<SwitchButton>(R.id.autoPlaySwitch)?.isChecked == true
    }

    override fun onSubscribeButtonClicked(view: View, item: ChannelInfo) {
        super.onSubscribeButtonClicked(view, item)
        if (isSubscribed == 0) {
            homeViewModel.sendSubscriptionStatus(SubscriptionInfo(null, item.channel_owner_id, mPref.customerId), 1)
            isSubscribed = 1
            currentItem.isSubscribed = isSubscribed
            currentItem.subscriberCount = (++subscriberCount).toInt()
            detailsAdapter.notifyDataSetChanged()
        }
        else{
            UnSubscribeDialog.show(requireContext()){
                homeViewModel.sendSubscriptionStatus(SubscriptionInfo(null, item.channel_owner_id, mPref.customerId), -1)
                isSubscribed = 0
                currentItem.isSubscribed = isSubscribed
                currentItem.subscriberCount = (--subscriberCount).toInt()
                detailsAdapter.notifyDataSetChanged()
            }
        }
    }
    
    private fun initAdapter() {
        catchupAdapter = CatchUpDetailsAdapter(object : ProviderIconCallback<ChannelInfo> {
            override fun onItemClicked(item: ChannelInfo) {
                homeViewModel.fragmentDetailsMutableLiveData.postValue(item)
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
        mAdapter = ConcatAdapter(detailsAdapter, catchupAdapter.withLoadStateFooter(ListLoadStateAdapter{catchupAdapter.retry()}))
    }

    private fun observeListState() {
        lifecycleScope.launch {
            catchupAdapter
                .loadStateFlow
                .distinctUntilChangedBy {
                    it.refresh
                }.collect {
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
        lifecycleScope.launchWhenStarted {
            myChannelVideosViewModel.getMyChannelVideos(currentItem.channel_owner_id).collectLatest {
                catchupAdapter.submitData(it.filter { channelInfo -> channelInfo.id != currentItem.id })
            }
        }
    }
    
    private fun observeList() {
        lifecycleScope.launchWhenStarted {
            val catchupParams = CatchupParams(currentItem.id, currentItem.video_tags, landingPageViewModel.categoryId
                .value ?: 0, landingPageViewModel.subCategoryId.value ?: 0)
            viewModel.loadRelativeContent(catchupParams).collectLatest {
                catchupAdapter.submitData(it)
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

    override fun onShareClicked(view: View, item: ChannelInfo) {
        super.onShareClicked(view, item)
        homeViewModel.shareContentLiveData.postValue(item)
    }
    
    override fun onOpenMenu(view: View, item: ChannelInfo) {
        super.onOpenMenu(view, item)
        onOptionClicked(view, item)
    }

    override fun hideShareMenuItem(hide: Boolean): Boolean {
        return true
    }
    
    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {

    }
}