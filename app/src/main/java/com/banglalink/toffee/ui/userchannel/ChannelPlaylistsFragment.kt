package com.banglalink.toffee.ui.userchannel

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.model.ChannelPlaylist
import com.banglalink.toffee.ui.common.SingleListFragmentV2
import com.banglalink.toffee.ui.common.SingleListItemCallback

class ChannelPlaylistsFragment : SingleListFragmentV2<ChannelPlaylist>(), SingleListItemCallback<ChannelPlaylist> {
    
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
    
    override fun onItemClicked(item: ChannelPlaylist) {
        findNavController().navigate(R.id.action_menu_channel_to_channelPlaylistVideosFragment)
    }
    
    override fun getEmptyViewInfo(): Pair<Int, String?> {
        return Pair(R.drawable.ic_playlists_empty, "You haven't created any playlist yet")
    }
}