package com.banglalink.toffee.ui.playlist

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
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.banglalink.toffee.R
import com.banglalink.toffee.apiservice.GET_MY_CHANNEL_PLAYLIST_VIDEOS
import com.banglalink.toffee.apiservice.MyChannelPlaylistContentParam
import com.banglalink.toffee.common.paging.ListLoadStateAdapter
import com.banglalink.toffee.data.database.entities.SubscriptionInfo
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.data.repository.SubscriptionCountRepository
import com.banglalink.toffee.data.repository.SubscriptionInfoRepository
import com.banglalink.toffee.databinding.FragmentPlayListVideosBinding
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
import com.banglalink.toffee.ui.player.AddToPlaylistData
import com.banglalink.toffee.ui.widget.MarginItemDecoration
import com.banglalink.toffee.ui.widget.MyPopupWindow
import com.suke.widget.SwitchButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PlayListVideosFragment : BaseFragment(), MyChannelPlaylistItemListener {

    private var isSubscribed: Int = 0
    private var subscriberCount: Long = 0
    private var currentItem: ChannelInfo? = null
    private lateinit var mAdapter: ConcatAdapter
    @Inject lateinit var cacheManager: CacheManager
    private lateinit var detailsAdapter: ChannelHeaderAdapter
    private lateinit var args: PlayListVideosFragmentArgs
    private lateinit var requestParams: MyChannelPlaylistContentParam
    private lateinit var playlistAdapter: MyChannelPlaylistVideosAdapter
    private var _binding:FragmentPlayListVideosBinding?=null
    private val binding get() = _binding!!
    @Inject lateinit var subscriptionInfoRepository: SubscriptionInfoRepository
    @Inject lateinit var subscriptionCountRepository: SubscriptionCountRepository
    private val homeViewModel by activityViewModels<HomeViewModel>()
    val mViewModel by viewModels<MyChannelPlaylistVideosViewModel>()

    companion object {
        fun newInstance(info: PlaylistPlaybackInfo): PlayListVideosFragment {
            return PlayListVideosFragment().apply {
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
        args = PlayListVideosFragmentArgs.fromBundle(requireArguments())
        requestParams = MyChannelPlaylistContentParam(args.playlistInfo.channelOwnerId, args.playlistInfo.playlistId)
        currentItem = args.playlistInfo.currentItem
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPlayListVideosBinding.inflate(inflater, container, false)
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
        binding
        binding.playlistName.text = args.playlistInfo.playlistName
        binding.backButton.safeClick({ findNavController().popBackStack() })
        with(binding.myChannelPlaylistVideos) {
            addItemDecoration(MarginItemDecoration(12))
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
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
                if (isSubscribed == 0) {
                    homeViewModel.sendSubscriptionStatus(SubscriptionInfo(null, item.channel_owner_id, mPref.customerId), 1)
                    isSubscribed = 1
                    currentItem?.isSubscribed = isSubscribed
                    currentItem?.subscriberCount = (++subscriberCount).toInt()
                    detailsAdapter.notifyDataSetChanged()
                } else {
                    UnSubscribeDialog.show(requireContext()) {
                        homeViewModel.sendSubscriptionStatus(SubscriptionInfo(null, item.channel_owner_id, mPref.customerId), -1)
                        isSubscribed = 0
                        currentItem?.isSubscribed = isSubscribed
                        currentItem?.subscriberCount = (--subscriberCount).toInt()
                        detailsAdapter.notifyDataSetChanged()
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
                isSubscribed = if (subscriptionInfoRepository.getSubscriptionInfoByChannelId(it.channel_owner_id, mPref.customerId) != null) 1 else 0
                subscriberCount = subscriptionCountRepository.getSubscriberCount(it.channel_owner_id)
                it.isSubscribed = if (subscriptionInfoRepository.getSubscriptionInfoByChannelId(it.channel_owner_id, mPref.customerId) != null) 1 else 0
                it.subscriberCount = subscriptionCountRepository.getSubscriberCount(it.channel_owner_id).toInt()
                detailsAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun observeVideoList() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            mViewModel.getMyChannelPlaylistVideos(requestParams).collectLatest {
                playlistAdapter.submitData(it)
            }
        }
    }

    private fun observeSubscribeChannel() {
        observe(homeViewModel.subscriptionLiveData) { response ->
            when(response) {
                is Resource.Success -> {
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
                is Resource.Failure -> {
                    requireContext().showToast(response.error.msg)
                }
            }
        }
    }

    private fun observeListState() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            playlistAdapter
                    .loadStateFlow
//                .distinctUntilChangedBy {
//                    it.refresh
//                }
                    .collect {
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
        if ( mPref.customerId>0) {
            PopupMenu(requireContext(), view).apply {
                inflate(R.menu.menu_delete_playlist_video)
                menu.findItem(R.id.menu_share).isVisible = item.isApproved == 1
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.menu_share -> {
                            homeViewModel.shareContentLiveData.postValue(item)
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
        } else {
            openMenu(view, item)
        }
    }

    private fun observeDeletePlaylistVideo() {
        observe(mViewModel.deletePlaylistVideoLiveData) {
            when (it) {
                is Resource.Success -> {
                    cacheManager.clearCacheByUrl(GET_MY_CHANNEL_PLAYLIST_VIDEOS)
                    playlistAdapter.refresh()
                    requireContext().showToast(it.data.message)
                }
                is Resource.Failure -> {
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
                    homeViewModel.shareContentLiveData.postValue(channelInfo)
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_fav -> {
                    homeViewModel.updateFavorite(channelInfo).observe(viewLifecycleOwner, { resp ->
                        handleFavoriteResponse(resp)
                    })
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_not_interested -> {
                    return@setOnMenuItemClickListener true
                }
                else -> {
                    return@setOnMenuItemClickListener false
                }
            }
        }
        popupMenu.show()
    }

    private fun handleFavoriteResponse(it: Resource<ChannelInfo>) {
        when (it) {
            is Resource.Success -> {
                val channelInfo = it.data
                when (channelInfo.favorite) {
                    "0" -> {
                        context?.showToast("Content successfully removed from favorite list")
                    }
                    "1" -> {
                        context?.showToast("Content successfully added to favorite list")
                    }
                }
            }
            is Resource.Failure -> {
                context?.showToast(it.error.msg)
            }
        }
    }

    fun setCurrentChannel(channelInfo: ChannelInfo?) {
        currentItem = channelInfo
        detailsAdapter.setChannelInfo(channelInfo)
        playlistAdapter.setSelectedItem(channelInfo)
        setSubscriptionStatus()
    }
}