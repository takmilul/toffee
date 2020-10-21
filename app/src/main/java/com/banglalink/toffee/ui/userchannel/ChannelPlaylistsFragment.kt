package com.banglalink.toffee.ui.userchannel

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.R.layout
import com.banglalink.toffee.apiservice.MyChannelPlaylistParams
import com.banglalink.toffee.common.paging.BaseListFragment
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.model.UgcChannelPlaylist
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.alert_dialog_create_playlist.view.*
import kotlinx.android.synthetic.main.alert_dialog_create_playlist.view.dialogTitleTextView
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

    /*override fun initAdapter() {
        mAdapter = ChannelPlaylistListAdapter(this)
        mViewModel = ViewModelProvider(this).get(ChannelPlaylistViewModel::class.java)
        enableToolbar = arguments?.getBoolean(SHOW_TOOLBAR) ?: false
        mViewModel.enableToolbar = enableToolbar
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isOwner = arguments?.getInt(IS_OWNER) ?: 1
        channelId = arguments?.getInt(CHANNEL_ID) ?: 2

    }
    
    override fun onItemClicked(item: UgcChannelPlaylist) {
        super.onItemClicked(item)
        findNavController().navigate(R.id.action_menu_channel_to_channelPlaylistVideosFragment)
//        showCreatePlaylistDialog()
//        showAddToPlaylistDialog()
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
        dialogView.createButton.setOnClickListener { alertDialog.dismiss() }
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
            createButton.text = "Save"
            createButton.setOnClickListener { alertDialog.dismiss() }
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
    
    private fun showAddToPlaylistDialog(){
        /*val data = mAdapter.getItems().map { it.program_name!! }
        val fragment = ChannelAddToPlaylistFragment.newInstance(data)
        fragment.show(requireActivity().supportFragmentManager, "add_to_playlist")
        fragment.dialog?.setCanceledOnTouchOutside(true)*/
    }

}