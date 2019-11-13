package com.banglalink.toffee.ui.home

import android.view.View
import com.banglalink.toffee.ui.player.ChannelInfo

interface OptionCallBack {
    fun onOptionClicked(anchor: View, channelInfo: ChannelInfo)
    fun viewAllVideoClick()
}