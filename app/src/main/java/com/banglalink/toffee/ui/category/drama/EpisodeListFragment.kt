package com.banglalink.toffee.ui.category.drama

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.ConcatAdapter
import com.banglalink.toffee.R
import com.banglalink.toffee.apiservice.DramaSeasonRequestParam
import com.banglalink.toffee.common.paging.ListLoadStateAdapter
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.databinding.FragmentEpisodeListBinding
import com.banglalink.toffee.enums.Reaction.Love
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.SeriesPlaybackInfo
import com.banglalink.toffee.ui.common.*
import com.banglalink.toffee.ui.home.CatchupDetailsViewModel
import com.banglalink.toffee.ui.home.ChannelHeaderAdapter
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.ui.player.AddToPlaylistData
import com.banglalink.toffee.ui.widget.MarginItemDecoration
import com.banglalink.toffee.ui.widget.MyPopupWindow
import com.suke.widget.SwitchButton
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch

class EpisodeListFragment: HomeBaseFragment(), ProviderIconCallback<ChannelInfo> {
    private lateinit var mAdapter: EpisodeListAdapter
    private var currentItem: ChannelInfo? = null
    private var detailsAdapter: ChannelHeaderAdapter? = null
    private val mViewModel by viewModels<EpisodeListViewModel>()
    private val playerViewModel by viewModels<CatchupDetailsViewModel>()
    private val landingViewModel by activityViewModels<LandingPageViewModel>()
    private lateinit var binding: FragmentEpisodeListBinding

    private var seasonListJob: Job? = null
    private lateinit var seriesInfo: SeriesPlaybackInfo

    companion object {
        const val SERIES_INFO = "series-info"

        fun newInstance(seriesPlaybackInfo: SeriesPlaybackInfo): EpisodeListFragment {
            return EpisodeListFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(SERIES_INFO, seriesPlaybackInfo)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        seriesInfo = requireArguments().getParcelable(SERIES_INFO)!!
        mViewModel.seasonList.value =  (1..(seriesInfo.totalSeason)).map { "Season $it" }
        mViewModel.selectedSeason.value = seriesInfo.seasonNo - 1
        currentItem = seriesInfo.currentItem
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEpisodeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observe(playerViewModel.channelSubscriberCount) {
            currentItem?.isSubscribed = if (playerViewModel.isChannelSubscribed.value!!) 1 else 0
            currentItem?.subscriberCount = it
            detailsAdapter?.notifyDataSetChanged()
        }
        
        setSubscriptionStatus()
        setupHeader()
        setupList()
        observeListState()
    }

    private fun setSubscriptionStatus() {
        currentItem?.let { channelInfo ->
            val customerId = mPref.customerId
            val isOwner = if (channelInfo.channel_owner_id == customerId) 1 else 0
            val isPublic = if (channelInfo.channel_owner_id == customerId) 0 else 1
            val channelId = channelInfo.channel_owner_id.toLong()
            playerViewModel.getChannelInfo(isOwner, isPublic, channelId, channelId.toInt())
        }
    }

    fun getSeriesId() = seriesInfo.seriesId
    fun getPlaylistId() = seriesInfo.playlistId()

    fun isAutoplayEnabled(): Boolean {
        return view?.findViewById<SwitchButton>(R.id.autoPlaySwitch)?.isChecked == true
    }

    private fun observeListState() {
        lifecycleScope.launch {
            mAdapter
                .loadStateFlow
                .distinctUntilChangedBy {
                    it.refresh
                }.collect {
                    val list = mAdapter.snapshot()
                    homeViewModel.addToPlayListMutableLiveData.postValue(
                        AddToPlaylistData(getPlaylistId(), list.items, false)
                    )
                }
        }
    }

    private fun setupHeader() {
        detailsAdapter = ChannelHeaderAdapter(seriesInfo, object:
            SeriesHeaderCallback {
            override fun onOpenMenu(view: View, item: ChannelInfo) {
                openMenu(view, item)
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

            override fun onShareClicked(view: View, item: ChannelInfo) {
                homeViewModel.shareContentLiveData.postValue(item)
            }

            override fun onSubscribeButtonClicked(view: View, item: ChannelInfo) {
                playerViewModel.toggleSubscriptionStatus(item.id.toInt(), item.channel_owner_id)
            }

            override fun onProviderIconClicked(item: ChannelInfo) {
                super.onProviderIconClicked(item)
                landingViewModel.navigateToMyChannel(this@EpisodeListFragment, item.id.toInt(), item.channel_owner_id, item.isSubscribed)
            }

            override fun onSeasonChanged(newSeason: Int) {
                if(newSeason - 1 != mViewModel.selectedSeason.value) {
                    mViewModel.selectedSeason.value = newSeason - 1
                    observeList(newSeason)
                }
            }
        }, mViewModel)
    }

    private fun setupList() {
        mAdapter = EpisodeListAdapter(object: ProviderIconCallback<ChannelInfo> {
            override fun onItemClicked(item: ChannelInfo) {
                if(item == currentItem || item.id == currentItem?.id) {
                    return
                }
                seriesInfo = seriesInfo.apply {
                    seasonNo = item.seasonNo
                    channelId = item.id.toInt()
                    currentItem = item
                }
                homeViewModel.addToPlayListMutableLiveData.postValue(
                    AddToPlaylistData(getPlaylistId(), mAdapter.snapshot().items)
                )
                homeViewModel.fragmentDetailsMutableLiveData.postValue(
                    seriesInfo
                )
            }

            override fun onOpenMenu(view: View, item: ChannelInfo) {
                openMenu(view, item)
            }
            
            override fun onProviderIconClicked(item: ChannelInfo) {
                super.onProviderIconClicked(item)
                landingViewModel.navigateToMyChannel(this@EpisodeListFragment, item.id.toInt(), item.channel_owner_id, item.isSubscribed)
            }
        }, currentItem)

        with(binding.listview) {
            addItemDecoration(MarginItemDecoration(8))
            adapter = ConcatAdapter(detailsAdapter, mAdapter.withLoadStateFooter(ListLoadStateAdapter { mAdapter.retry() }))

            mAdapter.addLoadStateListener {
                binding.progressBar.isVisible = it.source.refresh is LoadState.Loading

//                mAdapter.apply {
//                    val showEmpty = itemCount <= 0 && !it.source.refresh.endOfPaginationReached
//                    binding.emptyView.isGone = !showEmpty
//                    binding.listview.isVisible = !showEmpty
//                }
            }
        }
//        Log.e("PLAYLIST_DEBUG", "SETUP - ${seriesInfo.seasonNo}")
        observeList(seriesInfo.seasonNo)
    }

    private fun observeList(currentSeasonNo: Int) {
        seasonListJob?.cancel()
        seasonListJob = lifecycleScope.launchWhenStarted {
            mViewModel.getEpisodesBySeason(
                DramaSeasonRequestParam(
                    seriesInfo.type,
                    seriesInfo.seriesId,
                    currentSeasonNo
                ))
                .collectLatest {
                    mAdapter.submitData(it)
                }
        }
    }

    override fun onProviderIconClicked(item: ChannelInfo) {
        super.onProviderIconClicked(item)
        landingViewModel.navigateToMyChannel(this, item.id.toInt(), item.channel_owner_id, item.isSubscribed)
    }

    override fun onOpenMenu(view: View, item: ChannelInfo) {
        super.onOpenMenu(view, item)
        openMenu(view, item)
    }

    private fun openMenu(anchor: View, channelInfo: ChannelInfo) {
        val popupMenu = MyPopupWindow(requireContext(), anchor)
        popupMenu.inflate(R.menu.menu_catchup_item)

        if (channelInfo.favorite == null || channelInfo.favorite == "0") {
            popupMenu.menu.getItem(0).title = "Add to Favorites"
        }
        else {
            popupMenu.menu.getItem(0).title = "Remove from Favorites"
        }

        popupMenu.menu.findItem(R.id.menu_share).isVisible = false
        popupMenu.setOnMenuItemClickListener{
            when(it?.itemId){
                R.id.menu_share->{
                    homeViewModel.shareContentLiveData.postValue(channelInfo)
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_fav->{
                    homeViewModel.updateFavorite(channelInfo).observe(viewLifecycleOwner, { resp->
                        handleFavoriteResponse(resp)
                    })
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_not_interested->{
//                    removeItemNotInterestedItem(channelInfo)
                    return@setOnMenuItemClickListener true
                }
                else->{
                    return@setOnMenuItemClickListener false
                }
            }
        }
        popupMenu.show()
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {

    }

    fun setCurrentChannel(channelInfo: ChannelInfo?) {
        currentItem = channelInfo
        detailsAdapter?.setChannelInfo(channelInfo)
        mAdapter.setSelectedItem(channelInfo)
        setSubscriptionStatus()
    }
}