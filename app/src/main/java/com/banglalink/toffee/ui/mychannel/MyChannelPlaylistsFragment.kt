package com.banglalink.toffee.ui.mychannel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.R.string
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.ListLoadStateAdapter
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.databinding.FragmentMyChannelPlaylistsBinding
import com.banglalink.toffee.extension.handleUrlShare
import com.banglalink.toffee.model.MyChannelPlaylist
import com.banglalink.toffee.model.PlaylistPlaybackInfo
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.widget.MarginItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyChannelPlaylistsFragment : BaseFragment(), BaseListItemCallback<MyChannelPlaylist> {
    
    private var channelOwnerId: Int = 0
    @Inject lateinit var cacheManager: CacheManager
    private lateinit var mAdapter: MyChannelPlaylistAdapter
    private var _binding: FragmentMyChannelPlaylistsBinding ? = null
    private val binding get() = _binding!!
    private val mViewModel by activityViewModels<MyChannelPlaylistViewModel>()
    
    companion object {
        const val IS_MY_CHANNEL = "isMyChannel"
        const val PLAYLIST_INFO = "playlistInfo"
        const val CHANNEL_OWNER_ID = "channelOwnerId"
        
        fun newInstance(channelOwnerId: Int, isMyChannel: Boolean): MyChannelPlaylistsFragment {
            return MyChannelPlaylistsFragment().apply {
                arguments = bundleOf(CHANNEL_OWNER_ID to channelOwnerId, IS_MY_CHANNEL to isMyChannel)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAdapter = MyChannelPlaylistAdapter(this)
        arguments?.let {
            channelOwnerId = it.getInt(CHANNEL_OWNER_ID)
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentMyChannelPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressBar.load(R.drawable.content_loader)
        setEmptyView()
        with(binding.myChannelPlaylists) {
            addItemDecoration(MarginItemDecoration(12))
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                mAdapter.loadStateFlow.collectLatest {
                    binding.progressBar.isVisible = it.source.refresh is LoadState.Loading
                    mAdapter.apply {
                        val showEmpty = itemCount <= 0 && !it.source.refresh.endOfPaginationReached && it.source.refresh !is LoadState.Loading
                        binding.emptyView.isVisible = showEmpty
                        binding.myChannelPlaylists.isVisible = !showEmpty
                    }
                }
            }
            adapter = mAdapter.withLoadStateFooter(ListLoadStateAdapter { mAdapter.retry() })
            setHasFixedSize(true)
        }
        
        observeMyChannelPlaylists()
    }
    
    private fun setEmptyView() {
        with(binding) {
            emptyViewLabel.text = getString(string.empty_playlist_msg_user)
            emptyView.isVisible = true
        }
    }
    
    private fun observeMyChannelPlaylists() {
        viewLifecycleOwner.lifecycleScope.launch {
            mViewModel.getMyChannelPlaylists(channelOwnerId).collectLatest {
                mAdapter.submitData(it)
            }
        }
    }
    
    override fun onItemClicked(item: MyChannelPlaylist) {
        super.onItemClicked(item)
        if (findNavController().currentDestination?.id != R.id.myChannelPlaylistVideosFragment && findNavController().currentDestination?.id == R.id.myChannelPlaylistsFragment) {
            findNavController().navigate(R.id.action_myChannelPlaylistsFragment_to_myChannelPlaylistVideosFragment, Bundle().apply {
                putParcelable(PLAYLIST_INFO, PlaylistPlaybackInfo(item.id, channelOwnerId, item.name ?: "", item.totalContent, item
                    .playlistShareUrl, item.isApproved))
            })
        }
    }
    
    override fun onOpenMenu(view: View, item: MyChannelPlaylist) {
        super.onOpenMenu(view, item)
            PopupMenu(requireContext(), view).apply {
                inflate(R.menu.menu_channel_playlist)
                menu.findItem(R.id.menu_edit_playlist).isVisible = false
                menu.findItem(R.id.menu_delete_playlist).isVisible = false
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.menu_share_playlist -> {
                            item.playlistShareUrl?.let { requireActivity().handleUrlShare(it) }
                        }
                    }
                    return@setOnMenuItemClickListener true
                }
                show()
            }
        }
    
    override fun onDestroyView() {
        binding.myChannelPlaylists.adapter = null
        super.onDestroyView()
        _binding = null
    }
}