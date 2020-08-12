package com.banglalink.toffee.ui.useractivities

import com.banglalink.toffee.R
import com.banglalink.toffee.model.ChannelSubscriptionInfo
import com.banglalink.toffee.ui.common.MyBaseAdapterV2

class ChannelSubscriptionListAdapter(callback: ChannelSubscriptionListItemCallback?):
    MyBaseAdapterV2<ChannelSubscriptionInfo>(callback) {
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.tab_subscriptions_list_item
    }
}