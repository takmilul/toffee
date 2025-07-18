package com.banglalink.toffee.ui.mychannel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.filter
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.FirebaseParams
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.apiservice.ApiRoutes
import com.banglalink.toffee.apiservice.BrowsingScreens
import com.banglalink.toffee.common.paging.ListLoadStateAdapter
import com.banglalink.toffee.data.database.dao.FavoriteItemDao
import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.databinding.FragmentMyChannelVideosBinding
import com.banglalink.toffee.enums.Reaction.Love
import com.banglalink.toffee.extension.checkVerification
import com.banglalink.toffee.extension.handleAddToPlaylist
import com.banglalink.toffee.extension.handleFavorite
import com.banglalink.toffee.extension.handleReport
import com.banglalink.toffee.extension.handleShare
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.px
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.common.ContentReactionCallback
import com.banglalink.toffee.ui.common.ReactionIconCallback
import com.banglalink.toffee.ui.common.ReactionPopup
import com.banglalink.toffee.ui.home.HomeActivity
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.widget.MarginItemDecoration
import com.banglalink.toffee.ui.widget.ToffeeAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyChannelVideosFragment : BaseFragment(), ContentReactionCallback<ChannelInfo> {
    
    private var channelOwnerId: Int = 0
    private var isOwner: Boolean = false
    private var isMyChannel: Boolean = false
    @Inject lateinit var reactionDao: ReactionDao
    @Inject lateinit var cacheManager: CacheManager
    @Inject lateinit var favoriteDao: FavoriteItemDao
    private lateinit var mAdapter: MyChannelVideosAdapter
    private var _binding: FragmentMyChannelVideosBinding ? = null
    private val binding get() = _binding!!
    private val homeViewModel by activityViewModels<HomeViewModel>()
    val mViewModel by viewModels<MyChannelVideosViewModel>()
    private val videosReloadViewModel by activityViewModels<MyChannelReloadViewModel>()
    
    companion object {
        const val IS_MY_CHANNEL = "isMyChannel"
        private const val CHANNEL_OWNER_ID = "channelOwnerId"
        
        fun newInstance(channelOwnerId: Int, isMyChannel: Boolean): MyChannelVideosFragment {
            return MyChannelVideosFragment().apply {
                arguments = bundleOf(CHANNEL_OWNER_ID to channelOwnerId, IS_MY_CHANNEL to isMyChannel)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAdapter = MyChannelVideosAdapter(this)
        channelOwnerId = arguments?.getInt(CHANNEL_OWNER_ID) ?: 0
        isOwner = channelOwnerId == mPref.customerId
        isMyChannel = arguments?.getBoolean(IS_MY_CHANNEL) ?: false
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyChannelVideosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressBar.load(R.drawable.content_loader)
        setEmptyView()
        with(binding.myChannelVideos) {
            addItemDecoration(MarginItemDecoration(12))
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                mAdapter.loadStateFlow.collectLatest {
                    binding.progressBar.isVisible = it.source.refresh is LoadState.Loading
                    val showEmpty = mAdapter.itemCount <= 0 && !it.source.refresh.endOfPaginationReached && it.source.refresh !is LoadState.Loading
                    binding.emptyView.isVisible = showEmpty
                    binding.myChannelVideos.isVisible = !showEmpty
                }
            }
            adapter = mAdapter.withLoadStateFooter(ListLoadStateAdapter { mAdapter.retry() })
            setHasFixedSize(true)
        }
        if (isOwner && !mPref.isVerifiedUser && isMyChannel) {
            return
        }
        observeReloadVideos()
        observeDeleteVideo()
        observeMyChannelVideos()
    }
    
    private fun observeMyChannelVideos() {
        viewLifecycleOwner.lifecycleScope.launch {
            mViewModel.getMyChannelVideos(channelOwnerId).collectLatest {
                mAdapter.submitData(it.filter { !it.isExpired })
            }
        }
    }
    @OptIn(UnstableApi::class)
    private fun setEmptyView() {
        with(binding) {
            if (isOwner) {
                emptyViewLabel.text = getString(R.string.owner_video_empty_msg)
                uploadVideoButton.setOnClickListener {
                    if (!mPref.isVerifiedUser){
                        ToffeeAnalytics.toffeeLogEvent(
                            ToffeeEvents.LOGIN_SOURCE,
                            bundleOf(
                                "source" to "upload",
                                "method" to "mobile"
                            )
                        )
                    }
                    requireActivity().checkVerification {
                        requireActivity().let {
                            if(it is HomeActivity) it.checkChannelDetailAndUpload()
                        }
                    }
                }
                creatorsPolicyButton.setOnClickListener {
                    findNavController().navigate(R.id.privacyPolicyFragment, Bundle().apply { 
                        putString("myTitle", "Creators Policy")
                        putString("url", mPref.creatorsPolicyUrl)
                    })
                }
            } else {
                uploadVideoButton.hide()
                creatorsPolicyButton.hide()
                emptyViewLabel.text = getString(R.string.public_video_empty_msg)
                (emptyViewIcon.layoutParams as ViewGroup.MarginLayoutParams).topMargin = 32.px
            }
            emptyView.isVisible = true
        }
    }
    
    override fun onOpenMenu(view: View, item: ChannelInfo) {
        super.onOpenMenu(view, item)
        PopupMenu(requireContext(), view).apply {
            if (isOwner && mPref.isVerifiedUser) {
                inflate(R.menu.menu_channel_owner_videos)
            } else {
                inflate(R.menu.menu_channel_videos)
                menu.findItem(R.id.menu_report).isVisible = !(mPref.customerId == item.channel_owner_id && mPref.isVerifiedUser)
                menu.findItem(R.id.menu_fav).isVisible = item.isApproved == 1
                if (item.favorite == null || item.favorite == "0" || !mPref.isVerifiedUser) {
                    menu.findItem(R.id.menu_fav).title = "Add to Favorites"
                } else {
                    menu.findItem(R.id.menu_fav).title = "Remove from Favorites"
                }
            }
            menu.findItem(R.id.menu_share).isVisible = item.isApproved == 1
            
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_edit_content -> {
                        parentFragment?.findNavController()?.navigate(R.id.myChannelVideosEditFragment, bundleOf(
                            MyChannelVideosEditFragment.CHANNEL_INFO to item
                        ))
                    }
                    R.id.menu_add_to_playlist -> {
                        requireActivity().handleAddToPlaylist(item, if(isOwner && mPref.isVerifiedUser) 0 else 1)
                    }
                    R.id.menu_share -> {
                        requireActivity().handleShare(item)
                    }
                    R.id.menu_fav -> {
                        requireActivity().handleFavorite(item, favoriteDao)
                    }
                    R.id.menu_report -> {
                        requireActivity().handleReport(item)
                    }
                    R.id.menu_delete_content -> {
                        showDeleteVideoDialog(item.id.toInt())
                    }
                }
                return@setOnMenuItemClickListener true
            }
        }.show()
    }
    
    private fun showDeleteVideoDialog(contentId: Int) {
        ToffeeAlertDialogBuilder(
            requireContext(),
            text = "Are you sure to delete?",
            positiveButtonTitle = "No",
            negativeButtonTitle = "Delete",
            positiveButtonListener = { it?.dismiss() },
            negativeButtonListener = {
                mViewModel.deleteVideo(contentId)
                it?.dismiss()
            }
        ).create().show()
    }
    
    override fun onItemClicked(item: ChannelInfo) {
        super.onItemClicked(item)
        homeViewModel.playContentLiveData.postValue(item)
    }
    
    override fun onReactionClicked(view: View, reactionCountView: View, item: ChannelInfo) {
        super.onReactionClicked(view, reactionCountView, item)
        val iconLocation = IntArray(2)
        view.getLocationOnScreen(iconLocation)
        val reactionPopupFragment = ReactionPopup.newInstance(item, iconLocation, view.height).apply {
            setCallback(object : ReactionIconCallback {
                override fun onReactionChange(reactionCount: String, reactionText: String, reactionIcon: Int) {
                    (reactionCountView as TextView).text = reactionCount
                    (view as TextView).text = reactionText
                    view.setCompoundDrawablesWithIntrinsicBounds(reactionIcon, 0, 0, 0)
                    if (reactionText == Love.name) {
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
        super.onShareClicked(view, item, isPlaylist)
        requireActivity().handleShare(item)
    }
    
    private fun observeReloadVideos() {
        observe(videosReloadViewModel.reloadVideos) {
            if (it) {
                reloadVideosList()
            }
        }
    }
    
    private fun observeDeleteVideo() {
        observe(mViewModel.deleteVideoLiveData) {
            when (it) {
                is Success -> {
                    requireContext().showToast(it.data?.message ?: getString(R.string.try_again_message))
                    it.data?.let {
                        reloadVideosList()
                        videosReloadViewModel.reloadPlaylist.value = true
                    }
                }
                is Failure -> {
                    ToffeeAnalytics.logEvent(
                        ToffeeEvents.EXCEPTION,
                        bundleOf(
                            "api_name" to ApiNames.DELETE_MY_CHANNEL_VIDEO,
                            FirebaseParams.BROWSER_SCREEN to BrowsingScreens.MY_CHANNEL,
                            "error_code" to it.error.code,
                            "error_description" to it.error.msg)
                    )
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }
    
    private fun reloadVideosList() {
        cacheManager.clearCacheByUrl(ApiRoutes.GET_MY_CHANNEL_VIDEOS)
        mAdapter.refresh()
    }
    
    override fun onDestroyView() {
        binding.myChannelVideos.adapter = null
        super.onDestroyView()
        _binding = null
    }
}