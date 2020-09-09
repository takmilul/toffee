package com.banglalink.toffee.ui.useractivities

import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.SingleListItemCallback

interface ChannelSubscriptionListItemCallback: SingleListItemCallback<ChannelInfo> {
    fun onSubscribeClicked(item: ChannelInfo)
    fun onNotificationBellClicked(item: ChannelInfo, pos: Int)
}