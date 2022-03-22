package com.banglalink.toffee.ui.userplaylist

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.banglalink.toffee.R
import com.banglalink.toffee.R.string
import com.banglalink.toffee.analytics.FirebaseParams
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.apiservice.ApiRoutes
import com.banglalink.toffee.apiservice.BrowsingScreens
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.ListLoadStateAdapter
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.databinding.AlertDialogMyChannelPlaylistCreateBinding
import com.banglalink.toffee.databinding.FragmentUserPlaylistBinding
import com.banglalink.toffee.extension.*
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class UserPlaylistFragment : BaseFragment(), BaseListItemCallback<MyChannelPlaylist> {
    private var listJob: Job? = null
    @Inject lateinit var cacheManager: CacheManager
    private lateinit var mAdapter: MyChannelPlaylistAdapter
    private var _binding: FragmentUserPlaylistBinding? = null
    private val binding get() = _binding!!
    val mViewModel by viewModels<MyChannelPlaylistViewModel>()
    private val playlistReloadViewModel by activityViewModels<MyChannelReloadViewModel>()
    private val createPlaylistViewModel by viewModels<MyChannelPlaylistCreateViewModel>()
    private val deletePlaylistViewModel by viewModels<MyChannelPlaylistDeleteViewModel>()

    companion object {
        const val PLAYLIST_INFO = "playlistInfo"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAdapter = MyChannelPlaylistAdapter(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUserPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            var isInitialized = false
            myChannelPlaylists.addItemDecoration(MarginItemDecoration(12))
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                mAdapter.loadStateFlow.collectLatest {
                    mAdapter.apply {
                        val isLoading = it.source.refresh is LoadState.Loading || !isInitialized
                        val isEmpty = mAdapter.itemCount <= 0 && !it.source.refresh.endOfPaginationReached
                        progressBar.isVisible = isLoading
                        emptyView.isVisible = isEmpty && !isLoading
                        myChannelPlaylists.isVisible = !isEmpty && !isLoading
                        createPlaylistButton.isVisible = !isEmpty && !isLoading
                        isInitialized = true
                    }
                }
            }
            myChannelPlaylists.setHasFixedSize(true)
            myChannelPlaylists.adapter = mAdapter.withLoadStateFooter(ListLoadStateAdapter { mAdapter.retry() })
            createPlaylistButton.safeClick({ requireActivity().checkVerification { showCreatePlaylistDialog() } })
            createPlaylistButtonNone.safeClick({ requireActivity().checkVerification { showCreatePlaylistDialog() } })
        }
        observeMyChannelPlaylists()
        observeEditPlaylist()
        observeDeletePlaylist()
        observeReloadPlaylist()
    }

    private fun showCreatePlaylistDialog() {
        val playlistBinding = AlertDialogMyChannelPlaylistCreateBinding.inflate(this.layoutInflater)
        val dialogBuilder = android.app.AlertDialog.Builder(requireContext()).setView(playlistBinding.root)
        val alertDialog: android.app.AlertDialog = dialogBuilder.create().apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
        createPlaylistViewModel.playlistName = ""
        playlistBinding.playlistNameEditText.text.clear()
        playlistBinding.viewModel = createPlaylistViewModel
        playlistBinding.createButton.safeClick({
            if (!createPlaylistViewModel.playlistName.isNullOrBlank()) {
                observeCreatePlaylist()
                createPlaylistViewModel.createPlaylist(mPref.customerId, 1)
                createPlaylistViewModel.playlistName = null
                alertDialog.dismiss()
            } else {
                requireContext().showToast(getString(string.playlist_name_empty_msg))
            }
        })
        playlistBinding.closeIv.safeClick({ alertDialog.dismiss() })
    }

    private fun observeCreatePlaylist() {
        observe(createPlaylistViewModel.createPlaylistLiveData) {
            when (it) {
                is Resource.Success -> {
                    requireContext().showToast(it.data.message ?: "")
                    cacheManager.clearCacheByUrl(ApiRoutes.GET_USER_PLAYLISTS)
                    playlistReloadViewModel.reloadPlaylist.value = true
                }
                is Resource.Failure -> {
                    ToffeeAnalytics.logEvent(
                        ToffeeEvents.EXCEPTION,
                        bundleOf(
                            "api_name" to ApiNames.CREATE_PLAYLIST,
                            FirebaseParams.BROWSER_SCREEN to BrowsingScreens.MY_CHANNEL_PLAYLIST_PAGE,
                            "error_code" to it.error.code,
                            "error_description" to it.error.msg)
                    )
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }
    
    private fun observeMyChannelPlaylists() {
        listJob?.cancel()
        listJob = viewLifecycleOwner.lifecycleScope.launch {
            mViewModel.getMyChannelUserPlaylists(mPref.customerId).collectLatest {
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
        findNavController().navigate(R.id.userPlaylistVideos, Bundle().apply {
            putParcelable(PLAYLIST_INFO, PlaylistPlaybackInfo(item.id, mPref.customerId, item.name, item.totalContent, item.playlistShareUrl, item
                .isApproved, true))
        })
    }

    private fun observeEditPlaylist() {
        observe(createPlaylistViewModel.editPlaylistLiveData) {
            when (it) {
                is Resource.Success -> {
                    requireContext().showToast(it.data.message)
                    cacheManager.clearCacheByUrl(ApiRoutes.GET_USER_PLAYLISTS)
                    reloadPlaylist()
                }
                is Resource.Failure -> {
                    ToffeeAnalytics.logEvent(
                        ToffeeEvents.EXCEPTION,
                        bundleOf(
                            "api_name" to ApiNames.EDIT_PLAYLIST,
                            FirebaseParams.BROWSER_SCREEN to BrowsingScreens.MY_CHANNEL_PLAYLIST_PAGE,
                            "error_code" to it.error.code,
                            "error_description" to it.error.msg)
                    )
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }

    private fun showEditPlaylistDialog(playlistId: Int, playlistName: String) {
        val playlistBinding = AlertDialogMyChannelPlaylistCreateBinding.inflate(this.layoutInflater)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(playlistBinding.root)
        val alertDialog = dialogBuilder.create().apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
        createPlaylistViewModel.playlistName = playlistName
        with(playlistBinding) {
            viewModel = createPlaylistViewModel
            dialogTitleTextView.text = getString(string.edit_playlist)
            createButton.text = getString(string.save_text)
            createButton.setOnClickListener {
                if (!createPlaylistViewModel.playlistName.isNullOrBlank()) {
                    createPlaylistViewModel.editPlaylist(playlistId, mPref.customerId, 1)
                    alertDialog.dismiss()
                } else {
                    requireContext().showToast(getString(string.playlist_name_empty_msg))
                }
            }
            closeIv.setOnClickListener { alertDialog.dismiss() }
        }
    }

    private fun showDeletePlaylistDialog(playlistId: Int) {
        VelBoxAlertDialogBuilder(
            requireContext(),
            text = getString(string.delete_confirmation),
            positiveButtonTitle = getString(string.no_text),
            negativeButtonTitle = getString(string.delete_text),
            positiveButtonListener = { it?.dismiss() },
            negativeButtonListener = {
                deletePlaylistViewModel.deletePlaylistName(playlistId, 1)
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
                    ToffeeAnalytics.logEvent(
                        ToffeeEvents.EXCEPTION,
                        bundleOf(
                            "api_name" to ApiNames.DELETE_PLAYLIST,
                            FirebaseParams.BROWSER_SCREEN to BrowsingScreens.MY_CHANNEL_PLAYLIST_PAGE,
                            "error_code" to it.error.code,
                            "error_description" to it.error.msg)
                    )
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }

    private fun reloadPlaylist() {
        cacheManager.clearCacheByUrl(ApiRoutes.GET_USER_PLAYLISTS)
        mAdapter.refresh()
    }

    override fun onOpenMenu(view: View, item: MyChannelPlaylist) {
        super.onOpenMenu(view, item)
        PopupMenu(requireContext(), view).apply {
            inflate(R.menu.menu_channel_playlist)
            menu.findItem(R.id.menu_share_playlist).isVisible = item.isApproved == 1
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_edit_playlist -> {
                        showEditPlaylistDialog(item.id, item.name)
                    }
                    R.id.menu_delete_playlist -> {
                        showDeletePlaylistDialog(item.id)
                    }
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