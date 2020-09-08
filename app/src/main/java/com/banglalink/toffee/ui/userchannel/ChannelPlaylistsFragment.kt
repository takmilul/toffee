package com.banglalink.toffee.ui.userchannel

import androidx.lifecycle.ViewModelProvider
import com.banglalink.toffee.R
import com.banglalink.toffee.model.ChannelPlaylist
import com.banglalink.toffee.ui.common.SingleListFragmentV2
import com.banglalink.toffee.ui.common.SingleListItemCallback

class ChannelPlaylistsFragment : SingleListFragmentV2<ChannelPlaylist>(), SingleListItemCallback<ChannelPlaylist> {
    
    companion object {
        fun newInstance(): ChannelPlaylistsFragment {
            return ChannelPlaylistsFragment()
        }
    }
    
    override fun initAdapter() {
        mAdapter = ChannelPlaylistListAdapter(this)
        mViewModel = ViewModelProvider(this).get(ChannelPlaylistViewModel::class.java)
    }
    
    override fun onOpenMenu(item: ChannelPlaylist) {
        super.onOpenMenu(item)
    }
    
    override fun onItemClicked(item: ChannelPlaylist) {
        super.onItemClicked(item)
    }
    
    override fun getEmptyViewInfo(): Pair<Int, String?> {
        return Pair(R.drawable.ic_playlists_empty, "You haven't created any playlist yet")
    }
}