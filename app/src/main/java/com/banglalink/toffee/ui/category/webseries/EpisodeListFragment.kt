package com.banglalink.toffee.ui.category.webseries

import android.os.Bundle
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
import androidx.paging.filter
import androidx.recyclerview.widget.ConcatAdapter
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.apiservice.DramaSeasonRequestParam
import com.banglalink.toffee.common.paging.ListLoadStateAdapter
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.database.entities.SubscriptionInfo
import com.banglalink.toffee.data.repository.SubscriptionCountRepository
import com.banglalink.toffee.data.repository.SubscriptionInfoRepository
import com.banglalink.toffee.databinding.FragmentEpisodeListBinding
import com.banglalink.toffee.enums.Reaction.Love
import com.banglalink.toffee.extension.checkVerification
import com.banglalink.toffee.extension.handleAddToPlaylist
import com.banglalink.toffee.extension.handleFavorite
import com.banglalink.toffee.extension.handleReport
import com.banglalink.toffee.extension.handleShare
import com.banglalink.toffee.extension.handleUrlShare
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelNavParams
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.SeriesPlaybackInfo
import com.banglalink.toffee.model.ShareableData
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.common.ReactionIconCallback
import com.banglalink.toffee.ui.common.ReactionPopup
import com.banglalink.toffee.ui.common.SeriesHeaderCallback
import com.banglalink.toffee.ui.common.UnSubscribeDialog
import com.banglalink.toffee.ui.home.ChannelHeaderAdapter
import com.banglalink.toffee.ui.player.AddToPlaylistData
import com.banglalink.toffee.ui.widget.MarginItemDecoration
import com.banglalink.toffee.ui.widget.MyPopupWindow
import com.banglalink.toffee.util.EncryptionUtil
import com.suke.widget.SwitchButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@AndroidEntryPoint
class EpisodeListFragment: HomeBaseFragment(), ProviderIconCallback<ChannelInfo> {
    @Inject lateinit var json: Json
    private var isSubscribed: Int = 0
    private var subscriberCount: Long = 0
    private var seasonListJob: Job? = null
    @Inject lateinit var localSync: LocalSync
    private var currentItem: ChannelInfo? = null
    private lateinit var mAdapter: EpisodeListAdapter
    private lateinit var seriesInfo: SeriesPlaybackInfo
    private var detailsAdapter: ChannelHeaderAdapter? = null
    private var _binding: FragmentEpisodeListBinding ? = null
    private val binding get() = _binding!!
    @Inject lateinit var subscriptionInfoRepository: SubscriptionInfoRepository
    @Inject lateinit var subscriptionCountRepository: SubscriptionCountRepository
    private val mViewModel by viewModels<EpisodeListViewModel>()
    
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
        val seasonList = seriesInfo.activeSeasonList?.map { "Season $it" } ?: listOf("Season 1")
        mViewModel.seasonList.value =  seasonList
        mViewModel.selectedSeason.value = minOf(seriesInfo.seasonNo - 1, seasonList.size - 1)
        currentItem = seriesInfo.currentItem
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEpisodeListBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressBar.load(R.drawable.content_loader)
        setSubscriptionStatus()
        setupHeader()
        setupList()
//        observeListState()
        observeSubscribeChannel()
    }
    
    private fun setSubscriptionStatus() {
        lifecycleScope.launch {
//            currentItem?.let {
//                localSync.syncData(it)
//            }
            detailsAdapter?.notifyDataSetChanged()
        }
    }
    
    fun getSeriesId() = seriesInfo.seriesId
    fun getSeasonNo() = seriesInfo.seasonNo
    fun getPlaylistId() = seriesInfo.playlistId()
    
    fun isAutoplayEnabled(): Boolean {
        return view?.findViewById<SwitchButton>(R.id.autoPlaySwitch)?.isChecked == true
    }
    
    private fun setupHeader() {
        detailsAdapter = ChannelHeaderAdapter(seriesInfo, object: SeriesHeaderCallback {
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
                    }
                })
                }
                childFragmentManager.commit { add(reactionPopupFragment, ReactionPopup.TAG) }
            }
            
            override fun onShareClicked(view: View, item: ChannelInfo, isPlaylist: Boolean) {
                val channelInfo = seriesInfo.currentItem
                if (isPlaylist && channelInfo != null) {
                    try {
                        var shareUrl = seriesInfo.shareUrl
                        val hash = shareUrl?.substringAfter("data=")?.trim()
                        hash?.let {
                            val shareableData = json.decodeFromString<ShareableData>(EncryptionUtil.decryptResponse(it).trimIndent())
                            val currentSeasonNo = seriesInfo.activeSeasonList?.getOrElse(mViewModel.selectedSeason.value ?: 0){1} ?: 1
                            if (shareableData.seasonNo != currentSeasonNo) {
                                val newShareableData = shareableData.copy(seasonNo = currentSeasonNo)
                                val jsonString = json.encodeToString(newShareableData)
                                val prefix = shareUrl?.substringBefore("data=")?.trim()?.plus("data=")
                                shareUrl = prefix.plus(EncryptionUtil.encryptRequest(jsonString))
                            }
                            shareUrl?.let { requireActivity().handleUrlShare(it) }
                        }
                    } catch (e: Exception) {
                        requireActivity().handleShare(channelInfo)
                    }
                } else {
                    requireActivity().handleShare(item)
                }
            }
            
            override fun onSubscribeButtonClicked(view: View, item: ChannelInfo) {
                requireActivity().checkVerification { 
                    if (item.isSubscribed == 0) {
                        homeViewModel.sendSubscriptionStatus(SubscriptionInfo(null, item.channel_owner_id, mPref.customerId), 1)
                    }
                    else {
                        UnSubscribeDialog.show(requireContext()){
                            homeViewModel.sendSubscriptionStatus(SubscriptionInfo(null, item.channel_owner_id, mPref.customerId), -1)
                        }
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
                    val seasonNumber = seriesInfo.activeSeasonList?.getOrElse(newSeason - 1){0} ?: 0
                    observeList(seasonNumber)
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
                homeViewModel.playContentLiveData.postValue(
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
            var isFirstLoad = false
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                mAdapter.loadStateFlow
//                    .distinctUntilChangedBy { it.refresh }
                    .collectLatest {
                        binding.progressBar.isVisible = it.source.refresh is LoadState.Loading
                        
                        val list = mAdapter.snapshot()
                        homeViewModel.addToPlayListMutableLiveData.postValue(
                            AddToPlaylistData(getPlaylistId(), list.items, false)
                        )
                        if (it.source.refresh !is LoadState.Loading && !isFirstLoad) {
                            isFirstLoad = true
                            seriesInfo.apply {
                                activeSeasonList = list.firstOrNull()?.activeSeasonList
                            }
                            val seasonList = seriesInfo.activeSeasonList?.map { "Season $it" } ?: listOf("Season 1")
                            mViewModel.seasonList.value = seasonList
                            val selectedSeason = minOf(seriesInfo.seasonNo - 1, seasonList.size - 1)
                            mViewModel.selectedSeason.value = selectedSeason
                            detailsAdapter?.updateSpinner(requireContext(), seasonList, selectedSeason)
                        }
                    }
            }
        }
        observeList(seriesInfo.seasonNo)
    }
    
    private fun observeList(currentSeasonNo: Int) {
        seasonListJob?.cancel()
        seasonListJob = viewLifecycleOwner.lifecycleScope.launch {
            mViewModel.getEpisodesBySeason(
                DramaSeasonRequestParam(
                    seriesInfo.type,
                    seriesInfo.seriesId,
                    currentSeasonNo
                )
            ).collectLatest {
                mAdapter.submitData(it.filter { !it.isExpired })
            }
        }
    }
    
    private fun observeSubscribeChannel() {
        observe(homeViewModel.subscriptionLiveData) { response ->
            when(response) {
                is Resource.Success -> {
                    if (response.data == null) {
                        requireContext().showToast(getString(R.string.try_again_message))
                    } else {
                        currentItem?.apply {
                            isSubscribed = response.data?.isSubscribed ?: 0
                            subscriberCount = response.data?.subscriberCount ?: 0
                        }
                        mAdapter.notifyDataSetChanged()
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
        
        if (channelInfo.favorite == null || channelInfo.favorite == "0" || !mPref.isVerifiedUser) {
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
                R.id.menu_add_to_playlist->{
                    requireActivity().handleAddToPlaylist(channelInfo)
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_fav->{
                    requireActivity().handleFavorite(channelInfo, favoriteDao)
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
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}