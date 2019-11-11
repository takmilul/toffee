package com.banglalink.toffee.model

import com.banglalink.toffee.ui.player.ChannelInfo

data class ContentBean(
    val channels: List<ChannelInfo>?,
    val count: Int,
    val totalCount: Int,
    val balance: Int,
    val systemTime:String
)