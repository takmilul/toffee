package com.banglalink.toffee.ui.mychannel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.filter
import androidx.paging.map
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.FirebaseParams
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.apiservice.ApiRoutes
import com.banglalink.toffee.apiservice.BrowsingScreens
import com.banglalink.toffee.common.paging.ListLoadStateAdapter
import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.database.dao.FavoriteItemDao
import com.banglalink.toffee.data.database.entities.SubscriptionInfo
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.databinding.FragmentMyChannelPlaylistVideosBinding
import com.banglalink.toffee.enums.NativeAdAreaType
import com.banglalink.toffee.enums.NativeAdType.SMALL
import com.banglalink.toffee.enums.Reaction
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.listeners.MyChannelPlaylistItemListener
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelNavParams
import com.banglalink.toffee.model.PlaylistPlaybackInfo
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.common.*
import com.banglalink.toffee.ui.home.ChannelHeaderAdapter
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.nativead.NativeAdAdapter
import com.banglalink.toffee.ui.player.AddToPlaylistData
import com.banglalink.toffee.ui.widget.MarginItemDecoration
import com.banglalink.toffee.ui.widget.MyPopupWindow
import com.banglalink.toffee.util.BindingUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyChannelPlaylistVideosFragment : BaseFragment(), MyChannelPlaylistItemListener {
    
    @Inject lateinit var localSync: LocalSync
    private var currentItem: ChannelInfo? = null
    private lateinit var mAdapter: ConcatAdapter
    @Inject lateinit var bindingUtil: BindingUtil
    @Inject lateinit var cacheManager: CacheManager
    @Inject lateinit var favoriteDao: FavoriteItemDao
    private lateinit var playlistInfo: PlaylistPlaybackInfo
    private lateinit var detailsAdapter: ChannelHeaderAdapter
    private var nativeAdBuilder: NativeAdAdapter.Builder? = null
    private lateinit var playlistAdapter: MyChannelPlaylistVideosAdapter
    private var _binding: FragmentMyChannelPlaylistVideosBinding ? = null
    private val binding get() = _binding!!
    private val homeViewModel by activityViewModels<HomeViewModel>()
    private val mViewModel by viewModels<PlaylistVideosViewModel>()
    private val reloadViewModel by activityViewModels<MyChannelReloadViewModel>()
    
    companion object {
        fun newInstance(info: PlaylistPlaybackInfo): MyChannelPlaylistVideosFragment {
            return MyChannelPlaylistVideosFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("playlistInfo", info)
                }
            }
        }
    }
    
    fun getPlaylistId(): Long = playlistInfo.getPlaylistIdLong()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        playlistInfo = MyChannelPlaylistVideosFragmentArgs.fromBundle(requireArguments()).playlistInfo
        currentItem = playlistInfo.currentItem
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyChannelPlaylistVideosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressBar.load(R.drawable.content_loader)
    
        initAdapter()
        observeListState()
        observeVideoList()
        observeListReload()
        setSubscriptionStatus()
        observeSubscribeChannel()
        
        currentItem?.let { 
            binding.backButton.hide()
            binding.playlistName.hide()
            binding.myChannelPlaylistVideos.updatePadding(top = 0.dp)
        }
        binding.emptyViewLabel.text = "No item found"
        binding.playlistName.text = playlistInfo.playlistName
        binding.backButton.safeClick({ findNavController().popBackStack() })
        with(binding.myChannelPlaylistVideos) {
            addItemDecoration(MarginItemDecoration(12))
            adapter = mAdapter
            setHasFixedSize(true)
        }
    }
    
    private fun initAdapter() {
        playlistAdapter = MyChannelPlaylistVideosAdapter(this, currentItem)
        detailsAdapter = ChannelHeaderAdapter(playlistInfo, object : ContentReactionCallback<ChannelInfo> {
            override fun onOpenMenu(view: View, item: ChannelInfo) {
                openMenu(view, item)
            }
    
            override fun onReactionClicked(view: View, reactionCountView: View, item: ChannelInfo) {
                super.onReactionClicked(view, reactionCountView, item)
                val iconLocation = IntArray(2)
                view.getLocationOnScreen(iconLocation)
                val reactionPopupFragment = ReactionPopup.newInstance(item, iconLocation, view.height, true).apply {
                    setCallback(object : ReactionIconCallback {
                        override fun onReactionChange(reactionCount: String, reactionText: String, reactionIcon: Int) {
                            (reactionCountView as TextView).text = reactionCount
                            (view as TextView).text = reactionText
                            view.setCompoundDrawablesWithIntrinsicBounds(reactionIcon, 0, 0, 0)
                            if (reactionText == Reaction.Love.name) {
                                view.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
                            } else {
                                view.setTextColor(ContextCompat.getColor(requireContext(), R.color.fixed_second_text_color))
                            }
                        }
                    })
                }
                childFragmentManager.commit { add(reactionPopupFragment, ReactionPopup.TAG) }
            }
            
            override fun onShareClicked(view: View, item: ChannelInfo, isPlaylist: Boolean) {
                if (isPlaylist) {
                    playlistInfo.playlistShareUrl?.let { requireActivity().handleUrlShare(it) }
                } else {
                    requireActivity().handleShare(item)
                }
            }
    
            override fun onSubscribeButtonClicked(view: View, item: ChannelInfo) {
                if (!mPref.isVerifiedUser){
                    ToffeeAnalytics.toffeeLogEvent(
                        ToffeeEvents.LOGIN,
                        bundleOf(
                            "source" to "channel",
                            "method" to "mobile"
                        )
                    )
                }
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
    
            override fun onProviderIconClicked(item: ChannelInfo) {
                super.onProviderIconClicked(item)
                homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(item.channel_owner_id)
            }
        }, mPref)
        val nativeAdSettings = mPref.nativeAdSettings.value?.find {
            it.area== NativeAdAreaType.PLAYER_PLAYLIST.value
        }
        val playlistAdUnitId = nativeAdSettings?.adUnitId
        val recommendedAdInterval =  nativeAdSettings?.adInterval ?: 0
        val isRecommendedActive = nativeAdSettings?.isActive ?:false
        if (mPref.isNativeAdActive && currentItem != null && isRecommendedActive && recommendedAdInterval > 0 && !playlistAdUnitId.isNullOrBlank()) {
            nativeAdBuilder = NativeAdAdapter.Builder.with(playlistAdUnitId, playlistAdapter as Adapter<ViewHolder>, SMALL)
            val nativeAdAdapter = nativeAdBuilder!!.adItemInterval(recommendedAdInterval).build(bindingUtil)
            mAdapter = ConcatAdapter(detailsAdapter, nativeAdAdapter)
        } else {
            mAdapter = ConcatAdapter(detailsAdapter, playlistAdapter.withLoadStateFooter(ListLoadStateAdapter{playlistAdapter.retry()}))
        }
    }
    
    fun setCurrentChannel(channelInfo: ChannelInfo?) {
        currentItem = channelInfo
        detailsAdapter.setChannelInfo(channelInfo)
        playlistAdapter.setSelectedItem(channelInfo)
        setSubscriptionStatus()
    }
    
    private fun setSubscriptionStatus() {
        lifecycleScope.launch {
            currentItem?.let {
                localSync.syncData(it)
                detailsAdapter.notifyDataSetChanged()
            }
        }
    }
    
    private fun observeVideoList() {
        viewLifecycleOwner.lifecycleScope.launch {
            mViewModel.getMyChannelPlaylistVideos(playlistInfo).collectLatest {
                playlistAdapter.submitData(it.filter { !it.isExpired }.map { channel->
                    localSync.syncData(channel)
                    channel
                })
            }
        }
    }
    
    private fun observeListState() {
        var isInitialized = false
    
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            playlistAdapter.loadStateFlow.collectLatest {
                val isLoading = it.source.refresh is LoadState.Loading || !isInitialized
                val isEmpty = playlistAdapter.itemCount <= 0 && !it.source.refresh.endOfPaginationReached && !isLoading
                binding.progressBar.isVisible = isLoading
                binding.emptyView.isVisible = isEmpty
                binding.myChannelPlaylistVideos.isVisible = !isEmpty
                if (!isEmpty) {
                    val list = playlistAdapter.snapshot()
                    homeViewModel.addToPlayListMutableLiveData.postValue(
                        AddToPlaylistData(getPlaylistId(), list.items, false)
                    )
                }
                isInitialized = true
            }
        }
    }
    
    private fun observeDeletePlaylistVideo() {
        observe(mViewModel.deletePlaylistVideoLiveData) {
            when (it) {
                is Success -> {
                    requireContext().showToast(it.data.message)
                    reloadViewModel.reloadVideos.value = true
                    reloadViewModel.reloadPlaylist.value = true
                }
                is Failure -> {
                    ToffeeAnalytics.logEvent(
                        ToffeeEvents.EXCEPTION,
                        bundleOf(
                            "api_name" to ApiNames.DELETE_MY_CHANNEL_VIDEO,
                            FirebaseParams.BROWSER_SCREEN to BrowsingScreens.MY_CHANNEL_PLAYLIST_PAGE,
                            "error_code" to it.error.code,
                            "error_description" to it.error.msg)
                    )
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }
    
    private fun observeListReload() {
        observe(reloadViewModel.reloadVideos) {
            if (it) {
                reloadPlaylistVideos()
            }
        }
    }

    private fun reloadPlaylistVideos() {
        cacheManager.clearCacheByUrl(ApiRoutes.GET_MY_CHANNEL_PLAYLIST_VIDEOS)
        playlistAdapter.refresh().let {
            lifecycleScope.launch {
                playlistAdapter.loadStateFlow.collectLatest {
                    if (playlistInfo.playlistItemCount != playlistAdapter.itemCount) {
                        playlistInfo.playlistItemCount = playlistAdapter.itemCount
                        detailsAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    private fun observeSubscribeChannel() {
        observe(homeViewModel.subscriptionLiveData) { response ->
            when(response) {
                is Success -> {
                    currentItem?.apply {
                        isSubscribed = response.data.isSubscribed
                        subscriberCount = response.data.subscriberCount
                    }
                    detailsAdapter.notifyDataSetChanged()
                }
                is Failure -> {
                    requireContext().showToast(response.error.msg)
                }
            }
        }
    }
    
    override fun onProviderIconClicked(item: ChannelInfo) {
        super.onProviderIconClicked(item)
        homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(item.channel_owner_id)
    }

    override fun onItemClickAtPosition(position: Int, item: ChannelInfo) {
        if (item == currentItem || item.id == currentItem?.id) {
            return
        }
        homeViewModel.addToPlayListMutableLiveData.postValue(
            AddToPlaylistData(getPlaylistId(), playlistAdapter.snapshot().items)
        )
        homeViewModel.playContentLiveData.postValue(
            playlistInfo.copy(playIndex = position, currentItem = item)
        )
    }
    
    override fun onOpenMenu(view: View, item: ChannelInfo) {
        super.onOpenMenu(view, item)
        if (playlistInfo.channelOwnerId == mPref.customerId && mPref.isVerifiedUser) {
            PopupMenu(requireContext(), view).apply {
                inflate(R.menu.menu_delete_playlist_video)
                menu.findItem(R.id.menu_fav).isVisible = false
                menu.findItem(R.id.menu_report).isVisible = false
                menu.findItem(R.id.menu_share).isVisible = item.isApproved == 1
                menu.findItem(R.id.menu_delete_playlist_video).isVisible = currentItem?.id != item.id
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.menu_share -> {
                            requireActivity().handleShare(item)
                        }
                        R.id.menu_delete_playlist_video -> {
                            observeDeletePlaylistVideo()
                            mViewModel.deletePlaylistVideo(playlistInfo.channelOwnerId, item.id.toInt(), playlistInfo.playlistId)
                            mViewModel.insertActivity(item, Reaction.Delete.value)
                        }
                    }
                    return@setOnMenuItemClickListener true
                }
            }.show()
        } else {
            openMenu(view, item)
        }
    }

    private fun openMenu(anchor: View, channelInfo: ChannelInfo) {
        MyPopupWindow(requireContext(), anchor).apply {
            inflate(R.menu.menu_catchup_item)
            if (channelInfo.favorite == null || channelInfo.favorite == "0") {
                menu.findItem(R.id.menu_fav).title = "Add to Favorites"
            } else {
                menu.findItem(R.id.menu_fav).title = "Remove from Favorites"
            }
            menu.findItem(R.id.menu_share).isVisible = channelInfo.isApproved == 1
            menu.findItem(R.id.menu_report).isVisible = mPref.customerId != channelInfo.channel_owner_id
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_share -> {
                        requireActivity().handleShare(channelInfo)
                    }
                    R.id.menu_fav -> {
                        requireActivity().handleFavorite(channelInfo, favoriteDao, onAdded = {playlistAdapter.refresh()}, onRemoved = {playlistAdapter.refresh()})
                    }
                    R.id.menu_add_to_playlist->{
                        requireActivity().handleAddToPlaylist(channelInfo, if (mPref.customerId == channelInfo.channel_owner_id) 0 else 1)
                    }
                    R.id.menu_report -> {
                        requireActivity().handleReport(channelInfo)
                    }
                }
                return@setOnMenuItemClickListener true
            }
        }.show()
    }
    
    override fun onDestroyView() {
        nativeAdBuilder?.destroyAd()
        binding.myChannelPlaylistVideos.adapter = null
        nativeAdBuilder=null
        super.onDestroyView()
        _binding = null
    }
}