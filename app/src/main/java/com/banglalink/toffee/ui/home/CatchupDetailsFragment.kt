package com.banglalink.toffee.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.ListLoadStateAdapter
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.enums.Reaction.Love
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.*
import com.banglalink.toffee.ui.player.AddToPlaylistData
import com.banglalink.toffee.ui.widget.MarginItemDecoration
import com.suke.widget.SwitchButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_catchup.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CatchupDetailsFragment:HomeBaseFragment(), ContentReactionCallback<ChannelInfo> {
    private lateinit var mAdapter: ConcatAdapter
    private lateinit var catchupAdapter: CatchUpDetailsAdapter
    private lateinit var detailsAdapter: ChannelHeaderAdapter
    private lateinit var currentItem: ChannelInfo

    private val viewModel by viewModels<CatchupDetailsViewModel>()
    private val landingViewModel by activityViewModels<LandingPageViewModel>()

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
    ): View? {
        return inflater.inflate(R.layout.fragment_catchup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        initAdapter()

        viewModel.isChannelSubscribed.value = currentItem.isSubscribed == 1
        
        observe(viewModel.channelSubscriberCount) {
            currentItem.isSubscribed = if(viewModel.isChannelSubscribed.value!!) 1 else 0
            currentItem.subscriberCount = it
            detailsAdapter.notifyDataSetChanged()
        }

        val customerId = mPref.customerId
        val isOwner = if (currentItem.channel_owner_id == customerId) 1 else 0
        val isPublic = if (currentItem.channel_owner_id == customerId) 0 else 1
        val channelId = currentItem.channel_owner_id.toLong()
        viewModel.getChannelInfo(isOwner, isPublic, channelId, channelId.toInt())
        
        with(listview) {
            addItemDecoration(MarginItemDecoration(8))
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }

        observeList()
        observeListState()
    }

    fun isAutoplayEnabled(): Boolean {
        return view?.findViewById<SwitchButton>(R.id.autoPlaySwitch)?.isChecked == true
    }

    override fun onSubscribeButtonClicked(view: View, item: ChannelInfo) {
        super.onSubscribeButtonClicked(view, item)
        if (viewModel.isChannelSubscribed.value == false) {
            viewModel.toggleSubscriptionStatus(item.id.toInt(), item.channel_owner_id)
        }
        else{
            UnSubscribeDialog.show(requireContext()){
                viewModel.toggleSubscriptionStatus(item.id.toInt(), item.channel_owner_id)
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
                landingViewModel.navigateToMyChannel(this@CatchupDetailsFragment, item.id.toInt(), item.channel_owner_id, item.isSubscribed)
            }
        })
        detailsAdapter = ChannelHeaderAdapter(currentItem, this)
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

    private fun observeList() {
        lifecycleScope.launchWhenStarted {
            viewModel.loadRelativeContent(currentItem).collectLatest {
                catchupAdapter.submitData(it)
            }
        }
    }

    override fun onReactionClicked(view: View, reactionCountView: View, item: ChannelInfo) {
        super.onReactionClicked(view, reactionCountView, item)
        ReactionFragment.newInstance(item).apply { setCallback(object : ReactionIconCallback {
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
            }
        }) }.show(requireActivity().supportFragmentManager, ReactionFragment.TAG)
    }

    /*override fun onReactionLongPressed(view: View, reactionCountView: View, item: ChannelInfo) {
        super.onReactionLongPressed(view, reactionCountView, item)
        requireActivity().supportFragmentManager.beginTransaction().add(ReactionFragment.newInstance(view, reactionCountView, item), ReactionFragment.TAG).commit()
    }*/
    
    override fun onProviderIconClicked(item: ChannelInfo) {
        super.onProviderIconClicked(item)
        landingViewModel.navigateToMyChannel(this, item.id.toInt(), item.channel_owner_id, item.isSubscribed)
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