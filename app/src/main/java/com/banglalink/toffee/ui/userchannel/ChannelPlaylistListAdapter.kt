package com.banglalink.toffee.ui.userchannel

import com.banglalink.toffee.R
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.MyBaseAdapterV2
import com.banglalink.toffee.ui.common.SingleListItemCallback

class ChannelPlaylistListAdapter(callback: SingleListItemCallback<ChannelInfo>?): MyBaseAdapterV2<ChannelInfo>(callback) {
    
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.list_item_channel_playlist
    }
}