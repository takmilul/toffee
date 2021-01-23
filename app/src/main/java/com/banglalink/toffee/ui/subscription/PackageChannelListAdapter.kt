package com.banglalink.toffee.ui.subscription

import com.banglalink.toffee.R
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.MyBaseAdapter

class PackageChannelListAdapter: MyBaseAdapter<ChannelInfo>() {
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.package_channel_item
    }
}