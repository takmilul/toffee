package com.banglalink.toffee.ui.mychannel

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.apiservice.GET_MY_CHANNEL_PLAYLISTS_URL
import com.banglalink.toffee.common.paging.BaseListFragment
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.databinding.AlertDialogMyChannelPlaylistCreateBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.MyChannelPlaylist
import com.banglalink.toffee.model.PlaylistPlaybackInfo
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.widget.VelBoxAlertDialogBuilder
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

    @Inject lateinit var cacheManager: CacheManager
    @Inject lateinit var viewModelAssistedFactory: MyChannelPlaylistViewModel.AssistedFactory
    override val mViewModel by viewModels<MyChannelPlaylistViewModel> { MyChannelPlaylistViewModel.provideFactory(viewModelAssistedFactory, isOwner, channelOwnerId) }
    private val deletePlaylistViewModel by viewModels<MyChannelPlaylistDeleteViewModel>()
    private val editPlaylistViewModel by viewModels<MyChannelPlaylistCreateViewModel>()
    private val playlistReloadViewModel by activityViewModels<MyChannelReloadViewModel>()

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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeEditPlaylist()
        observeDeletePlaylist()
        observeReloadPlaylist()
//        retrofitCache.urls().forEach { Log.e("RETROFIT_URL", "\n\n\n First Load: $it") }
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
                reloadPlaylist()
            }
        }
    }

    override fun onItemClicked(item: MyChannelPlaylist) {
        super.onItemClicked(item)
        /*val adapter = parentFragment?.view?.findViewById<ViewPager2>(R.id.viewPager)?.adapter
        val frg = parentFragmentManager.fragments
        if(adapter is ViewPagerAdapter){
            adapter.replaceFragment(MyChannelPlaylistVideosFragment.newInstance(PlaylistPlaybackInfo(item.id, channelOwnerId, isOwner, item.name, item.totalContent)), 1)
            adapter.createFragment(1)
            adapter.notifyItemChanged(1)
        }*/
        
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
                    Toast.makeText(requireContext(), it.data.message, Toast.LENGTH_SHORT).show()
                    reloadPlaylist()
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
            if (!editPlaylistViewModel.playlistName.isNullOrBlank()) {
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
                    Toast.makeText(requireContext(), it.data.message, Toast.LENGTH_SHORT).show()
                    reloadPlaylist()
                }
                is Failure -> {
                    Toast.makeText(requireContext(), it.error.msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun reloadPlaylist(){
        cacheManager.clearCacheByUrl(GET_MY_CHANNEL_PLAYLISTS_URL)
        mAdapter.refresh()
    }
}