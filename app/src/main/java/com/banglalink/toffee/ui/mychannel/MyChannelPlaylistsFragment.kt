package com.banglalink.toffee.ui.mychannel

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.R.layout
import com.banglalink.toffee.common.paging.BaseListFragment
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.databinding.AlertDialogMyChannelPlaylistCreateBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.MyChannelPlaylist
import com.banglalink.toffee.model.PlaylistPlaybackInfo
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.alert_dialog_decision.view.*
import kotlinx.android.synthetic.main.layout_my_channel_playlist_empty_view.*
import javax.inject.Inject

@AndroidEntryPoint
class MyChannelPlaylistsFragment : BaseListFragment<MyChannelPlaylist>(), BaseListItemCallback<MyChannelPlaylist> {

    private var enableToolbar: Boolean = false

    private var isOwner: Int = 0
    private var channelId: Int = 0
    private var channelOwnerId: Int = 0
    override val mAdapter by lazy { MyChannelPlaylistAdapter(this) }

    @Inject lateinit var viewModelAssistedFactory: MyChannelPlaylistViewModel.AssistedFactory
    override val mViewModel by viewModels<MyChannelPlaylistViewModel> { MyChannelPlaylistViewModel.provideFactory(viewModelAssistedFactory, isOwner, channelOwnerId) }
    private val deletePlaylistViewModel by viewModels<MyChannelPlaylistDeleteViewModel>()
    private val editPlaylistViewModel by viewModels<MyChannelPlaylistCreateViewModel>()
    private val playlistReloadViewModel by activityViewModels<MyChannelPlaylistReloadViewModel>()
    private val createPlaylistViewModel by viewModels<MyChannelPlaylistCreateViewModel>()

    companion object {
        private const val SHOW_TOOLBAR = "enableToolbar"
        const val IS_OWNER = "isOwner"
        const val CHANNEL_OWNER_ID = "channelOwnerId"
        const val CHANNEL_ID = "channelId"
        const val PLAYLIST_INFO = "playlistInfo"

        fun newInstance(enableToolbar: Boolean, isOwner: Int, channelOwnerId: Int, channelId: Int): MyChannelPlaylistsFragment {
            val instance = MyChannelPlaylistsFragment()
            val bundle = Bundle()
            bundle.putBoolean(SHOW_TOOLBAR, enableToolbar)
            bundle.putInt(IS_OWNER, isOwner)
            bundle.putInt(CHANNEL_OWNER_ID, channelOwnerId)
            bundle.putInt(CHANNEL_ID, channelId)
            instance.arguments = bundle
            return instance
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isOwner = arguments?.getInt(IS_OWNER) ?: 1
        channelOwnerId = arguments?.getInt(CHANNEL_OWNER_ID) ?: 0
        channelId = arguments?.getInt(MyChannelHomeFragment.CHANNEL_ID) ?: 0
        mAdapter.isOwner = isOwner
        observeReloadPlaylist()
    }
    
    override fun setEmptyView() {
        val customView = layoutInflater.inflate(R.layout.layout_my_channel_playlist_empty_view, null)
        with(binding.emptyView) {
            removeAllViews()
            addView(customView)
            gravity = Gravity.CENTER_HORIZONTAL
            visibility = View.VISIBLE
            if (isOwner == 1){
                empty_view_label.text = "You haven't created any playlist yet"
                uploadVideoButton.setOnClickListener { 
                    if(parentFragment is MyChannelHomeFragment){
                        (parentFragment as MyChannelHomeFragment).showCreatePlaylistDialog()
                    }
                }
            }
            else{
                uploadVideoButton.visibility = View.GONE
                empty_view_label.text = "This channel has no playlist yet"
            }
        }
    }

    private fun observeReloadPlaylist() {
        observe(playlistReloadViewModel.reloadPlaylist) {
            if (it) {
                mAdapter.refresh()
            }
        }
    }

    override fun onItemClicked(item: MyChannelPlaylist) {
        super.onItemClicked(item)
        if (findNavController().currentDestination?.id == R.id.myChannelHomeFragment){
            val action = MyChannelHomeFragmentDirections.actionMyChannelHomeFragmentToMyChannelPlaylistVideosFragment(PlaylistPlaybackInfo(item.id, channelOwnerId, isOwner, item.name, item.totalContent))
            parentFragment?.findNavController()?.navigate(action)
        }
        else {
            findNavController().navigate(R.id.action_menu_channel_to_myChannelPlaylistVideosFragment, Bundle().apply {
                putParcelable(PLAYLIST_INFO, PlaylistPlaybackInfo(item.id, channelOwnerId, isOwner, item.name, item.totalContent))
            })
        }
    }

    override fun onOpenMenu(view: View, item: MyChannelPlaylist) {
        super.onOpenMenu(view, item)

        if (isOwner == 1) {
            android.widget.PopupMenu(requireContext(), view).apply {
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
                    Toast.makeText(requireContext(), it.data.message, Toast.LENGTH_SHORT).show()
                    mAdapter.refresh()
                }
                is Failure -> {
                    Toast.makeText(requireContext(), it.error.msg, Toast.LENGTH_SHORT).show()
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
            if (!editPlaylistViewModel.playlistName.isNullOrEmpty()) {
                observeEditPlaylist()
                editPlaylistViewModel.editPlaylist(playlistId, channelOwnerId, isOwner)
                alertDialog.dismiss()
            }
            else {
                Toast.makeText(requireContext(), "Please give a playlist name", Toast.LENGTH_SHORT).show()
            }
        }
        playlistBinding.closeIv.setOnClickListener{ alertDialog.dismiss() }
    }

    private fun showDeletePlaylistDialog(playlistId: Int) {
        val dialogView: View = this.layoutInflater.inflate(layout.alert_dialog_decision, null)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(dialogView)
        val alertDialog: AlertDialog = dialogBuilder.create().apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
        observeDeletePlaylist()
        with(dialogView) {
            noButton.setOnClickListener { alertDialog.dismiss() }
            deleteButton.setOnClickListener {
                deletePlaylistViewModel.deletePlaylistName(playlistId)
                alertDialog.dismiss()
            }
        }
    }

    private fun observeDeletePlaylist() {
        observe(deletePlaylistViewModel.liveData) {
            when (it) {
                is Success -> {
                    Toast.makeText(requireContext(), it.data.message, Toast.LENGTH_SHORT).show()
                    mAdapter.refresh()
                }
                is Failure -> {
                    Toast.makeText(requireContext(), it.error.msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}