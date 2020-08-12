package com.banglalink.toffee.ui.useractivities

import com.banglalink.toffee.model.ChannelSubscriptionInfo
import com.banglalink.toffee.ui.common.SingleListItemCallback

interface ChannelSubscriptionListItemCallback: SingleListItemCallback<ChannelSubscriptionInfo> {
    fun onSubscribeClicked(item: ChannelSubscriptionInfo)
    fun onNotificationBellClicked(item: ChannelSubscriptionInfo, pos: Int)
}