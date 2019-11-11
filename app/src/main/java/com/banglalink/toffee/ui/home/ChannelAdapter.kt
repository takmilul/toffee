package com.banglalink.toffee.ui.home

import com.banglalink.toffee.R
import com.banglalink.toffee.ui.player.ChannelInfo
import com.foxrentacar.foxpress.ui.common.MyBaseAdapter

class ChannelAdapter(channelCallback:(ChannelInfo)->Unit={}): MyBaseAdapter<ChannelInfo>(channelCallback) {
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.live_tv_item
    }
}