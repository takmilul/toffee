package com.banglalink.toffee.ui.useractivities

import com.banglalink.toffee.R
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.MyBaseAdapterV2

class ChannelSubscriptionListAdapter(callback: ChannelSubscriptionListItemCallback?):
    MyBaseAdapterV2<ChannelInfo>(callback) {
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.tab_subscriptions_list_item
    }
}