package com.banglalink.toffee.listeners

import android.view.View
import com.banglalink.toffee.model.ChannelInfo

interface OptionCallBack {
    fun onOptionClicked(anchor: View, channelInfo: ChannelInfo)
    fun viewAllVideoClick()
}