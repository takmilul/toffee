package com.banglalink.toffee.ui.userchannel

import com.banglalink.toffee.R
import com.banglalink.toffee.model.ChannelPlaylist
import com.banglalink.toffee.ui.common.MyBaseAdapterV2
import com.banglalink.toffee.ui.common.SingleListItemCallback

class ChannelPlaylistListAdapter(callback: SingleListItemCallback<ChannelPlaylist>?): MyBaseAdapterV2<ChannelPlaylist>(callback) {
    
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.list_channel_playlist
    }
}