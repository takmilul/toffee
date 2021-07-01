package com.banglalink.toffee.ui.mychannel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.map
import androidx.recyclerview.widget.ConcatAdapter
import com.banglalink.toffee.R
import com.banglalink.toffee.apiservice.GET_MY_CHANNEL_PLAYLISTS
import com.banglalink.toffee.apiservice.GET_MY_CHANNEL_PLAYLIST_VIDEOS
import com.banglalink.toffee.apiservice.MyChannelPlaylistContentParam
import com.banglalink.toffee.common.paging.ListLoadStateAdapter
import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.database.dao.FavoriteItemDao
import com.banglalink.toffee.data.database.entities.SubscriptionInfo
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.data.repository.SubscriptionCountRepository
import com.banglalink.toffee.data.repository.SubscriptionInfoRepository
import com.banglalink.toffee.databinding.FragmentMyChannelPlaylistVideosBinding
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
import com.banglalink.toffee.ui.player.AddToPlaylistData
import com.banglalink.toffee.ui.widget.MarginItemDecoration
import com.banglalink.toffee.ui.widget.MyPopupWindow
import com.suke.widget.SwitchButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyChannelPlaylistVideosFragment : BaseFragment(), MyChannelPlaylistItemListener {
    
    @Inject lateinit var localSync: LocalSync
    private var currentItem: ChannelInfo? = null
    private lateinit var mAdapter: ConcatAdapter
    @Inject lateinit var cacheManager: CacheManager
    @Inject lateinit var favoriteDao: FavoriteItemDao
    private lateinit var detailsAdapter: ChannelHeaderAdapter
    private lateinit var args: MyChannelPlaylistVideosFragmentArgs
    private lateinit var requestParams: MyChannelPlaylistContentParam
    private lateinit var playlistAdapter: MyChannelPlaylistVideosAdapter
    private var _binding: FragmentMyChannelPlaylistVideosBinding ? = null
    private val binding get() = _binding!!
    @Inject lateinit var subscriptionInfoRepository: SubscriptionInfoRepository
    @Inject lateinit var subscriptionCountRepository: SubscriptionCountRepository
    private val homeViewModel by activityViewModels<HomeViewModel>()
    val mViewModel by viewModels<MyChannelPlaylistVideosViewModel>()
    
    companion object {
        fun newInstance(info: PlaylistPlaybackInfo): MyChannelPlaylistVideosFragment {
            return MyChannelPlaylistVideosFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("playlistInfo", info)
                }
            }
        }
    }
    
    fun getPlaylistId(): Long = args.playlistInfo.getPlaylistIdLong()
    
    fun isAutoPlayEnabled(): Boolean {
        return view?.findViewById<SwitchButton>(R.id.autoPlaySwitch)?.isChecked == true
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        args = MyChannelPlaylistVideosFragmentArgs.fromBundle(requireArguments())
        requestParams = MyChannelPlaylistContentParam(args.playlistInfo.channelOwnerId, args.playlistInfo.playlistId)
        currentItem = args.playlistInfo.currentItem
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyChannelPlaylistVideosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        binding.myChannelPlaylistVideos.adapter = null
        super.onDestroyView()
        _binding = null
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initAdapter()
        observeListState()
        observeVideoList()
        setSubscriptionStatus()
//        observeSubscribeChannel()
        
        currentItem?.let { 
            binding.backButton.hide()
            binding.playlistName.hide()
            binding.myChannelPlaylistVideos.updatePadding(top = 0.dp)
        }
        binding.emptyViewLabel.text = "No item found"
        binding.playlistName.text = args.playlistInfo.playlistName
        binding.backButton.safeClick({ findNavController().popBackStack() })
        with(binding.myChannelPlaylistVideos) {
            addItemDecoration(MarginItemDecoration(12))
            adapter = mAdapter
            setHasFixedSize(true)
        }
    }
    
    private fun initAdapter() {
        playlistAdapter = MyChannelPlaylistVideosAdapter(this, currentItem)
        detailsAdapter = ChannelHeaderAdapter(args.playlistInfo, object : ContentReactionCallback<ChannelInfo> {
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
    
            override fun onShareClicked(view: View, item: ChannelInfo) {
                homeViewModel.shareContentLiveData.postValue(item)
            }
    
            override fun onSubscribeButtonClicked(view: View, item: ChannelInfo) {
                requireActivity().checkVerification {
                    if (item.isSubscribed == 0) {
                        homeViewModel.sendSubscriptionStatus(
                            SubscriptionInfo(
                                null,
                                item.channel_owner_id,
                                mPref.customerId
                            ), 1
                        )
                        currentItem?.let { 
                            it.isSubscribed = 1
                            it.subscriberCount++
                        }
                        detailsAdapter.notifyDataSetChanged()
                    } else {
                        UnSubscribeDialog.show(requireContext()) {
                            homeViewModel.sendSubscriptionStatus(
                                SubscriptionInfo(
                                    null,
                                    item.channel_owner_id,
                                    mPref.customerId
                                ), -1
                            )
                            currentItem?.let {
                                it.isSubscribed = 0
                                it.subscriberCount--
                            }
                            detailsAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
    
            override fun onProviderIconClicked(item: ChannelInfo) {
                super.onProviderIconClicked(item)
                homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(item.channel_owner_id)
            }
        }, mPref)
        mAdapter = ConcatAdapter(detailsAdapter, playlistAdapter.withLoadStateFooter(ListLoadStateAdapter { playlistAdapter.retry() }))
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
            mViewModel.getMyChannelPlaylistVideos(requestParams).collectLatest {
                playlistAdapter.submitData(it.map { channel->
                    localSync.syncData(channel, LocalSync.SYNC_FLAG_FAVORITE or LocalSync.SYNC_FLAG_VIEW_COUNT)
                    channel
                })
            }
        }
    }
    
    private fun observeSubscribeChannel() {
        observe(homeViewModel.subscriptionLiveData) { response ->
            when(response) {
                is Success -> {
                    currentItem?.let {
                        val status = response.data.isSubscribed.takeIf { it == 1 } ?: -1
                        if (response.data.isSubscribed == 1){
                            it.isSubscribed = 1
                            ++ it.subscriberCount
                        }
                        else {
                            it.isSubscribed = 0
                            -- it.subscriberCount
                        }
                        homeViewModel.updateSubscriptionCountTable(SubscriptionInfo(null, it.channel_owner_id, mPref.customerId), status)
                    }
                    detailsAdapter.notifyDataSetChanged()
                }
                is Failure -> {
                    requireContext().showToast(response.error.msg)
                }
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
        homeViewModel.fragmentDetailsMutableLiveData.postValue(args.playlistInfo.copy(
            playIndex = position,
            currentItem = item
        ))
    }
    
    override fun onOpenMenu(view: View, item: ChannelInfo) {
        super.onOpenMenu(view, item)
        if (requestParams.channelOwnerId == mPref.customerId) {
            PopupMenu(requireContext(), view).apply {
                inflate(R.menu.menu_delete_playlist_video)
                menu.findItem(R.id.menu_share).isVisible = item.isApproved == 1
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.menu_share -> {
                            requireActivity().handleShare(item)
                        }
                        R.id.menu_delete_playlist_video -> {
                            observeDeletePlaylistVideo()
                            mViewModel.deletePlaylistVideo(requestParams.channelOwnerId, item.id.toInt(), requestParams.playlistId)
                            mViewModel.insertActivity(item, Reaction.Delete.value)
                        }
                    }
                    return@setOnMenuItemClickListener true
                }
                show()
            }
        } else {
            openMenu(view, item)
        }
    }
    
    private fun observeDeletePlaylistVideo() {
        observe(mViewModel.deletePlaylistVideoLiveData) {
            when (it) {
                is Success -> {
                    cacheManager.clearCacheByUrl(GET_MY_CHANNEL_PLAYLISTS)
                    cacheManager.clearCacheByUrl(GET_MY_CHANNEL_PLAYLIST_VIDEOS)
                    playlistAdapter.refresh()
                    requireContext().showToast(it.data.message)
                }
                is Failure -> {
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }
    
    private fun openMenu(anchor: View, channelInfo: ChannelInfo) {
        val popupMenu = MyPopupWindow(requireContext(), anchor)
        popupMenu.inflate(R.menu.menu_catchup_item)
        
        if (channelInfo.favorite == null || channelInfo.favorite == "0") {
            popupMenu.menu.getItem(0).title = "Add to Favorites"
        } else {
            popupMenu.menu.getItem(0).title = "Remove from Favorites"
        }
        
        popupMenu.menu.findItem(R.id.menu_share).isVisible = channelInfo.isApproved == 1
        popupMenu.menu.findItem(R.id.menu_report).isVisible = mPref.customerId != channelInfo.channel_owner_id
        popupMenu.setOnMenuItemClickListener {
            when (it?.itemId) {
                R.id.menu_share -> {
                    requireActivity().handleShare(channelInfo)
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_fav -> {
                    requireActivity().handleFavorite(channelInfo, favoriteDao)
                    return@setOnMenuItemClickListener true
                }
                else -> {
                    return@setOnMenuItemClickListener false
                }
            }
        }
        popupMenu.show()
    }

    fun setCurrentChannel(channelInfo: ChannelInfo?) {
        currentItem = channelInfo
        detailsAdapter.setChannelInfo(channelInfo)
        playlistAdapter.setSelectedItem(channelInfo)
        setSubscriptionStatus()
    }
}