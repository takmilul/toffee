package com.banglalink.toffee.ui.userchannel

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.ViewModelProvider
import com.banglalink.toffee.R
import com.banglalink.toffee.R.layout
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.SingleListFragmentV2
import com.banglalink.toffee.ui.common.SingleListItemCallback
import kotlinx.android.synthetic.main.alert_dialog_create_playlist.view.*
import kotlinx.android.synthetic.main.alert_dialog_create_playlist.view.dialogTitleTextView
import kotlinx.android.synthetic.main.alert_dialog_decision.view.*

class ChannelPlaylistsFragment : SingleListFragmentV2<ChannelInfo>(), SingleListItemCallback<ChannelInfo> {

    private var enableToolbar: Boolean = false

    companion object {
        private const val SHOW_TOOLBAR = "enableToolbar"
        fun newInstance(enableToolbar: Boolean): ChannelPlaylistsFragment {
            val instance = ChannelPlaylistsFragment()
            val bundle = Bundle()
            bundle.putBoolean(SHOW_TOOLBAR, enableToolbar)
            instance.arguments = bundle
            return instance
        }
    }

    override fun initAdapter() {
        mAdapter = ChannelPlaylistListAdapter(this)
        mViewModel = ViewModelProvider(this).get(ChannelPlaylistViewModel::class.java)
        enableToolbar = arguments?.getBoolean(SHOW_TOOLBAR) ?: false
        mViewModel.enableToolbar = enableToolbar
    }

    override fun onItemClicked(item: ChannelInfo) {
        super.onItemClicked(item)
        showCreatePlaylistDialog()
        //findNavController().navigate(R.id.action_menu_channel_to_channelPlaylistVideosFragment)
    }

    override fun getEmptyViewInfo(): Pair<Int, String?> {
        return Pair(R.drawable.ic_playlists_empty, "You haven't created any playlist yet")
    }

    override fun onOpenMenu(anchor: View, item: ChannelInfo) {
        super.onOpenMenu(anchor, item)

        PopupMenu(requireContext(), anchor).apply {
            inflate(R.menu.menu_channel_playlist)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_edit_playlist -> {
                        showEditPlaylistDialog()
                    }
                    R.id.menu_delete_playlist -> {
                        showDeletePlaylistDialog()
                    }
                }
                return@setOnMenuItemClickListener true
            }
            show()
        }
    }
    
    private fun showCreatePlaylistDialog() {
        val dialogView: View = this.layoutInflater.inflate(layout.alert_dialog_create_playlist, null)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(dialogView)
        val alertDialog: AlertDialog = dialogBuilder.create().apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
        dialogView.submitButton.setOnClickListener { alertDialog.dismiss() }
    }
    
    private fun showEditPlaylistDialog() {
        val dialogView: View = this.layoutInflater.inflate(layout.alert_dialog_create_playlist, null)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(dialogView)
        val alertDialog: AlertDialog = dialogBuilder.create().apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
        with(dialogView){
            dialogTitleTextView.text = "Edit Playlist"
            submitButton.text = "Save"
            submitButton.setOnClickListener { alertDialog.dismiss() }
        }
    }
    
    private fun showDeletePlaylistDialog(){
        val dialogView: View = this.layoutInflater.inflate(layout.alert_dialog_decision, null)
        val dialogBuilder = AlertDialog.Builder(requireContext()).setView(dialogView)
        val alertDialog: AlertDialog = dialogBuilder.create().apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
        with(dialogView){
            noButton.setOnClickListener { alertDialog.dismiss() }
            deleteButton.setOnClickListener { alertDialog.dismiss() }
        }
    }
}