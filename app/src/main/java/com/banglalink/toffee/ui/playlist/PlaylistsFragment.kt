package com.banglalink.toffee.ui.playlist

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.banglalink.toffee.R
import com.banglalink.toffee.apiservice.GET_MY_CHANNEL_PLAYLISTS
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.ListLoadStateAdapter
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.databinding.AlertDialogMyChannelPlaylistCreateBinding
import com.banglalink.toffee.databinding.FragmentPlaylistsBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.MyChannelPlaylist
import com.banglalink.toffee.model.PlaylistPlaybackInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.mychannel.*
import com.banglalink.toffee.ui.widget.MarginItemDecoration
import com.banglalink.toffee.ui.widget.VelBoxAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject
@AndroidEntryPoint
class PlaylistsFragment : BaseFragment(), BaseListItemCallback<MyChannelPlaylist> {
    private var listJob: Job? = null

    private var isOwner: Boolean = false
    @Inject
    lateinit var cacheManager: CacheManager
    private lateinit var mAdapter: MyChannelPlaylistAdapter
    val mViewModel by viewModels<MyChannelPlaylistViewModel>()
    private var _binding:FragmentPlaylistsBinding?=null
    private val binding get() = _binding!!
    private val editPlaylistViewModel by viewModels<MyChannelPlaylistCreateViewModel>()
    private val deletePlaylistViewModel by viewModels<MyChannelPlaylistDeleteViewModel>()
    private val playlistReloadViewModel by activityViewModels<MyChannelReloadViewModel>()
    private val createPlaylistViewModel by viewModels<MyChannelPlaylistCreateViewModel>()
    companion object {
        const val CHANNEL_OWNER_ID = "mPref.customerId"
        const val PLAYLIST_INFO = "playlistInfo"

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAdapter = MyChannelPlaylistAdapter(this)

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onDestroyView() {
        binding.myChannelPlaylists.adapter = null
        super.onDestroyView()
        _binding = null
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEmptyView()

        with(binding.myChannelPlaylists) {
            addItemDecoration(MarginItemDecoration(12))

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                mAdapter.loadStateFlow
//                    .distinctUntilChangedBy { it.refresh }
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

        observeMyChannelPlaylists()
        observeEditPlaylist()
        observeDeletePlaylist()
        observeReloadPlaylist()
    }

    private fun setEmptyView() {
        with(binding) {
            if (mPref.customerId>0) {
                emptyViewLabel.text = "You haven't created any playlist yet"
                createPlaylistButton.setOnClickListener {
                    if (mPref.customerId > 0) {
                        showCreatePlaylistDialog()
                    }
                }
            } else {
                createPlaylistButton.visibility = View.GONE
                emptyViewLabel.text = "This channel has no playlist yet"
            }
        }
    }
    fun showCreatePlaylistDialog() {
        val playlistBinding = AlertDialogMyChannelPlaylistCreateBinding.inflate(this.layoutInflater)
        val dialogBuilder = android.app.AlertDialog.Builder(requireContext()).setView(playlistBinding.root)
        val alertDialog: android.app.AlertDialog = dialogBuilder.create().apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
        playlistBinding.viewModel = createPlaylistViewModel
        playlistBinding.createButton.setOnClickListener {
            if (!createPlaylistViewModel.playlistName.isNullOrBlank()) {
                observeCreatePlaylist()
                createPlaylistViewModel.createPlaylist(mPref.customerId)
                createPlaylistViewModel.playlistName = null
                alertDialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Please give a playlist name", Toast.LENGTH_SHORT).show()
            }
        }
        playlistBinding.closeIv.setOnClickListener { alertDialog.dismiss() }
    }
    private fun observeCreatePlaylist() {
        observe(createPlaylistViewModel.createPlaylistLiveData) {
            when (it) {
                is Resource.Success -> {
                    requireContext().showToast(it.data.message ?: "")
                    playlistReloadViewModel.reloadPlaylist.value = true
                }
                is Resource.Failure -> {
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }
    private fun observeMyChannelPlaylists() {
        Log.e("my","channel"+mPref.customerId)
        listJob?.cancel()
        listJob = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            mViewModel.getMyChannelPlaylists(mPref.customerId).collectLatest {
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

        if (findNavController().currentDestination?.id != R.id.menu_playlist_videos && findNavController().currentDestination?.id == R.id.menu_playlist) {
            findNavController().navigate(R.id.actionPlaylistsFragment_to_PlayListVideosFragment, Bundle().apply {
                putParcelable(PLAYLIST_INFO, PlaylistPlaybackInfo(item.id, mPref.customerId, item.name, item.totalContent))
            })
        }
    }

    override fun onOpenMenu(view: View, item: MyChannelPlaylist) {
        super.onOpenMenu(view, item)

        if (mPref.customerId>0) {
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
                is Resource.Success -> {
                    requireContext().showToast(it.data.message)
                    reloadPlaylist()
                }
                is Resource.Failure -> {
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
                editPlaylistViewModel.editPlaylist(playlistId, mPref.customerId)
                alertDialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Please give a playlist name", Toast.LENGTH_SHORT).show()
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
                is Resource.Success -> {
                    requireContext().showToast(it.data.message)
                    reloadPlaylist()
                }
                is Resource.Failure -> {
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }

    private fun reloadPlaylist() {
        cacheManager.clearCacheByUrl(GET_MY_CHANNEL_PLAYLISTS)
        mAdapter.refresh()
    }

}