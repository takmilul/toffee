package com.banglalink.toffee.ui.userchannel

import com.banglalink.toffee.R
import com.banglalink.toffee.model.ChannelVideo
import com.banglalink.toffee.ui.common.MyBaseAdapterV2
import com.banglalink.toffee.ui.common.SingleListItemCallback

class ChannelVideoListAdapter(callback: SingleListItemCallback<ChannelVideo>?): MyBaseAdapterV2<ChannelVideo>(callback) {
    
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.list_item_channel_videos
    }
}