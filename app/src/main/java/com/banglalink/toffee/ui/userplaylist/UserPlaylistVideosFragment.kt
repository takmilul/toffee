package com.banglalink.toffee.ui.userplaylist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import com.banglalink.toffee.apiservice.ApiRoutes
import com.banglalink.toffee.apiservice.MyChannelPlaylistContentParam
import com.banglalink.toffee.common.paging.ListLoadStateAdapter
import com.banglalink.toffee.data.database.LocalSync
import com.banglalink.toffee.data.database.dao.FavoriteItemDao
import com.banglalink.toffee.data.database.entities.SubscriptionInfo
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.databinding.FragmentUserPlaylistVideosBinding
import com.banglalink.toffee.enums.Reaction
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.listeners.MyChannelPlaylistItemListener
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.MyChannelNavParams
import com.banglalink.toffee.model.PlaylistPlaybackInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.*
import com.banglalink.toffee.ui.home.ChannelHeaderAdapter
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.mychannel.MyChannelPlaylistVideosAdapter
import com.banglalink.toffee.ui.mychannel.MyChannelPlaylistVideosViewModel
import com.banglalink.toffee.ui.mychannel.MyChannelReloadViewModel
import com.banglalink.toffee.ui.player.AddToPlaylistData
import com.banglalink.toffee.ui.widget.MarginItemDecoration
import com.banglalink.toffee.ui.widget.MyPopupWindow
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class UserPlaylistVideosFragment : BaseFragment(), MyChannelPlaylistItemListener {
    @Inject lateinit var localSync: LocalSync
    private var currentItem: ChannelInfo? = null
    private lateinit var mAdapter: ConcatAdapter
    @Inject lateinit var cacheManager: CacheManager
    @Inject lateinit var favoriteDao: FavoriteItemDao
    private lateinit var args: UserPlaylistVideosFragmentArgs
    private lateinit var detailsAdapter: ChannelHeaderAdapter
    private var _binding: FragmentUserPlaylistVideosBinding? = null
    private lateinit var requestParams: MyChannelPlaylistContentParam
    private val binding get() = _binding!!
    private lateinit var playlistAdapter: MyChannelPlaylistVideosAdapter
    private val homeViewModel by activityViewModels<HomeViewModel>()
    private val mViewModel by viewModels<MyChannelPlaylistVideosViewModel>()
    private val reloadViewModel by activityViewModels<MyChannelReloadViewModel>()

    companion object {
        fun newInstance(info: PlaylistPlaybackInfo): UserPlaylistVideosFragment {
            return UserPlaylistVideosFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("playlistInfo", info)
                }
            }
        }
    }

    fun getPlaylistId(): Long = args.playlistInfo.getPlaylistIdLong()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        args = UserPlaylistVideosFragmentArgs.fromBundle(requireArguments())
        currentItem = args.playlistInfo.currentItem
        requestParams = MyChannelPlaylistContentParam(args.playlistInfo.channelOwnerId, args.playlistInfo.playlistId)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUserPlaylistVideosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAdapter()
        observeListState()
        observeVideoList()
        observeListReload()
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
                            SubscriptionInfo(null, item.channel_owner_id, mPref.customerId), 1
                        )
                        currentItem?.let {
                            it.isSubscribed = 1
                            it.subscriberCount++
                        }
                        detailsAdapter.notifyDataSetChanged()
                    } else {
                        UnSubscribeDialog.show(requireContext()) {
                            homeViewModel.sendSubscriptionStatus(
                                SubscriptionInfo(null, item.channel_owner_id, mPref.customerId), -1
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
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            mViewModel.getMyChannelUserPlaylistVideos(requestParams).collectLatest {
                playlistAdapter.submitData(it.map { channel ->
                    localSync.syncData(channel, LocalSync.SYNC_FLAG_FAVORITE or LocalSync.SYNC_FLAG_VIEW_COUNT)
                    channel
                })
            }
        }
    }

    private fun observeSubscribeChannel() {
        observe(homeViewModel.subscriptionLiveData) { response ->
            when (response) {
                is Resource.Success -> {
                    currentItem?.let {
                        val status = response.data.isSubscribed.takeIf { it == 1 } ?: -1
                        if (response.data.isSubscribed == 1) {
                            it.isSubscribed = 1
                            ++it.subscriberCount
                        } else {
                            it.isSubscribed = 0
                            --it.subscriberCount
                        }
                        homeViewModel.updateSubscriptionCountTable(SubscriptionInfo(null, it.channel_owner_id, mPref.customerId), status)
                    }
                    detailsAdapter.notifyDataSetChanged()
                }
                is Resource.Failure -> {
                    requireContext().showToast(response.error.msg)
                }
            }
        }
    }

    private fun observeListState() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            playlistAdapter.loadStateFlow.collect {
                binding.progressBar.isVisible = it.source.refresh is LoadState.Loading
                playlistAdapter.apply {
                    val showEmpty = itemCount <= 0 && !it.source.refresh.endOfPaginationReached && it.source.refresh !is LoadState.Loading
                    binding.emptyView.isVisible = showEmpty
                    binding.myChannelPlaylistVideos.isVisible = !showEmpty
                }
                val list = playlistAdapter.snapshot()
                homeViewModel.addToPlayListMutableLiveData.postValue(
                    AddToPlaylistData(getPlaylistId(), list.items, false)
                )
            }
        }
    }

    private fun observeDeletePlaylistVideo() {
        observe(mViewModel.deletePlaylistVideoLiveData) {
            when (it) {
                is Resource.Success -> {
                    requireContext().showToast(it.data.message)
                    reloadViewModel.reloadVideos.value = true
                    reloadViewModel.reloadPlaylist.value = true
                }
                is Resource.Failure -> {
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
        cacheManager.clearCacheByUrl(ApiRoutes.GET_USER_PLAYLIST_VIDEOS)
        playlistAdapter.refresh().let { 
            lifecycleScope.launch { 
                playlistAdapter.loadStateFlow.collectLatest {
                    if (args.playlistInfo.playlistItemCount != playlistAdapter.itemCount) {
                        args.playlistInfo.playlistItemCount = playlistAdapter.itemCount
                        detailsAdapter.notifyDataSetChanged()
                    }
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
        homeViewModel.fragmentDetailsMutableLiveData.postValue(
            args.playlistInfo.copy(
                playIndex = position,
                currentItem = item
            )
        )
    }

    override fun onOpenMenu(view: View, item: ChannelInfo) {
        super.onOpenMenu(view, item)
        MyPopupWindow(requireContext(), view).apply {
            inflate(R.menu.menu_delete_playlist_video)
            if (item.favorite == null || item.favorite == "0" || !mPref.isVerifiedUser) {
                menu.getItem(0).title = "Add to Favorites"
            } else {
                menu.getItem(0).title = "Remove from Favorites"
            }
            menu.findItem(R.id.menu_share).isVisible = item.isApproved == 1
            menu.findItem(R.id.menu_delete_playlist_video).isVisible = currentItem?.id != item.id
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_fav -> {
                        requireActivity().handleFavorite(item, favoriteDao,
                            onAdded = {
                                playlistAdapter.refresh()
                                currentItem?.favorite = "1"
                                detailsAdapter.notifyDataSetChanged()
                            },
                            onRemoved = {
                                playlistAdapter.refresh()
                                currentItem?.favorite = "0"
                                detailsAdapter.notifyDataSetChanged()
                            })
                    }
                    R.id.menu_share -> {
                        requireActivity().handleShare(item)
                    }
                    R.id.menu_report -> {
                        requireActivity().handleReport(item)
                    }
                    R.id.menu_delete_playlist_video -> {
                        observeDeletePlaylistVideo()
                        mViewModel.deletePlaylistVideo(mPref.customerId, item.id.toInt(), requestParams.playlistId)
                        mViewModel.insertActivity(item, Reaction.Delete.value)
                    }
                }
                return@setOnMenuItemClickListener true
            }
            show()
        }
    }

    private fun openMenu(anchor: View, channelInfo: ChannelInfo) {
        MyPopupWindow(requireContext(), anchor).apply {
            inflate(R.menu.menu_catchup_item)
            if (channelInfo.favorite == null || channelInfo.favorite == "0") {
                menu.getItem(0).title = "Add to Favorites"
            } else {
                menu.getItem(0).title = "Remove from Favorites"
            }
            menu.findItem(R.id.menu_share).isVisible = channelInfo.isApproved == 1
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_share -> {
                        requireActivity().handleShare(channelInfo)
                    }
                    R.id.menu_fav -> {
                        requireActivity().handleFavorite(
                            channelInfo,
                            favoriteDao,
                            onAdded = { playlistAdapter.refresh() },
                            onRemoved = { playlistAdapter.refresh() })
                    }
                    R.id.menu_add_to_playlist -> {
                        requireActivity().handleAddToPlaylist(channelInfo)
                    }
                    R.id.menu_report -> {
                        requireActivity().handleReport(channelInfo)
                    }
                }
                return@setOnMenuItemClickListener true
            }
            show()
        }
    }

    override fun onDestroyView() {
        binding.myChannelPlaylistVideos.adapter = null
        super.onDestroyView()
        _binding = null
    }
}