package com.banglalink.toffee.ui.mychannel

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.banglalink.toffee.R
import com.banglalink.toffee.apiservice.GET_MY_CHANNEL_VIDEOS
import com.banglalink.toffee.common.paging.ListLoadStateAdapter
import com.banglalink.toffee.data.database.dao.ReactionDao
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.databinding.FragmentMyChannelVideosBinding
import com.banglalink.toffee.enums.Reaction.Love
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.about.AboutActivity
import com.banglalink.toffee.ui.common.*
import com.banglalink.toffee.ui.home.HomeActivity
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.widget.MarginItemDecoration
import com.banglalink.toffee.ui.widget.VelBoxAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class MyChannelVideosFragment : BaseFragment(), ContentReactionCallback<ChannelInfo> {
    
    private var listJob: Job? = null
    private var channelOwnerId: Int = 0
    private var isOwner: Boolean = false
    @Inject lateinit var reactionDao: ReactionDao
    @Inject lateinit var cacheManager: CacheManager
    private lateinit var mAdapter: MyChannelVideosAdapter
    val mViewModel by viewModels<MyChannelVideosViewModel>()
    private lateinit var binding: FragmentMyChannelVideosBinding
    private val homeViewModel by activityViewModels<HomeViewModel>()
    private val videosReloadViewModel by activityViewModels<MyChannelReloadViewModel>()
    
    companion object {
        private const val CHANNEL_OWNER_ID = "channelOwnerId"
        
        fun newInstance(channelOwnerId: Int): MyChannelVideosFragment {
            return MyChannelVideosFragment().apply {
                arguments = Bundle().apply {
                    putInt(CHANNEL_OWNER_ID, channelOwnerId)
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        mAdapter = MyChannelVideosAdapter(this)
        channelOwnerId = arguments?.getInt(CHANNEL_OWNER_ID) ?: 0
        isOwner = channelOwnerId == mPref.customerId
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMyChannelVideosBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setEmptyView()
        
        with(binding.myChannelVideos) {
            addItemDecoration(MarginItemDecoration(12))
            
            mAdapter.addLoadStateListener {
                binding.progressBar.isVisible = it.source.refresh is LoadState.Loading
                mAdapter.apply {
                    val showEmpty = itemCount <= 0 && !it.source.refresh.endOfPaginationReached && it.source.refresh !is LoadState.Loading
                    binding.emptyView.isVisible = showEmpty
                    binding.myChannelVideos.isVisible = !showEmpty
                }
            }
            adapter = mAdapter.withLoadStateFooter(ListLoadStateAdapter { mAdapter.retry() })
            setHasFixedSize(true)
        }
        
        observeReloadVideos()
        observeDeleteVideo()
        observeMyChannelVideos()
    }
    
    private fun observeMyChannelVideos() {
        listJob?.cancel()
        listJob = lifecycleScope.launchWhenStarted {
            mViewModel.getMyChannelVideos(channelOwnerId).collectLatest {
                mAdapter.submitData(it)
            }
        }
    }
    
    private fun setEmptyView() {
        with(binding) {
            if (isOwner) {
                emptyViewLabel.text = "You haven't uploaded any video yet"
                uploadVideoButton.setOnClickListener {
                    if (requireActivity() is HomeActivity) {
                        (requireActivity() as HomeActivity).showUploadDialog()
                    }
                }
                creatorsPolicyButton.setOnClickListener {
                    val intent = Intent(requireActivity(), HtmlPageViewActivity::class.java).apply {
                        putExtra(HtmlPageViewActivity.CONTENT_KEY, AboutActivity.PRIVACY_POLICY_URL)
                        putExtra(HtmlPageViewActivity.TITLE_KEY, "Creators Policy")
                    }
                    requireActivity().startActivity(intent)
                }
            } else {
                uploadVideoButton.hide()
                creatorsPolicyButton.hide()
                emptyViewLabel.text = "This channel has no video yet"
            }
        }
    }
    
    override fun onOpenMenu(view: View, item: ChannelInfo) {
        super.onOpenMenu(view, item)
        PopupMenu(requireContext(), view).apply {
            if (isOwner) {
                inflate(R.menu.menu_channel_owner_videos)
            } else {
                inflate(R.menu.menu_channel_videos)
            }
            this.menu.removeItem(R.id.menu_share)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_edit_content -> {
                        if (findNavController().currentDestination?.id != R.id.myChannelVideosEditFragment && findNavController().currentDestination?.id == R.id.myChannelHomeFragment) {
                            val action = MyChannelHomeFragmentDirections.actionMyChannelHomeFragmentToMyChannelVideosEditFragment(item)
                            parentFragment?.findNavController()?.navigate(action)
                        } else if(findNavController().currentDestination?.id != R.id.myChannelVideosEditFragment && findNavController().currentDestination?.id == R.id.menu_channel){
                            this@MyChannelVideosFragment.findNavController().navigate(
                                R.id.action_menu_channel_to_myChannelVideosEditFragment,
                                Bundle().apply {
                                    putParcelable(MyChannelVideosEditFragment.CHANNEL_INFO, item)
                                }
                            )
                        }
                    }
                    R.id.menu_add_to_playlist -> {
                        val fragment = MyChannelAddToPlaylistFragment.newInstance(channelOwnerId, item)
                        fragment.show(requireActivity().supportFragmentManager, "add_to_playlist")
                    }
                    R.id.menu_share -> {
                        homeViewModel.shareContentLiveData.postValue(item)
                    }
                    R.id.menu_fav -> {
                        homeViewModel.updateFavorite(item).observe(viewLifecycleOwner, Observer {
                            handleFavoriteResponse(it)
                        })
                    }
                    R.id.menu_delete_content -> {
                        showDeleteVideoDialog(item.id.toInt())
                    }
                }
                return@setOnMenuItemClickListener true
            }
            show()
        }
    }
    
    private fun showDeleteVideoDialog(contentId: Int) {
        VelBoxAlertDialogBuilder(
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
    
    private fun handleFavoriteResponse(it: Resource<ChannelInfo>) {
        when (it) {
            is Success -> {
                val channelInfo = it.data
                when (channelInfo.favorite) {
                    "0" -> {
                        context?.showToast("Content successfully removed from favorite list")
                        handleFavoriteRemovedSuccessFully(channelInfo)
                    }
                    "1" -> {
                        handleFavoriteAddedSuccessfully(channelInfo)
                        context?.showToast("Content successfully added to favorite list")
                    }
                }
            }
            is Failure -> {
                context?.showToast(it.error.msg)
            }
        }
    }
    
    private fun handleFavoriteAddedSuccessfully(channelInfo: ChannelInfo) {}
    
    private fun handleFavoriteRemovedSuccessFully(channelInfo: ChannelInfo) {}
    
    fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {}
    
    override fun onItemClicked(item: ChannelInfo) {
        super.onItemClicked(item)
        homeViewModel.fragmentDetailsMutableLiveData.postValue(item)
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
    
    override fun onShareClicked(view: View, item: ChannelInfo) {
        super.onShareClicked(view, item)
        homeViewModel.shareContentLiveData.postValue(item)
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
                    requireContext().showToast(it.data.message)
                    reloadVideosList()
                }
                is Failure -> requireContext().showToast(it.error.msg)
            }
        }
    }
    
    private fun reloadVideosList() {
        cacheManager.clearCacheByUrl(GET_MY_CHANNEL_VIDEOS)
        mAdapter.refresh()
    }
}