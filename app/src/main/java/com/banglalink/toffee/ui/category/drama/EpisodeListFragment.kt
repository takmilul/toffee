package com.banglalink.toffee.ui.category.drama

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.ConcatAdapter
import com.banglalink.toffee.R
import com.banglalink.toffee.apiservice.DramaSeasonRequestParam
import com.banglalink.toffee.common.paging.ListLoadStateAdapter
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.data.database.entities.SubscriptionInfo
import com.banglalink.toffee.data.repository.SubscriptionCountRepository
import com.banglalink.toffee.data.repository.SubscriptionInfoRepository
import com.banglalink.toffee.databinding.FragmentEpisodeListBinding
import com.banglalink.toffee.enums.Reaction.Love
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelNavParams
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.SeriesPlaybackInfo
import com.banglalink.toffee.ui.common.*
import com.banglalink.toffee.ui.home.ChannelHeaderAdapter
import com.banglalink.toffee.ui.home.HomeActivity
import com.banglalink.toffee.ui.player.AddToPlaylistData
import com.banglalink.toffee.ui.report.ReportPopupFragment
import com.banglalink.toffee.ui.widget.MarginItemDecoration
import com.banglalink.toffee.ui.widget.MyPopupWindow
import com.suke.widget.SwitchButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EpisodeListFragment: HomeBaseFragment(), ProviderIconCallback<ChannelInfo> {
    private lateinit var mAdapter: EpisodeListAdapter
    private var currentItem: ChannelInfo? = null
    private var isSubscribed: Int = 0
    private var subscriberCount: Long = 0
    private var detailsAdapter: ChannelHeaderAdapter? = null
    private val mViewModel by viewModels<EpisodeListViewModel>()
    private var _binding: FragmentEpisodeListBinding ? = null
    private val binding get() = _binding!!
    @Inject lateinit var subscriptionInfoRepository: SubscriptionInfoRepository
    @Inject lateinit var subscriptionCountRepository: SubscriptionCountRepository

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
        _binding = FragmentEpisodeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSubscriptionStatus()
        setupHeader()
        setupList()
//        observeListState()
//        observeSubscribeChannel()
    }

    private fun setSubscriptionStatus() {
        lifecycleScope.launch {
            currentItem?.let {
                isSubscribed = if(subscriptionInfoRepository.getSubscriptionInfoByChannelId(it.channel_owner_id, mPref.customerId) != null) 1 else 0
                subscriberCount = subscriptionCountRepository.getSubscriberCount(it.channel_owner_id)
                it.isSubscribed = if(subscriptionInfoRepository.getSubscriptionInfoByChannelId(it.channel_owner_id, mPref.customerId) != null) 1 else 0
                it.subscriberCount = subscriptionCountRepository.getSubscriberCount(it.channel_owner_id).toInt()
                detailsAdapter?.notifyDataSetChanged()
            }
        }
    }

    fun getSeriesId() = seriesInfo.seriesId
    fun getPlaylistId() = seriesInfo.playlistId()

    fun isAutoplayEnabled(): Boolean {
        return view?.findViewById<SwitchButton>(R.id.autoPlaySwitch)?.isChecked == true
    }

    private fun setupHeader() {
        detailsAdapter = ChannelHeaderAdapter(seriesInfo, object:
            SeriesHeaderCallback {
            override fun onOpenMenu(view: View, item: ChannelInfo) {
                openMenu(view, item)
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

            override fun onShareClicked(view: View, item: ChannelInfo) {
                requireActivity().handleShare(item)
            }

            override fun onSubscribeButtonClicked(view: View, item: ChannelInfo) {
                if (isSubscribed == 0) {
                    homeViewModel.sendSubscriptionStatus(SubscriptionInfo(null, item.channel_owner_id, mPref.customerId), 1)
                    isSubscribed = 1
                    currentItem?.isSubscribed = isSubscribed
                    currentItem?.subscriberCount = (++subscriberCount).toInt()
                    detailsAdapter?.notifyDataSetChanged()
                }
                else {
                    UnSubscribeDialog.show(requireContext()){
                        homeViewModel.sendSubscriptionStatus(SubscriptionInfo(null, item.channel_owner_id, mPref.customerId), -1)
                        isSubscribed = 0
                        currentItem?.isSubscribed = isSubscribed
                        currentItem?.subscriberCount = (--subscriberCount).toInt()
                        detailsAdapter?.notifyDataSetChanged()
                    }
                }
            }

            override fun onProviderIconClicked(item: ChannelInfo) {
                super.onProviderIconClicked(item)
                homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(item.channel_owner_id)
            }

            override fun onSeasonChanged(newSeason: Int) {
                if(newSeason - 1 != mViewModel.selectedSeason.value) {
                    mViewModel.selectedSeason.value = newSeason - 1
                    observeList(newSeason)
                }
            }
        }, mPref, mViewModel)
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
                homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(item.channel_owner_id)
            }
        }, currentItem)

        with(binding.listview) {
            addItemDecoration(MarginItemDecoration(8))
            adapter = ConcatAdapter(detailsAdapter, mAdapter.withLoadStateFooter(ListLoadStateAdapter { mAdapter.retry() }))

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                mAdapter.loadStateFlow
//                    .distinctUntilChangedBy { it.refresh }
                    .collectLatest {
                        binding.progressBar.isVisible = it.source.refresh is LoadState.Loading

                        val list = mAdapter.snapshot()
                        homeViewModel.addToPlayListMutableLiveData.postValue(
                            AddToPlaylistData(getPlaylistId(), list.items, false)
                        )
                    }
            }
        }
        observeList(seriesInfo.seasonNo)
    }

    private fun observeList(currentSeasonNo: Int) {
        seasonListJob?.cancel()
        seasonListJob = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
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

    private fun observeSubscribeChannel() {
        observe(homeViewModel.subscriptionLiveData) { response ->
            when(response) {
                is Resource.Success -> {
                    currentItem?.let {
                        val status = response.data.isSubscribed.takeIf { it == 1 } ?: -1
                        if (response.data.isSubscribed == 1) {
                            it.isSubscribed = 1
                            ++ it.subscriberCount
                        }
                        else {
                            it.isSubscribed = 0
                            -- it.subscriberCount
                        }
                        mAdapter.notifyDataSetChanged()
                        homeViewModel.updateSubscriptionCountTable(SubscriptionInfo(null, it.channel_owner_id, mPref.customerId), status)
                    }
                }
                is Resource.Failure -> {
                    requireContext().showToast(response.error.msg)
                }
            }
        }
    }
    
    override fun onProviderIconClicked(item: ChannelInfo) {
        super.onProviderIconClicked(item)
        homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(item.channel_owner_id)
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

        popupMenu.menu.findItem(R.id.menu_share).isVisible = true
        popupMenu.setOnMenuItemClickListener{
            when(it?.itemId){
                R.id.menu_share->{
                    requireActivity().handleShare(channelInfo)
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_fav->{
                    requireActivity().handleFavorite(channelInfo)
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_report -> {
                    requireActivity().handleReport(channelInfo)
                    return@setOnMenuItemClickListener true
                }
                else->{
                    return@setOnMenuItemClickListener false
                }
            }
        }
        popupMenu.show()
    }

    fun setCurrentChannel(channelInfo: ChannelInfo?) {
        currentItem = channelInfo
        detailsAdapter?.setChannelInfo(channelInfo)
        mAdapter.setSelectedItem(channelInfo)
        setSubscriptionStatus()
    }
}