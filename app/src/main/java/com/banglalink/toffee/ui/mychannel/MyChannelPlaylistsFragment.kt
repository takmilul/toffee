package com.banglalink.toffee.ui.mychannel

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.banglalink.toffee.R
import com.banglalink.toffee.R.string
import com.banglalink.toffee.apiservice.ApiRoutes
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.ListLoadStateAdapter
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.databinding.AlertDialogMyChannelPlaylistCreateBinding
import com.banglalink.toffee.databinding.FragmentMyChannelPlaylistsBinding
import com.banglalink.toffee.extension.checkVerification
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.MyChannelPlaylist
import com.banglalink.toffee.model.PlaylistPlaybackInfo
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.widget.MarginItemDecoration
import com.banglalink.toffee.ui.widget.VelBoxAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class MyChannelPlaylistsFragment : BaseFragment(), BaseListItemCallback<MyChannelPlaylist> {
    
    private var listJob: Job? = null
    private var channelOwnerId: Int = 0
    private var isOwner: Boolean = false
    @Inject lateinit var cacheManager: CacheManager
    private lateinit var mAdapter: MyChannelPlaylistAdapter
    private var _binding: FragmentMyChannelPlaylistsBinding ? = null
    private val binding get() = _binding!!
    val mViewModel by viewModels<MyChannelPlaylistViewModel>()
    private val playlistReloadViewModel by activityViewModels<MyChannelReloadViewModel>()
    private val editPlaylistViewModel by viewModels<MyChannelPlaylistCreateViewModel>()
    private val deletePlaylistViewModel by viewModels<MyChannelPlaylistDeleteViewModel>()
    
    companion object {
        const val CHANNEL_OWNER_ID = "channelOwnerId"
        const val PLAYLIST_INFO = "playlistInfo"
        
        fun newInstance(channelOwnerId: Int): MyChannelPlaylistsFragment {
            return MyChannelPlaylistsFragment().apply {
                arguments = Bundle().apply {
                    putInt(CHANNEL_OWNER_ID, channelOwnerId)
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAdapter = MyChannelPlaylistAdapter(this)
        channelOwnerId = arguments?.getInt(CHANNEL_OWNER_ID) ?: 0
        isOwner = channelOwnerId == mPref.customerId
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentMyChannelPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEmptyView()
        with(binding.myChannelPlaylists) {
            addItemDecoration(MarginItemDecoration(12))
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                mAdapter.loadStateFlow
                    .collectLatest {
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
        if (isOwner && !mPref.isVerifiedUser) {
            return
        }
        observeMyChannelPlaylists()
        observeEditPlaylist()
        observeDeletePlaylist()
        observeReloadPlaylist()
    }
    
    private fun setEmptyView() {
        with(binding) {
            if (isOwner) {
                emptyViewLabel.text = getString(string.empty_playlist_msg_owner)
                createPlaylistButton.setOnClickListener {
                    requireActivity().checkVerification {
                        if (mPref.channelId > 0) {
                            if (parentFragment?.parentFragment?.parentFragment is MyChannelHomeFragment) {
                                (parentFragment?.parentFragment?.parentFragment as? MyChannelHomeFragment)?.showCreatePlaylistDialog()
                            }
                        } else {
                            requireContext().showToast(getString(string.create_channel_msg))
                        }
                    }
                }
            } else {
                createPlaylistButton.visibility = View.GONE
                emptyViewLabel.text = getString(string.empty_playlist_msg_user)
            }
        }
    }
    
    private fun observeMyChannelPlaylists() {
        listJob?.cancel()
        listJob = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            mViewModel.getMyChannelPlaylists(channelOwnerId).collectLatest {
                mAdapter.submitData(it)
            }
        }
    }
    
    private fun observeReloadPlaylist() {
        observe(playlistReloadViewModel.reloadPlaylist) {
            if (it) {
                reloadPlaylist()
            }
        }
    }
    
    override fun onItemClicked(item: MyChannelPlaylist) {
        super.onItemClicked(item)
        if (findNavController().currentDestination?.id != R.id.myChannelPlaylistVideosFragment && findNavController().currentDestination?.id == R.id.myChannelPlaylistsFragment) {
            findNavController().navigate(R.id.action_myChannelPlaylistsFragment_to_myChannelPlaylistVideosFragment, Bundle().apply {
                putParcelable(PLAYLIST_INFO, PlaylistPlaybackInfo(item.id, channelOwnerId, item.name, item.totalContent))
            })
        }
    }
    
    override fun onOpenMenu(view: View, item: MyChannelPlaylist) {
        super.onOpenMenu(view, item)
        if (isOwner) {
            PopupMenu(requireContext(), view).apply {
                inflate(R.menu.menu_channel_playlist)
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.menu_edit_playlist -> {
                            showEditPlaylistDialog(item.id, item.name)
                        }
                        R.id.menu_delete_playlist -> {
                            showDeletePlaylistDialog(item.id)
                        }
                    }
                    return@setOnMenuItemClickListener true
                }
                show()
            }
        }
    }
    
    private fun observeEditPlaylist() {
        observe(editPlaylistViewModel.editPlaylistLiveData) {
            when (it) {
                is Success -> {
                    requireContext().showToast(it.data.message)
                    reloadPlaylist()
                }
                is Failure -> {
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }
    
    private fun showEditPlaylistDialog(playlistId: Int, playlistName: String) {
        val playlistBinding = AlertDialogMyChannelPlaylistCreateBinding.inflate(this.layoutInflater)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(playlistBinding.root)
        val alertDialog: AlertDialog = dialogBuilder.create().apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
        playlistBinding.viewModel = editPlaylistViewModel
        editPlaylistViewModel.playlistName = playlistName
        playlistBinding.dialogTitleTextView.text = "Edit Playlist"
        playlistBinding.createButton.text = "Save"
        playlistBinding.createButton.setOnClickListener {
            if (!editPlaylistViewModel.playlistName.isNullOrBlank()) {
                editPlaylistViewModel.editPlaylist(playlistId, channelOwnerId)
                alertDialog.dismiss()
            } else {
                requireContext().showToast("Please give a playlist name")
            }
        }
        playlistBinding.closeIv.setOnClickListener { alertDialog.dismiss() }
    }
    
    private fun showDeletePlaylistDialog(playlistId: Int) {
        VelBoxAlertDialogBuilder(
            requireContext(),
            text = "Are you sure to delete?",
            positiveButtonTitle = "No",
            negativeButtonTitle = "Delete",
            positiveButtonListener = { it?.dismiss() },
            negativeButtonListener = {
                deletePlaylistViewModel.deletePlaylistName(playlistId)
                it?.dismiss()
            }
        ).create().show()
    }
    
    private fun observeDeletePlaylist() {
        observe(deletePlaylistViewModel.liveData) {
            when (it) {
                is Success -> {
                    requireContext().showToast(it.data.message)
                    reloadPlaylist()
                }
                is Failure -> {
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }
    
    private fun reloadPlaylist() {
        cacheManager.clearCacheByUrl(ApiRoutes.GET_MY_CHANNEL_PLAYLISTS)
        mAdapter.refresh()
    }

    override fun onDestroyView() {
        binding.myChannelPlaylists.adapter = null
        super.onDestroyView()
        _binding = null
    }
}