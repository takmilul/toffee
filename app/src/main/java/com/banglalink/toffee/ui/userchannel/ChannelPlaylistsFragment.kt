package com.banglalink.toffee.ui.userchannel

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.R.layout
import com.banglalink.toffee.apiservice.MyChannelPlaylistParams
import com.banglalink.toffee.common.paging.BaseListFragment
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.databinding.AlertDialogCreatePlaylistBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.model.UgcChannelPlaylist
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.alert_dialog_decision.view.*
import javax.inject.Inject

@AndroidEntryPoint
class ChannelPlaylistsFragment : BaseListFragment<UgcChannelPlaylist>(), BaseListItemCallback<UgcChannelPlaylist> {

    private var enableToolbar: Boolean = false

    private var isOwner: Int = 0
    private var channelId: Int = 0
    override val mAdapter by lazy { ChannelPlaylistListAdapter(this) }

    @Inject lateinit var viewModelAssistedFactory: ChannelPlaylistViewModel.AssistedFactory
    override val mViewModel by viewModels<ChannelPlaylistViewModel> { ChannelPlaylistViewModel.provideFactory(viewModelAssistedFactory, MyChannelPlaylistParams(isOwner, channelId)) }
    private val deletePlaylistViewModel by viewModels<UgcMyChannelDeletePlaylistViewModel>()
    private val editPlaylistViewModel by viewModels<UgcMyChannelCreatePlaylistViewModel>()

    companion object {
        private const val SHOW_TOOLBAR = "enableToolbar"
        private const val IS_OWNER = "isOwner"
        private const val CHANNEL_ID = "channelId"

        fun newInstance(enableToolbar: Boolean, isOwner: Int, channelId: Int): ChannelPlaylistsFragment {
            val instance = ChannelPlaylistsFragment()
            val bundle = Bundle()
            bundle.putBoolean(SHOW_TOOLBAR, enableToolbar)
            bundle.putInt(IS_OWNER, isOwner)
            bundle.putInt(CHANNEL_ID, channelId)
            instance.arguments = bundle
            return instance
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isOwner = arguments?.getInt(IS_OWNER) ?: 1
        channelId = arguments?.getInt(CHANNEL_ID) ?: 2

    }

    override fun onItemClicked(item: UgcChannelPlaylist) {
        super.onItemClicked(item)
        findNavController().navigate(R.id.action_menu_channel_to_channelPlaylistVideosFragment)
    }

    override fun getEmptyViewInfo(): Pair<Int, String?> {
        return Pair(R.drawable.ic_playlists_empty, "You haven't created any playlist yet")
    }

    override fun onOpenMenu(view: View, item: UgcChannelPlaylist) {
        super.onOpenMenu(view, item)

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

    private fun observeEditPlaylist() {
        observe(editPlaylistViewModel.editPlaylistLiveData) {
            when (it) {
                is Success -> {
                    Toast.makeText(requireContext(), it.data.message, Toast.LENGTH_SHORT).show()
                }
                is Failure -> {
                    Toast.makeText(requireContext(), it.error.msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showEditPlaylistDialog(playlistId: Int, playlistName: String) {
        val playlistBinding = AlertDialogCreatePlaylistBinding.inflate(this.layoutInflater)
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
                editPlaylistViewModel.editPlaylist(playlistId)
                alertDialog.dismiss()
            }
            else {
                Toast.makeText(requireContext(), "Please give a playlist name", Toast.LENGTH_SHORT).show()
            }
        }
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
                    mAdapter.refresh()
                    Toast.makeText(requireContext(), it.data.message, Toast.LENGTH_SHORT).show()
                }
                is Failure -> {
                    Toast.makeText(requireContext(), it.error.msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /*private fun showCreatePlaylistDialog() {
        val dialogView: View = this.layoutInflater.inflate(layout.alert_dialog_create_playlist, null)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(dialogView)
        val alertDialog: AlertDialog = dialogBuilder.create().apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
        dialogView.createButton.setOnClickListener { alertDialog.dismiss() }
    }

    private fun showAddToPlaylistDialog() {
        *//*val data = mAdapter.getItems().map { it.program_name!! }
        val fragment = ChannelAddToPlaylistFragment.newInstance(data)
        fragment.show(requireActivity().supportFragmentManager, "add_to_playlist")
        fragment.dialog?.setCanceledOnTouchOutside(true)*//*
    }*/

}